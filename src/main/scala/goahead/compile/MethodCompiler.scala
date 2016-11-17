package goahead.compile

import goahead.Logger
import goahead.ast.Node
import goahead.compile.AstDsl._
import goahead.compile.Helpers._
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._

trait MethodCompiler extends Logger {
  import MethodCompiler._

  def compile(
    cls: ClassNode,
    method: Method,
    imports: Imports,
    mangler: Mangler
  ): (Imports, Node.FunctionDeclaration) = {
    logger.debug(s"Compiling method: ${cls.name}.${method.name}${method.desc}")
    logger.trace("ASM:\n    " + method.asmString.replace("\n", "\n    "))
    // Compile the sets
    val ctx = initContext(cls, method, imports, mangler, getLabelSets(method))
    compileLabelSets(ctx).leftMap { case (ctx, compiledStmts) =>
      postProcessStatements(ctx, compiledStmts).leftMap { case (ctx, stmts) =>
        signatureCompiler.buildFuncDecl(ctx, method, stmts).leftMap { case (ctx, funcDecl) =>
          ctx.imports -> funcDecl
        }
      }
    }
  }

  protected def initContext(
    cls: ClassNode,
    method: Method,
    imports: Imports,
    mangler: Mangler,
    sets: Seq[LabelSet]
  ) = {
    // For every argument type, we have to pre-add a local var
    IType.getArgumentTypes(method.desc).foldLeft(Context(cls, method, imports, mangler, sets)) {
      case (ctx, argType) => ctx.appendLocalVar(argType)._1
    }
  }

  protected def insnCompiler: InsnCompiler = InsnCompiler

  protected def getLabelSets(node: Method): Seq[LabelSet] = {
    val insns = node.instructions
    require(insns.headOption.exists(_.isInstanceOf[LabelNode]), "Expected label to be first insn")
    val initial = LabelSet(
      node = insns.head.asInstanceOf[LabelNode],
      handlerOf = node.tryCatchBlocks.find(_.handler.getLabel == insns.head.asInstanceOf[LabelNode].getLabel)
    )
    insns.foldLeft(Seq(initial)) { case (labelSets, insn) =>
      insn match {
        case n: FrameNode =>
          require(labelSets.last.newFrame.isEmpty, "Expected label to not have two frames")
          labelSets.init :+ labelSets.last.copy(newFrame = Some(n))
        case n: LineNumberNode =>
          if (n.start.getLabel != labelSets.last.node.getLabel) labelSets
          else labelSets.init :+ labelSets.last.copy(line = Some(n.line))
        case n: LabelNode =>
          labelSets :+ LabelSet(
            node = n,
            handlerOf = node.tryCatchBlocks.find(_.handler.getLabel == n.getLabel)
          )
        case n =>
          labelSets.init :+ labelSets.last.copy(insns = labelSets.last.insns :+ n)
      }
    }
  }

  protected def compileLabelSets(ctx: Context): (Context, Seq[Node.Statement]) = {
    ctx.sets.foldLeft(ctx -> Seq.empty[Node.Statement]) { case ((ctx, stmts), labelSet) =>
      compileLabelSet(ctx, labelSet).leftMap { case (ctx, labeledStatement) =>
        ctx -> (stmts :+ labeledStatement)
      }
    }
  }

  protected def compileLabelSet(ctx: Context, labelSet: LabelSet): (Context, Node.LabeledStatement) = {
    // Setup the frame
    logger.trace(s"Context before setup of ${labelSet.pretty}: ${ctx.prettyAppend}")
    setupFrame(ctx, labelSet).map { ctx =>
      // Build the code
      logger.trace(s"Context after setup of ${labelSet.pretty}: ${ctx.prettyAppend}")
      insnCompiler.compile(ctx, labelSet.insns).leftMap { case (ctx, stmts) =>
        // Post process it
        logger.trace(s"Context after compile of ${labelSet.pretty}: ${ctx.prettyAppend}")
        postProcessLabel(ctx, labelSet, stmts).leftMap { case (ctx, labelStmt) =>
          logger.trace(s"Context after post process of ${labelSet.pretty}: ${ctx.prettyAppend}")
          ctx -> labelStmt
        }
      }
    }
  }

