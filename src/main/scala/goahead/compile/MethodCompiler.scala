package goahead.compile

import goahead.Logger
import goahead.ast.Node
import goahead.compile.AstDsl._
import goahead.compile.Helpers._
import goahead.compile.insn.InsnCompiler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._

trait MethodCompiler extends Logger {
  import MethodCompiler._

  def compile(
    conf: Config,
    cls: Cls,
    method: Method,
    imports: Imports,
    mangler: Mangler
  ): (Imports, Node.FunctionDeclaration) = {
    logger.debug(s"Compiling method: ${cls.name}.${method.name}${method.desc}")
    logger.trace("ASM:\n    " + method.asmString.replace("\n", "\n    "))
    // Compile the sets
    val ctx = initContext(conf, cls, method, imports, mangler, getLabelSets(method))
    buildStmts(ctx).map { case (ctx, stmts) =>
      buildFuncDecl(ctx, stmts).map { case (ctx, funcDecl) =>
        ctx.imports -> funcDecl
      }
    }
  }

  protected def initContext(
    conf: Config,
    cls: Cls,
    method: Method,
    imports: Imports,
    mangler: Mangler,
    sets: Seq[LabelSet]
  ) = {
    // For every argument type, we have to pre-add a local var
    IType.getArgumentTypes(method.desc).foldLeft(Context(conf, cls, method, imports, mangler, sets)) {
      case (ctx, argType) => ctx.appendLocalVar(argType)._1
    }
  }

  protected def buildStmts(ctx: Context): (Context, Seq[Node.Statement]) = {
    // Abstract methods just need a panic
    if (ctx.method.access.isAccessAbstract) ctx -> "panic".toIdent.call(Seq("Abstract".toLit)).toStmt.singleSeq else {
      compileLabelSets(ctx).map { case (ctx, compiledStmts) =>
        postProcessStatements(ctx, compiledStmts)
      }
    }
  }

  protected def insnCompiler: InsnCompiler = InsnCompiler

  protected def getLabelSets(node: Method): Seq[LabelSet] = {
    if (node.access.isAccessAbstract) Seq.empty else {
      val insns = node.instructions
      require(insns.headOption.exists(_.isInstanceOf[LabelNode]), "Expected label to be first insn")
      val initial = LabelSet(insns.head.asInstanceOf[LabelNode])
      insns.foldLeft(Seq(initial)) { case (labelSets, insn) =>
        insn match {
          case n: FrameNode =>
            require(labelSets.last.newFrame.isEmpty, "Expected label to not have two frames")
            labelSets.init :+ labelSets.last.copy(newFrame = Some(n))
          case n: LineNumberNode =>
            if (n.start.getLabel != labelSets.last.label.getLabel) labelSets
            else labelSets.init :+ labelSets.last.copy(line = Some(n.line))
          case n: LabelNode =>
            labelSets.init :+ labelSets.last.copy(nextLabel = Some(n)) :+ LabelSet(
              label = n,
              exceptionType = node.tryCatchBlocks.find(_.handler.getLabel == n).map { block =>
                Option(block.`type`).getOrElse("java/lang/Throwable")
              }
            )
          case n =>
            labelSets.init :+ labelSets.last.copy(insns = labelSets.last.insns :+ n)
        }
      }
    }
  }

  protected def compileLabelSets(ctx: Context): (Context, Seq[Node.Statement]) = {
    ctx.sets.foldLeft(ctx -> Seq.empty[Node.Statement]) { case ((ctx, stmts), labelSet) =>
      compileLabelSet(ctx, labelSet).map { case (ctx, labeledStatement) =>
        ctx -> (stmts :+ labeledStatement)
      }
    }
  }

  protected def compileLabelSet(ctx: Context, labelSet: LabelSet): (Context, Node.LabeledStatement) = {
    // Setup the frame
    logger.trace(s"Context before setup of ${labelSet.pretty}: ${ctx.prettyAppend}")
    setupLabel(ctx, labelSet).map { ctx =>
      // Build the code
      logger.trace(s"Context after setup of ${labelSet.pretty}: ${ctx.prettyAppend}")
      insnCompiler.compile(ctx, labelSet.insns).map { case (ctx, stmts) =>
        // Post process it
        logger.trace(s"Context after compile of ${labelSet.pretty}: ${ctx.prettyAppend}")
        postProcessLabel(ctx, labelSet, stmts).map { case (ctx, labelStmt) =>
          logger.trace(s"Context after post process of ${labelSet.pretty}: ${ctx.prettyAppend}")
          ctx -> labelStmt
        }
      }
    }
  }

