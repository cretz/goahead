package goahead.compile

import goahead.ast.Node
import org.objectweb.asm.Type
import org.objectweb.asm.tree._

import scala.annotation.tailrec

trait MethodCompiler {
  // TODO: make things extensible by removing finals and making only the recursive stuff private
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(
    cls: ClassNode,
    method: Method,
    imports: Imports,
    mangler: Mangler
  ): (Imports, Node.FunctionDeclaration) = {

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
      Type.getArgumentTypes(ctx.method.desc).zipWithIndex.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
        case ((ctx, params), (argType, argIndex)) =>
          ctx.typeToGoType(argType).leftMap { case (ctx, typ) =>
            val name = "var" + (argIndex + indexAdd)
            ctx -> (params :+ (name -> typ))
          }
      }

    ctxWithParams.leftMap { case (ctx, params) =>
      val ctxWithResultTypOpt = Type.getReturnType(ctx.method.desc) match {
        case Type.VOID_TYPE => ctx -> None
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
  ) = Context(cls, method, imports, mangler, sets)

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
      recursiveCompileLabelSets(codedCtx.copy(sets = ctx.sets.tail), newStmtFn)
    }
  }

  protected def contextAndStatementsForFrame(ctx: Context): (Context, Seq[Node.Statement]) = {
    ctx.sets.head.newFrame match {
      case None =>
        ctx -> Nil
      case Some(frame) =>
        contextAndStatementsForFrameLocals(contextForFrameStack(ctx, frame), frame)
    }
  }

  protected def contextForFrameStack(ctx: Context, f: FrameNode): Context = {
    if (f.stack == null || f.stack.isEmpty) ctx.copy(stack = Stack.empty)
    else {
      import scala.collection.JavaConversions._
      val initial = ctx.copy(stack = Stack.empty) -> Seq.empty[(String, Node.Expression)]
      val ctxWithFuncParams = f.stack.zipWithIndex.foldLeft(initial) {
        case ((ctx, prevFuncParams), (stackItem, index)) =>

          val name = s"stack$index"

          TypedExpression.Type.fromFrameVarType(ctx.cls, stackItem) match {
            case exprType @ TypedExpression.Type.Simple(typ) =>
              ctx.typeToGoType(typ).leftMap { case (ctx, goType) =>

                ctx.copy(
                  stack = ctx.stack.push(TypedExpression(expr = name.toIdent, typ = exprType, cheapRef = true))
                ) -> (prevFuncParams :+ name -> goType)
              }
            case _ =>
              // TODO: what about these stack vars that are undefined and what not?
              ctx -> prevFuncParams
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

  protected def contextAndStatementsForFrameLocals(ctx: Context, f: FrameNode): (Context, Seq[Node.Statement]) = {
    import scala.collection.JavaConversions._
    // Filter out nulls which can happen with CHOP
    val acceptableLocals = Option(f.local).map(_.zipWithIndex.filterNot(_ == null)).getOrElse(Nil)
    acceptableLocals.foldLeft(ctx -> Seq.empty[Node.Statement]) { case ((ctx, stmts), (localVar, index)) =>
      val varNum = ctx.localVars.size + index
      TypedExpression.Type.fromFrameVarType(ctx.cls, localVar) match {
        case exprType @ TypedExpression.Type.Simple(typ) =>
          ctx.typeToGoType(typ).leftMap { case (ctx, goType) =>
            ctx.copy(
              localVars = ctx.localVars + (varNum -> TypedExpression(s"var$varNum".toIdent, exprType, cheapRef = true))
            ) -> (stmts :+ varDecl(s"var$varNum", goType).toStmt)
          }
        case _ =>
          // TODO: what about these local vars that are undefined and what not?
          ctx -> stmts
      }
    }
  }

  protected def compileLabelCode(ctx: Context): (Context, Seq[Node.Statement]) =
    insnCompiler.compile(ctx, ctx.sets.head.insns)

  protected def statementPostProcessors = Seq(
    removeUnusedLabels _,
    addTempVars _
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
            else ctx -> (stmts :+ labelStmt.copy(statement = newInnerStmts.head))
          }
        case Node.BlockStatement(blockStmts) =>
          removeUnusedLabels(ctx, blockStmts).leftMap { case (ctx, newStmts) =>
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
    if (ctx.stack.tempVars.isEmpty) ctx -> stmts
    else {
      val ctxAndNamedTypes = ctx.stack.tempVars.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
        case ((ctx, prevNamedTypes), tempVar) =>
          ctx.typeToGoType(tempVar.typ).leftMap { case (ctx, typ) =>
            ctx -> (prevNamedTypes :+ (tempVar.name -> typ))
          }
      }
      // Prepend the temp vars
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
    localVars: Map[Int, TypedExpression] = Map.empty
  ) extends Contextual[Context] {
    override def updatedImports(mports: Imports) = copy(imports = mports)
  }
}
