package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.VarInsnNode

trait VarInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: VarInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode  {
      case Opcodes.ALOAD =>
        ctx.getLocalVar(insn.`var`, ObjectType).leftMap { case (ctx, local) =>
          ctx.stackPushed(local) -> Nil
        }
      case Opcodes.ASTORE =>
        ctx.stackPopped { case (ctx, value) =>
          ctx.getLocalVar(insn.`var`, value.typ).leftMap { case (ctx, local) =>
            value.toExprNode(ctx, local.typ).leftMap { case (ctx, value) =>
              ctx -> local.expr.assignExisting(value).singleSeq
            }
          }
        }
    }
  }
}
