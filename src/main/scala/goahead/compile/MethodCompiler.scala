package goahead.compile

import goahead.Logger
import goahead.ast.Node
import goahead.ast.Node.Statement
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._

import scala.annotation.tailrec
import AstDsl._
import Helpers._

trait MethodCompiler extends Logger {
  import MethodCompiler._

  def compile(
    cls: ClassNode,
    method: Method,
    imports: Imports,
    mangler: Mangler
  ): (Imports, Node.FunctionDeclaration) = {
    logger.debug(s"Compiling method: ${cls.name}::${method.name}")
    logger.trace("ASM:\n    " + method.asmString.replace("\n", "\n    "))
    // Compile the sets
    val ctx = initContext(cls, method, imports, mangler, getLabelSets(method))
    compileLabelSets(ctx).leftMap { case (ctx, compiledStmts) =>
      (buildFuncDecl _).tupled(postProcessStatements(ctx, compiledStmts)).leftMap { case (ctx, funcDecl) =>
        ctx.imports -> funcDecl
      }
    }
  }

  protected def buildFuncDecl(ctx: Context, stmts: Seq[Node.Statement]): (Context, Node.FunctionDeclaration) = {
    val ctxAndNonPointerRecTyp =
      if (ctx.method.access.isAccessStatic) ctx.staticInstTypeExpr(ctx.cls.name)
      else ctx.instTypeExpr(ctx.cls.name)

    ctxAndNonPointerRecTyp.leftMap { case (ctx, nonPointerRecTyp) =>

      buildFuncType(ctx).leftMap { case (ctx, funcType) =>

        ctx -> funcDecl(
          rec = Some(field("this", nonPointerRecTyp.star)),
          name = ctx.mangler.methodName(ctx.method.name, ctx.method.desc),
          funcType = funcType,
          stmts = stmts
        )
      }
    }
  }

  protected def buildFuncType(ctx: Context): (Context, Node.FunctionType) = {
    // 0 is "this" in non-static
    val indexAdd = if (ctx.method.access.isAccessStatic) 0 else 1

    val ctxWithParams =
      IType.getArgumentTypes(ctx.method.desc).zipWithIndex.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
        case ((ctx, params), (argType, argIndex)) =>
          ctx.typeToGoType(argType).leftMap { case (ctx, typ) =>
            val name = "var" + (argIndex + indexAdd)
            ctx -> (params :+ (name -> typ))
          }
      }

    ctxWithParams.leftMap { case (ctx, params) =>
      val ctxWithResultTypOpt = IType.getReturnType(ctx.method.desc) match {
        case IType.VoidType => ctx -> None
        case retTyp => ctx.typeToGoType(retTyp).leftMap { case (ctx, typ) => ctx -> Some(typ) }
      }

      ctxWithResultTypOpt.leftMap { case (ctx, resultTypOpt) => ctx -> funcType(params, resultTypOpt) }
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
    val initial = LabelSet(insns.head.asInstanceOf[LabelNode])
    insns.foldLeft(Seq(initial)) { case (labelSets, insn) =>
      insn match {
        case n: FrameNode =>
          require(labelSets.last.newFrame.isEmpty, "Expected label to not have two frames")
          labelSets.init :+ labelSets.last.copy(newFrame = Some(n))
        case n: LineNumberNode =>
          if (n.start.getLabel != labelSets.last.node.getLabel) labelSets
          else labelSets.init :+ labelSets.last.copy(line = Some(n.line))
        case n: LabelNode =>
          labelSets :+ LabelSet(n)
        case n =>
          labelSets.init :+ labelSets.last.copy(insns = labelSets.last.insns :+ n)
      }
    }
  }

  protected def compileLabelSets(ctx: Context): (Context, Seq[Node.Statement]) =
    recursiveCompileLabelSets(ctx, _.toSeq)

  @tailrec
  protected final def recursiveCompileLabelSets(
    origCtx: Context,
    stmtFn: Seq[Node.Statement] => Seq[Node.Statement]
  ): (Context, Seq[Node.Statement]) = {
    if (origCtx.sets.isEmpty) origCtx -> stmtFn(Nil)
    else {
      // Compile the label w/ framing info...
      val (ctx, newStmtFn) = compileLabel(origCtx).leftMap { case (ctx, stmts) =>

        // We need to check whether we are an anon function or not
        // and make a wrapping function accordingly
        val labelName = ctx.sets.head.node.getLabel.toString
        val newStmtFn: Seq[Node.Statement] => Seq[Node.Statement] = ctx.labelsAsFunctions.get(labelName) match {
          case None =>
            // This is a regular labeled statement
            nextStmts => stmtFn(labeled(labelName, stmts) +: nextStmts)
          case Some(funcType) =>
            // This is an anon function which we assign to a var which we expect
            // to be declared at the very top of the func

            // We have to add a statement to the bottom here that goes-to the next label
            val ctxAndStmts = ctx.sets.lift(1) match {
              case None =>
                ctx -> stmts
              case Some(nextSet) =>
                val nextLabel = nextSet.node.getLabel.toString
                ctx.copy(usedLabels = ctx.usedLabels + nextLabel) -> (stmts :+ goto(nextLabel))
            }

            ctxAndStmts.leftMap { case (ctx, stmts) =>
              nextStmts => stmtFn(labelName.toIdent.assignExisting(funcType.toFuncLit(stmts)) +: nextStmts)
            }
        }

        ctx -> newStmtFn
      }

      // Make the tail recursive call with the top set taken off
      recursiveCompileLabelSets(ctx.copy(sets = ctx.sets.tail), newStmtFn)
    }
  }

