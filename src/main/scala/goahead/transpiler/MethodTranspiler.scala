package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.tree._

import scala.collection.JavaConversions._
import goahead.transpiler.Helpers._
import org.objectweb.asm.Type

import scala.annotation.tailrec

trait MethodTranspiler extends
  FieldInsnTranspiler
  with FrameTranspiler
  with IntInsnTranspiler
  with JumpInsnTranspiler
  with LabelTranspiler
  with LdcInsnTranspiler
  with LineNumberTranspiler
  with MethodInsnTranspiler
  with ZeroOpInsnTranspiler
{

  import MethodTranspiler._

  def transpile(classCtx: ClassTranspiler.Context, methodNode: MethodNode): Node.FunctionDeclaration =
    transpile(Context(classCtx, methodNode))

  def transpile(ctx: Context): Node.FunctionDeclaration = {
    Node.FunctionDeclaration(
      receivers = Seq(
        Node.Field(
          names = Seq("this".toIdent),
          typ = Node.StarExpression(
            ctx.classCtx.classRefExpr(ctx.classCtx.classNode.name, ctx.methodNode.access.isAccessStatic)
          )
        )
      ),
      name = goMethodName(ctx.methodNode.name, ctx.methodType).toIdent,
      typ = ctx.classCtx.methodToFunctionType(ctx.methodType.getReturnType, ctx.methodType.getArgumentTypes),
      body = Some(Node.BlockStatement(transpileInsns(ctx)))
    )
  }

  def transpileInsns(ctx: Context): Seq[Node.Statement] = {
    val stmts = ctx.methodNode.instructionIter.toSeq.flatMap { insn =>
      var stmts = insn match {
        case i: FieldInsnNode => transpile(ctx, i)
        case i: FrameNode => transpile(ctx, i)
        case i: InsnNode => transpile(ctx, i)
        case i: IntInsnNode => transpile(ctx, i)
        case i: JumpInsnNode => transpile(ctx, i)
        case i: LabelNode => transpile(ctx, i)
        case i: LdcInsnNode => transpile(ctx, i)
        case i: LineNumberNode => transpile(ctx, i)
        case i: MethodInsnNode => transpile(ctx, i)
        case i => sys.error(s"Unknown instruction: $i")
      }
      stmts.headOption.zip(ctx.wrapNextStatement).headOption.foreach { case (headStmt, fn) =>
        stmts = fn(headStmt) +: stmts.tail
        ctx.wrapNextStatement = None
      }
      stmts
    }

    removeUnusedLabelsFromStmts(ctx, stmts)
  }

  private[this] def removeUnusedLabelsFromStmts(ctx: Context, stmts: Seq[Node.Statement]): Seq[Node.Statement] = {
    stmts.map {
      case stmt @ Node.LabeledStatement(label, Some(innerStmt)) =>
        if (!ctx.usedLabels.contains(label.name)) innerStmt
        else stmt.copy(statement = removeUnusedLabelsFromStmts(ctx, Seq(innerStmt)).headOption)
      case stmt: Node.BlockStatement =>
        stmt.copy(removeUnusedLabelsFromStmts(ctx, stmt.statements))
      case other =>
        other
    }
  }
}

object MethodTranspiler extends MethodTranspiler {
  case class Context(
    classCtx: ClassTranspiler.Context,
    methodNode: MethodNode,
    stack: MutableMethodStack = new MutableMethodStack()
  ) {
    // TODO: move this mutable stuff out
    var wrapNextStatement = Option.empty[Node.Statement => Node.Statement]
    var usedLabels = Set.empty[String]

    lazy val methodType = Type.getMethodType(methodNode.desc)

    def nullPointerAssertion(expr: Node.Expression): Node.Statement = {
      Node.IfStatement(
        condition = Node.BinaryExpression(
          left = expr,
          operator = Node.Token.Eql,
          right = NilExpr
        ),
        body = Node.BlockStatement(Seq(throwError(
          Node.CallExpression(
            classCtx.constructorRefExpr("java/lang/NullPointerException", Type.getMethodType(Type.VOID_TYPE)),
            Seq.empty
          )
        )))
      )
    }

    def throwError(expr: Node.Expression): Node.Statement = {
      Node.ExpressionStatement(Node.CallExpression("panic".toIdent, Seq(expr)))
    }

    def zeroOfType(typ: Type): Node.Expression = exprToType(0.toLit, Type.INT_TYPE, typ)

    def exprToType(expr: Node.Expression, oldType: Type, typ: Type): Node.Expression = {
      oldType.getSort -> typ.getSort match {
        case (Type.INT, Type.BOOLEAN) => expr match {
          case Node.BasicLiteral(Node.Token.Int, "1") => "true".toIdent
          case Node.BasicLiteral(Node.Token.Int, "0") => "false".toIdent
          case _ => sys.error(s"Unable to change int $expr to boolean")
        }
        case (oldSort, newSort) if oldSort == newSort =>
          expr
        case _ =>
          sys.error(s"Unable to convert from type $oldType to $typ")
      }
    }
  }
}