  protected def setupLabel(origCtx: Context, labelSet: LabelSet): Context = {
    labelSet.newFrame match {
      case None => origCtx
      case Some(frame) =>
        // Add a stack variable for each stack item here
        val initCtx = origCtx.copy(stack = Stack.empty)
        val ctx = origCtx.frameStack(frame).zipWithIndex.foldLeft(initCtx) {
          case (ctx, (stackType, index)) =>
            val stackVar = TypedExpression.namedVar(s"${labelSet.label.getLabel}_stack$index", stackType)
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
            ctx.dropLocalVars(frame.local.size())
          case Opcodes.F_FULL =>
            val locals = ctx.frameLocals(frame)
            if (locals.length == ctx.localVars.size) ctx
            else if (locals.length < ctx.localVars.size) ctx.takeLocalVars(locals.length)
            else locals.drop(ctx.localVars.size).foldLeft(ctx) { case (ctx, localType) =>
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
    // TODO: this can be dangerous if they are embedded in expressions that are used after the frame
    // but that does not seem to happen in practice
    val (tempVarsOnStack, tempVarsNotOnStack) = ctx.localTempVars.partition(ctx.stack.items.contains)
    // Make local decls out of ones not on stack and not already in function vars
    val tempVarsNotOnStackAndNotAtFuncLevel = tempVarsNotOnStack.filterNot(ctx.functionVars.contains)
    val ctxAndVarDecl =
      if (tempVarsNotOnStackAndNotAtFuncLevel.isEmpty) ctx -> None
      else ctx.createVarDecl(tempVarsNotOnStackAndNotAtFuncLevel).map(_ -> Some(_))

    // Leave the existing ones on the stack and in the temp set and add them to func-level vars
    ctxAndVarDecl.map { case (ctx, maybeDecl) =>

      // If the last insn of the label is not an unconditional jump, we need to prepare to
      // fall through
      val ctxAndAddOnStmts = labelSet.nextLabel match {
        case Some(nextLabel) if !labelSet.insns.lastOption.exists(_.isUnconditionalJump) =>
          ctx.prepareToGotoLabel(nextLabel)
        case _ => ctx -> Nil
      }

      ctxAndAddOnStmts.map { case (ctx, addOnStmts) =>
        ctx.copy(localTempVars = tempVarsOnStack, functionVars = ctx.functionVars ++ tempVarsOnStack) ->
          labeled(labelSet.label.getLabel.toString, maybeDecl.toSeq ++ stmts ++ addOnStmts)
      }
    }
  }

  protected def statementPostProcessors: Seq[PostProcessor] = Seq(
    postprocess.ApplyTryCatch,
    postprocess.AddFunctionVars,
    postprocess.RemoveUnusedLabels
  )

  protected def postProcessStatements(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    statementPostProcessors.foldLeft(ctx -> stmts) {
      case (ret, postProcessor) => postProcessor.tupled(ret)
    }
  }

  protected def buildFuncDecl(ctx: Context, stmts: Seq[Node.Statement]): (Context, Node.FunctionDeclaration) = {
    signatureCompiler.buildFuncDecl(ctx, ctx.method, stmts).map { case (ctx, funcDecl) =>
      // As a special case, default interface functions have the receiver removed, "this" set as
      // the first param instead, and the name changed to the default version
      if (!ctx.cls.access.isAccessInterface || ctx.method.access.isAccessStatic) ctx -> funcDecl else {
        ctx -> funcDecl.copy(
          name = ctx.mangler.interfaceDefaultMethodName(ctx.cls.name, ctx.method.name, ctx.method.desc).toIdent,
          receivers = Nil,
          typ = funcDecl.typ.copy(
            parameters =
              field("this", ctx.mangler.instanceInterfaceName(ctx.cls.name).toIdent) +: funcDecl.typ.parameters
          )
        )
      }
    }
  }

  protected def signatureCompiler: SignatureCompiler = SignatureCompiler
}

object MethodCompiler extends MethodCompiler {
  type PostProcessor = (Context, Seq[Node.Statement]) => (Context, Seq[Node.Statement])

  case class LabelSet(
    label: LabelNode,
    exceptionType: Option[String] = None,
    newFrame: Option[FrameNode] = None,
    line: Option[Int] = None,
    insns: Seq[AbstractInsnNode] = Nil,
    nextLabel: Option[LabelNode] = None
  ) {
    def pretty: String = label.getLabel.toString + newFrame.map(" - " + _.pretty).getOrElse("")
    def endsWithUnconditionalJump = insns.nonEmpty && insns.last.isUnconditionalJump
  }

  case class Context(
    conf: Config,
    cls: Cls,
    method: Method,
    imports: Imports,
    mangler: Mangler,
    sets: Seq[LabelSet],
    localVars: LocalVars,
    usedLabels: Set[String] = Set.empty,
    labelsAsFunctions: Map[String, Node.FunctionType] = Map.empty,
    stack: Stack = Stack.empty,
    localTempVars: IndexedSeq[TypedExpression] = IndexedSeq.empty,
    functionVars: Seq[TypedExpression] = IndexedSeq.empty
  ) extends Contextual[Context] {
    override def updatedImports(mports: Imports) = copy(imports = mports)

    def prettyLines: Seq[String] = {
      Seq("Context:") ++
        stack.prettyLines.map("  " + _) ++
        localVars.prettyLines.map("  " + _)
    }

    def prettyAppend: String = prettyLines.mkString("\n  ", "\n  ", "")
  }

  object Context {
    def apply(
      conf: Config,
      cls: Cls,
      method: Method,
      imports: Imports,
      mangler: Mangler,
      sets: Seq[LabelSet]
    ): Context = {
      Context(conf, cls, method, imports, mangler, sets, LocalVars(
        thisVar = if (method.access.isAccessStatic) None else Some(
          TypedExpression.namedVar("this", IType.getObjectType(cls.name))
        )
      ))
    }
  }
}