  protected def setupFrame(origCtx: Context, labelSet: LabelSet): Context = {
    labelSet.newFrame match {
      case None => origCtx
      case Some(frame) =>
        // Add a stack variable for each stack item here
        val initCtx = origCtx.copy(stack = Stack.empty)
        val ctx = origCtx.frameStack(frame).zipWithIndex.foldLeft(initCtx) {
          case (ctx, (stackType, index)) =>
            val stackVar = TypedExpression.namedVar(s"${labelSet.node.getLabel}_stack$index", stackType)
            ctx.copy(functionVars = ctx.functionVars :+ stackVar).stackPushed(stackVar)
        }

        // Now with the local variables
        frame.`type` match {
          case Opcodes.F_SAME1 | Opcodes.F_SAME =>
            ctx
          case Opcodes.F_APPEND =>
            ctx.frameLocals(frame).foldLeft(ctx) { case (ctx, localType) =>
              ctx.appendLocalVar(localType)._1
            }
          case Opcodes.F_CHOP =>
            ctx.copy(localVars = ctx.localVars.dropRight(ctx.frameLocals(frame).size))
          case Opcodes.F_FULL =>
            val locals = ctx.frameLocals(frame)
            if (locals.length == ctx.localVars.length) ctx
            else if (locals.length < ctx.localVars.length) ctx.copy(localVars = ctx.localVars.take(locals.length))
            else locals.drop(ctx.localVars.length).foldLeft(ctx) { case (ctx, localType) =>
              ctx.appendLocalVar(localType)._1
            }
        }
    }
  }

  protected def postProcessLabel(
    ctx: Context,
    labelSet: LabelSet,
    stmts: Seq[Node.Statement]
  ): (Context, Node.LabeledStatement) = {
    // All temp vars that are in the temp var section but are not on the stack are removed
    // from the temp var section and decld. If they are on the stack and not already in
    // functionVars, they get added to function vars
    val (tempVarsOnStack, tempVarsNotOnStack) = ctx.localTempVars.partition(ctx.stack.items.contains)
    // Make local decls out of ones not on stack and not already in function vars
    val ctxAndVarDecl =
    if (tempVarsNotOnStack.isEmpty) ctx -> None
    else createVarDecl(ctx, tempVarsNotOnStack.filterNot(ctx.functionVars.contains)).leftMap(_ -> Some(_))

    // Leave the existing ones on the stack and in the temp set and add them to func-level vars
    ctxAndVarDecl.leftMap { case (ctx, maybeDecl) =>
      ctx.copy(localTempVars = tempVarsOnStack, functionVars = ctx.functionVars ++ tempVarsOnStack) ->
        labeled(labelSet.node.getLabel.toString, maybeDecl.toSeq ++ stmts)
    }
  }

  protected def statementPostProcessors = Seq(
    applyTryCatch _,
    removeUnusedLabels _,
    addFunctionVars _
  )

  protected def postProcessStatements(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    statementPostProcessors.foldLeft(ctx -> stmts) {
      case (ret, postProcessor) => postProcessor.tupled(ret)
    }
  }

  protected def addFunctionVars(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Add all function vars not part of the arg list
    val argTypeCount = IType.getArgumentTypes(ctx.method.desc).length
    require(ctx.functionVars.size >= argTypeCount)
    val varsToDecl = ctx.functionVars.drop(argTypeCount)
    if (varsToDecl.isEmpty) ctx -> stmts
    else createVarDecl(ctx, varsToDecl).leftMap { case (ctx, declStmt) => ctx -> (declStmt +: stmts) }
  }

  protected def applyTryCatch(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // If there are no try/catch, we do nothing
    val tryCatchNodes = ctx.method.tryCatchBlocks
    if (tryCatchNodes.isEmpty) ctx -> stmts else {
      // We must have a new statement at the top for holding the current label
      val labelVarDecl = varDecl("currentLabel", "string".toIdent).toStmt

      // We have to go over every top-level label statement and set the current label as the first statement
      val updatedStmts = stmts.map {
        case Node.LabeledStatement(Node.Identifier(label), Node.BlockStatement(stmts)) =>
          labeled(label, "currentLabel".toIdent.assignExisting(label.toLit) +: stmts)
        case stmt =>
          stmt
      }

      // Now we have to have recover code
      ctx.withImportAlias("fmt").leftMap { case (ctx, fmt) =>
        val recoverStmt = iff(
          init = Some("r".toIdent.assignDefine("recover".toIdent.call())),
          lhs = "r".toIdent,
          op = Node.Token.Neq,
          rhs = NilExpr,
          body = Seq("panic".toIdent.call(Seq(
            fmt.toIdent.sel("Sprintf").call(Seq(
              "Got panic at label %v: %v".toLit,
              "currentLabel".toIdent,
              "r".toIdent
            ))
          )).toStmt)
        )
        val deferStmt = funcType(Nil, None).toFuncLit(Seq(recoverStmt)).call().defer
        ctx -> (Seq(labelVarDecl, deferStmt) ++ updatedStmts)
      }
    }
  }

