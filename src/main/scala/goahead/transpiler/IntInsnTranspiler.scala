package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree.IntInsnNode
import Helpers._

trait IntInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: IntInsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.BIPUSH =>
        ctx.stack.push(insn.operand.toLit, Type.INT_TYPE, cheapRef = true)
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
    Nil
  }
}