  protected def compileLabel(origCtx: Context): (Context, Seq[Node.Statement]) = {
    // Manip the context and get head statements for frame
    contextAndStatementsBeforeFrame(origCtx).leftMap { case (ctx, frameStmts) =>
      // TODO: does the frame only last the length of one label? Doubt it...
      compileLabelCode(ctx).leftMap { case (ctx, codeStmts) =>
        // Fix up the context after compilation
        contextAfterFrame(origCtx, ctx).leftMap { case (ctx, afterFrameStmts) =>
          ctx -> (frameStmts ++ afterFrameStmts ++ codeStmts)
        }
      }
    }
  }

  protected def contextAndStatementsBeforeFrame(ctx: Context): (Context, Seq[Node.Statement]) = {
    ctx.sets.head.newFrame match {
      case None =>
        ctx -> Nil
      case Some(frame) =>
        contextLocalsAndStatementsBeforeFrame(contextStackBeforeFrame(ctx, frame), frame)
    }
  }

  protected def contextStackBeforeFrame(ctx: Context, f: FrameNode): Context = {
    if (f.stack == null || f.stack.isEmpty) ctx.copy(stack = Stack.empty)
    else {
      import scala.collection.JavaConversions._
      val initial = ctx.copy(stack = Stack.empty) -> Seq.empty[(String, Node.Expression)]
      val ctxWithFuncParams = f.stack.zipWithIndex.foldLeft(initial) {
        case ((ctx, prevFuncParams), (stackItem, index)) =>

          val name = s"stack$index"

          val exprType = IType.fromFrameVarType(ctx.cls, stackItem)

          ctx.typeToGoType(exprType).leftMap  { case (ctx, goType) =>
            ctx.copy(
              stack = ctx.stack.push(TypedExpression.namedVar(name, exprType))
            ) -> (prevFuncParams :+ (name -> goType))
          }
      }

      ctxWithFuncParams.leftMap { case (ctx, funcParams) =>
        // If there are params, we need to add the function type for this label
        if (funcParams.isEmpty) ctx
        else ctx.copy(
          labelsAsFunctions = ctx.labelsAsFunctions + (ctx.sets.head.node.getLabel.toString -> funcType(funcParams))
        )
      }
    }
  }

  protected def contextLocalsAndStatementsBeforeFrame(
    origCtx: Context,
    f: FrameNode
  ): (Context, Seq[Node.Statement]) = {
    import scala.collection.JavaConversions._

    if (f.local == null || f.local.isEmpty) origCtx -> Nil
    else if (f.`type` == Opcodes.F_CHOP) {
      // If it's chop, remove the last X locals
      origCtx.copy(localVars = origCtx.localVars.dropRight(f.local.size)) -> Nil
    } else {

      // Otherwise, we add/decls the locals as necessary
      val ctxToStartWith = f.`type` match {
        case Opcodes.F_APPEND => origCtx
        // Clear out the existing locals, though we may append some at the end down there
        case Opcodes.F_FULL => origCtx.copy(localVars = IndexedSeq.empty)
        case typ => sys.error(s"Unknown frame type: $typ")
      }

      val ctxAndStmts = f.local.foldLeft(ctxToStartWith -> Seq.empty[Node.Statement]) {
        case ((ctx, stmts), localVar) =>
          val exprType = IType.fromFrameVarType(ctx.cls, localVar)

          ctx.appendLocalVar(exprType).leftMap { case (ctx, localVarRef) =>
            ctx.typeToGoType(localVarRef.typ).leftMap { case (ctx, goType) =>
              // We're allowed to assume the local var is always an identifier
              ctx -> (stmts :+ varDecl(localVarRef.name, goType).toStmt)
            }
          }
      }

      // In the case of F_FULL, we need to tack on the old locals that were there (TODO: right?)
      if (f.`type` != Opcodes.F_FULL) ctxAndStmts
      else ctxAndStmts.leftMap { case (ctx, stmts) =>
        ctx.copy(localVars = ctx.localVars ++ origCtx.localVars.drop(f.local.size())) -> stmts
      }
    }
  }

