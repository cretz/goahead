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
    cls: Cls,
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
    cls: Cls,
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
    setupLabel(ctx, labelSet).map { ctx =>
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
      else ctx.createVarDecl(tempVarsNotOnStack.filterNot(ctx.functionVars.contains)).leftMap(_ -> Some(_))

    // Leave the existing ones on the stack and in the temp set and add them to func-level vars
    ctxAndVarDecl.leftMap { case (ctx, maybeDecl) =>

      // If the last insn of the label is not an unconditional jump, we need to prepare to
      // fall through
      val ctxAndAddOnStmts = labelSet.nextLabel match {
        case Some(nextLabel) if !labelSet.insns.lastOption.exists(_.isUnconditionalJump) =>
          ctx.prepareToGotoLabel(nextLabel)
        case _ => ctx -> Nil
      }

      ctxAndAddOnStmts.leftMap { case (ctx, addOnStmts) =>
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
    cls: Cls,
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