  protected def removeUnusedLabels(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // This shouldn't blow up the stack too much, no need reworking to tailrec
    // We know the labeled statements are not in if statements, TODO: right?
    stmts.foldLeft(ctx -> Seq.empty[Node.Statement]) { case ((ctx, stmts), stmt) =>
      stmt match {
        case labelStmt @ Node.LabeledStatement(Node.Identifier(label), innerStmt) =>
          removeUnusedLabels(ctx, Seq(innerStmt)).leftMap { case (ctx, newInnerStmts) =>
            if (!ctx.usedLabels.contains(label)) ctx -> (stmts ++ newInnerStmts)
            else {
              require(newInnerStmts.length == 1)
              ctx -> (stmts :+ labelStmt.copy(statement = newInnerStmts.head))
            }
          }
        case Node.BlockStatement(blockStmts) =>
          // If the block is empty, just remove it
          if (blockStmts.isEmpty) ctx -> stmts
          else removeUnusedLabels(ctx, blockStmts).leftMap { case (ctx, newStmts) =>
            // Only put a block around the statement if it's not already a single block
            newStmts match {
              case Seq(Node.BlockStatement(innerStmts)) => ctx -> (stmts ++ innerStmts)
              case _ => ctx -> (stmts :+ block(newStmts))
            }
          }
        case other =>
          ctx -> (stmts :+ other)
      }
    }
  }

  protected def createVarDecl(ctx: Context, vars: Seq[TypedExpression]): (Context, Node.Statement) = {
    // Collect them all and then send at once to a single var decl
    val ctxAndNamedTypes = vars.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
      case ((ctx, prevNamedTypes), localVar) =>
        // We ignore undefined types here...
        localVar.typ match {
          case IType.Undefined =>
            ctx -> prevNamedTypes
          case _ =>
            ctx.typeToGoType(localVar.typ).leftMap { case (ctx, typ) =>
              ctx -> (prevNamedTypes :+ (localVar.name -> typ))
            }
        }
    }
    ctxAndNamedTypes.leftMap { case (ctx, namedTypes) =>
      ctx -> varDecls(namedTypes: _*).toStmt
    }
  }

  protected def signatureCompiler: SignatureCompiler = SignatureCompiler
}

object MethodCompiler extends MethodCompiler {
  case class LabelSet(
    node: LabelNode,
    newFrame: Option[FrameNode] = None,
    line: Option[Int] = None,
    insns: Seq[AbstractInsnNode] = Nil,
    handlerOf: Option[TryCatchBlockNode] = None
  ) {
    def pretty: String = node.getLabel.toString + newFrame.map(" - " + _.pretty).getOrElse("")
    def endsWithUnconditionalJump = insns.nonEmpty && insns.last.isUnconditionalJump
  }

  case class Context(
    cls: ClassNode,
    method: Method,
    imports: Imports,
    mangler: Mangler,
    sets: Seq[LabelSet],
    usedLabels: Set[String] = Set.empty,
    labelsAsFunctions: Map[String, Node.FunctionType] = Map.empty,
    stack: Stack = Stack.empty,
    localTempVars: IndexedSeq[TypedExpression] = IndexedSeq.empty,
    localVars: IndexedSeq[TypedExpression] = IndexedSeq.empty,
    functionVars: Seq[TypedExpression] = IndexedSeq.empty
  ) extends Contextual[Context] {
    override def updatedImports(mports: Imports) = copy(imports = mports)

    def prettyLines: Seq[String] = {
      Seq("Context:") ++ stack.prettyLines.map("  " + _) ++ Seq("  Locals:") ++
        localVars.zipWithIndex.map { case (v, i) => s"    $i: ${v.pretty}" }
    }

    def prettyAppend: String = prettyLines.mkString("\n  ", "\n  ", "")
  }
}
