package goahead.compile

import goahead.ast.Node
import org.objectweb.asm.{Label, Opcodes, Type}
import org.objectweb.asm.tree._

import scala.annotation.tailrec

trait MethodCompiler {
  // TODO: make things extensible by removing finals and making only the recursive stuff private
  import Helpers._, MethodCompiler._, AstDsl._

  def compile(cls: ClassNode, method: MethodNode): Node.FunctionDeclaration = {
    val labelSets = getLabelSets(method)
    // TODO:
    // * getLabelSets
    // * Compile each label on its own
    ???
  }

  def getLabelSets(node: MethodNode): Seq[LabelSet] = {
    val insns = node.instructionIter.toSeq
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

  @tailrec
  final def compileLabelSets(
    ctx: Context,
    stmtFn: Option[Node.Statement] => Seq[Node.Statement]
  ): (Context, Seq[Node.Statement]) = {
    if (ctx.sets.isEmpty) ctx -> stmtFn(None)
    else {
      // Manip the context and get head statements for frame
      val (framedCtx, frameStmts) = contextAndStatementsForFrame(ctx)

      // Now the code
      val (codedCtx, codeStmts) = compileLabelCode(framedCtx)

      // We need to check whether we are an anon function or not
      // and make a wrapping function accordingly
      val labelName = ctx.sets.head.node.getLabel.toString
      val newStmtFn: Option[Node.Statement] => Seq[Node.Statement] = ctx.labelsAsFunctions.get(labelName) match {
        case None =>
          // This is a regular labeled statement
          childStmtOpt => stmtFn(Some(labeled(labelName, frameStmts ++ codeStmts ++ childStmtOpt)))
        case Some(funcType) =>
          // This is an anon function which we assign to a var which we expect
          // to be declared at the very top of the func
          childStmtOpt => stmtFn {
            Some(
              labelName.toIdent.assignExisting {
                funcType.toFuncLit(frameStmts ++ codeStmts ++ childStmtOpt)
              }
            )
          }
      }

      // Make the tail recursive call with the top set taken off
      compileLabelSets(ctx.copy(sets = ctx.sets.tail), newStmtFn)
    }
  }

  def contextAndStatementsForFrame(ctx: Context): (Context, Seq[Node.Statement]) = {
    ctx.sets.head.newFrame match {
      case None =>
        ctx -> Nil
      case Some(frame) =>
        contextAndStatementsForFrameLocals(contextForFrameStack(ctx, frame), frame)
    }
  }

  def contextForFrameStack(ctx: Context, f: FrameNode): Context = {
    if (f.stack == null || f.stack.isEmpty) ctx.copy(stack = Stack.empty)
    else {
      import scala.collection.JavaConversions._
      import ctx.mangler
      val initial = ctx.copy(stack = Stack.empty) -> Seq.empty[(String, Node.Expression)]
      val (newCtx, funcParams) = f.stack.zipWithIndex.foldLeft(initial) {
        case ((foldCtx, foldFuncParams), (stackItem, index)) =>
          val name = s"stack$index"
          TypedExpression.Type.fromFrameVarType(ctx.cls, stackItem) match {
            case exprType @ TypedExpression.Type.Simple(typ) =>
              val (newImports, goType) = typeToGoType(ctx.imports, typ)
              val newFoldCtx = foldCtx.copy(
                stack = foldCtx.stack.push(TypedExpression(expr = name.toIdent, typ = exprType, cheapRef = true)),
                imports = newImports
              )
              val newFoldFuncParams = foldFuncParams :+ name -> goType
              newFoldCtx -> newFoldFuncParams
            case _ =>
              // TODO: what about these stack vars that are undefined and what not?
              foldCtx -> foldFuncParams
          }
      }
      // If there are params, we need to add the function type for this label
      if (funcParams.isEmpty) newCtx
      else newCtx.copy(
        labelsAsFunctions = newCtx.labelsAsFunctions + (ctx.sets.head.node.getLabel.toString -> funcType(funcParams))
      )
    }
  }

  def contextAndStatementsForFrameLocals(
    ctx: Context,
    f: FrameNode
  )(implicit m: Mangler): (Context, Seq[Node.Statement]) = {
    import scala.collection.JavaConversions._
    // Filter out nulls which can happen with COP
    val acceptableLocals = f.local.zipWithIndex.filterNot(_._1 == null)
    acceptableLocals.foldLeft(ctx -> Seq.empty[Node.Statement]) { case ((ctx, stmts), (localVar, index)) =>
      val varNum = ctx.localVars.size + index
      TypedExpression.Type.fromFrameVarType(ctx.cls, localVar) match {
        case exprType @ TypedExpression.Type.Simple(typ) =>
          val (newImports, goType) = typeToGoType(ctx.imports, typ)
          val newCtx = ctx.copy(
            imports = newImports,
            localVars = ctx.localVars + (varNum -> TypedExpression(s"var$varNum".toIdent, exprType, cheapRef = true))
          )
          newCtx -> (stmts :+ varDecl(s"var$varNum", goType).toStmt)
        case _ =>
          // TODO: what about these local vars that are undefined and what not?
          ctx -> stmts
      }
    }
  }

  @tailrec
  def compileLabelCode(ctx: Context): (Context, Seq[Node.Statement]) = {
    // TODO: one instruction at a time and recursively call this
    ???
  }
}

object MethodCompiler {
  case class LabelSet(
    node: LabelNode,
    newFrame: Option[FrameNode] = None,
    line: Option[Int] = None,
    insns: Seq[AbstractInsnNode] = Nil
  )

  case class Context(
    cls: ClassNode,
    method: MethodNode,
    imports: Imports,
    mangler: Mangler = Mangler.Simple,
    sets: Seq[LabelSet] = Nil,
    usedLabels: Set[Label] = Set.empty,
    labelsAsFunctions: Map[String, Node.FunctionType] = Map.empty,
    stack: Stack = Stack.empty,
    localVars: Map[Int, TypedExpression] = Map.empty
  )
}
