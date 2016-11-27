package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.TypeInsnNode

trait TypeInsnCompiler {
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: TypeInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.NEW =>
        // Just create the struct and put the entire instantiation on the stack
        ctx.staticNewExpr(insn.desc).leftMap { case (ctx, newExpr) =>
          ctx.stackPushed(
            TypedExpression(newExpr, IType.getObjectType(insn.desc), cheapRef = false)
          ) -> Nil
        }
    }
  }
}
