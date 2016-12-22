package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode

trait LdcInsnCompiler {
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: LdcInsnNode): (Context, Seq[Node.Statement]) = {
    // TODO: check Type for class lits and method types and handles
    insn.cst match {
      case v: java.lang.Double =>
        ctx.stackPushed(TypedExpression(v.toDouble.toLit, IType.DoubleType, cheapRef = true)) -> Nil
      case v: java.lang.Float =>
        ctx.stackPushed(TypedExpression(v.toFloat.toLit, IType.FloatType, cheapRef = true)) -> Nil
      case v: java.lang.Integer =>
        ctx.stackPushed(TypedExpression(v.toInt.toLit, IType.IntType, cheapRef = true)) -> Nil
      case v: java.lang.Long =>
        ctx.stackPushed(TypedExpression(v.toLong.toLit, IType.LongType, cheapRef = true)) -> Nil
      case v: String =>
        ctx.newString(v).map { case (ctx, str) =>
          ctx.stackPushed(TypedExpression(str, StringType, cheapRef = true)) -> Nil
        }
      case v: Type => v.getSort match {
        case Type.OBJECT | Type.ARRAY =>
          ctx.typedTypeLit(IType(v)).map(_.stackPushed(_) -> Nil)
        case _ =>
          sys.error(s"Unsupported LDC type: $v")
      }
      case cst =>
        sys.error(s"Unrecognized LDC type: $cst")
    }
  }
}
