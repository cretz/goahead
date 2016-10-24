package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree.TypeInsnNode

trait TypeInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: TypeInsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.NEW =>
        // No-op, we let the <init> do this
        Nil
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }
}
