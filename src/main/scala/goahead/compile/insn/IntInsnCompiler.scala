package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.IntInsnNode

trait IntInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: IntInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.BIPUSH | Opcodes.SIPUSH =>
        ctx.stackPushed(insn.operand.toTypedLit) -> Nil
      case Opcodes.NEWARRAY =>
        ctx.stackPopped { case (ctx, sizeTypeExpr) =>
          sizeTypeExpr.toExprNode(ctx, IType.IntType).map { case (ctx, sizeExpr) =>
            // Create new slice and push on the stack
            val jvmType = insn.operand match {
              case Opcodes.T_BOOLEAN => IType.BooleanType.asArray
              case Opcodes.T_CHAR => IType.CharType.asArray
              case Opcodes.T_FLOAT => IType.FloatType.asArray
              case Opcodes.T_DOUBLE => IType.DoubleType.asArray
              case Opcodes.T_BYTE => IType.ByteType.asArray
              case Opcodes.T_SHORT => IType.ShortType.asArray
              case Opcodes.T_INT => IType.IntType.asArray
              case Opcodes.T_LONG => IType.LongType.asArray
              case other => sys.error(s"Unrecognized operand: $other")
            }
            jvmType.arrayNewFn(ctx).map { case (ctx, arrayNewFn) =>
              ctx.typeToGoType(jvmType).map { case (ctx, goType) =>
                ctx.stackPushed {
                  TypedExpression(
                    arrayNewFn.call(Seq(sizeExpr)),
                    jvmType,
                    cheapRef = false
                  )
                } -> Nil
              }
            }
          }
        }
    }
  }
}