  protected def contextAfterFrame(origCtx: Context, ctx: Context): (Context, Seq[Node.Statement]) = {
    /*  protected def addTempVars(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    if (ctx.tempVars.isEmpty) ctx -> stmts
    else createVarDecl(ctx, ctx.tempVars).leftMap { case (ctx, declStmt) => ctx -> (declStmt +: stmts) }
  }*/
    // Technically we only need to fix up locals, not the stack
    val updatedCtx = ctx.sets.head.newFrame match {
      case None => ctx
      case Some(f) => f.`type` match {
        case Opcodes.F_SAME | Opcodes.F_SAME1 =>
          // Nothing to change
          ctx
        case Opcodes.F_APPEND =>
          // Remove the appended locals
          ctx.copy(localVars = ctx.localVars.dropRight(f.local.size))
        case Opcodes.F_CHOP =>
          // Put the removed ones back...note, we do this instead of just completely put the
          // orig back because we want whatever type inference we got during this frame
          ctx.copy(localVars = ctx.localVars ++ origCtx.localVars.takeRight(f.local.size))
        case Opcodes.F_FULL =>
          // Put only the locals back that we took away
          ctx.copy(localVars = origCtx.localVars.take(f.local.size()) ++ ctx.localVars.drop(f.local.size()))
        case code => sys.error(s"Unrecognized frame code $code")
      }
    }
    // Now we will include the temp vars that were added after the original and are NOT on the stack
    val (tempVarsToKeep, tempVarsToWriteStmts) =
      updatedCtx.tempVars.drop(origCtx.tempVars.size).partition(updatedCtx.stack.items.contains)
    if (tempVarsToWriteStmts.isEmpty) updatedCtx -> Nil
    else createVarDecl(updatedCtx, tempVarsToWriteStmts).leftMap { case (ctx, stmt) =>
      ctx.copy(tempVars = ctx.tempVars.take(origCtx.tempVars.size) ++ tempVarsToKeep) -> Seq(stmt)
    }
  }

  protected def compileLabelCode(ctx: Context): (Context, Seq[Node.Statement]) = {
    insnCompiler.compile(ctx, ctx.sets.head.insns)
  }

  protected def statementPostProcessors = Seq(
    removeUnusedLabels _,
    addTempVars _,
    addLocalVars _,
    addLabelFuncDecls _
  )

  protected def postProcessStatements(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    statementPostProcessors.foldLeft(ctx -> stmts) {
      case (ret, postProcessor) => postProcessor.tupled(ret)
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

  protected def addTempVars(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    if (ctx.tempVars.isEmpty) ctx -> stmts
    else createVarDecl(ctx, ctx.tempVars).leftMap { case (ctx, declStmt) => ctx -> (declStmt +: stmts) }
  }

  protected def addLocalVars(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    val argTypeCount = IType.getArgumentTypes(ctx.method.desc).length
    require(ctx.localVars.size >= argTypeCount)
    // Add all local var defs
    val localVarsToDecl = ctx.localVars.drop(argTypeCount)
    if (localVarsToDecl.isEmpty) ctx -> stmts
    else createVarDecl(ctx, localVarsToDecl).leftMap { case (ctx, declStmt) => ctx -> (declStmt +: stmts) }
  }

  protected def createVarDecl(ctx: Context, vars: Seq[TypedExpression]): (Context, Node.Statement) = {
    // Collect them all and then send at once to a single var decl
    val ctxAndNamedTypes = vars.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
      case ((ctx, prevNamedTypes), localVar) =>
        ctx.typeToGoType(localVar.typ).leftMap { case (ctx, typ) =>
          ctx -> (prevNamedTypes :+ (localVar.name -> typ))
        }
    }
    ctxAndNamedTypes.leftMap { case (ctx, namedTypes) =>
      ctx -> varDecls(namedTypes: _*).toStmt
    }
  }

  protected def addLabelFuncDecls(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    if (ctx.labelsAsFunctions.isEmpty) ctx -> stmts
    else {
      val ctxAndNamedTypes = ctx.labelsAsFunctions.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
        case ((ctx, prevNamedTypes), (name, funcType)) =>
          // Have to remove the names from the func types
          ctx -> (prevNamedTypes :+ (name -> funcType.sansNames))
      }
      ctxAndNamedTypes.leftMap { case (ctx, namedTypes) =>
        ctx -> (varDecls(namedTypes: _*).toStmt +: stmts)
      }
    }
  }
}

object MethodCompiler extends MethodCompiler {
  case class LabelSet(
    node: LabelNode,
    newFrame: Option[FrameNode] = None,
    line: Option[Int] = None,
    insns: Seq[AbstractInsnNode] = Nil
  )

  case class Context(
    cls: ClassNode,
    method: Method,
    imports: Imports,
    mangler: Mangler,
    sets: Seq[LabelSet],
    usedLabels: Set[String] = Set.empty,
    labelsAsFunctions: Map[String, Node.FunctionType] = Map.empty,
    stack: Stack = Stack.empty,
    tempVars: IndexedSeq[TypedExpression] = IndexedSeq.empty,
    localVars: IndexedSeq[TypedExpression] = IndexedSeq.empty
  ) extends Contextual[Context] {
    override def updatedImports(mports: Imports) = copy(imports = mports)
  }

  trait PanicOnlyMethodCompiler extends MethodCompiler {
    override def compileLabelSets(ctx: Context): (Context, Seq[Statement]) = {
      ctx -> "panic".toIdent.call("Not Implemented".toLit.singleSeq).toStmt.singleSeq
    }
  }

  object PanicOnlyMethodCompiler extends PanicOnlyMethodCompiler
}
