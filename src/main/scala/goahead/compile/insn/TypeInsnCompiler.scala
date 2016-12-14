package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.TypeInsnNode

trait TypeInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: TypeInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.ANEWARRAY =>
        ctx.stackPopped { case (ctx, sizeTypeExpr) =>
          sizeTypeExpr.toExprNode(ctx, IType.IntType).map { case (ctx, sizeExpr) =>
            val jvmType = IType.getObjectType("java/lang/Object").asArray
            jvmType.arrayNewFn(ctx).map { case (ctx, arrayNewFn) =>
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
      case Opcodes.NEW =>
        // Just create the struct and put the entire instantiation on the stack
        ctx.staticNewExpr(insn.desc).map { case (ctx, newExpr) =>
          ctx.stackPushed(
            TypedExpression(newExpr, IType.getObjectType(insn.desc), cheapRef = false)
          ) -> Nil
        }
    }
  }
}
