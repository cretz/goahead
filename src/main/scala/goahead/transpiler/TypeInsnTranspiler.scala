package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree.TypeInsnNode

trait TypeInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: TypeInsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.NEW =>
        // Just create the struct and put the entire instantiation on the stack
        ctx.stack.push(ctx.classCtx.staticNewExpr(insn.desc), Type.getObjectType(insn.desc), cheapRef = false)
        Nil
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }
}
