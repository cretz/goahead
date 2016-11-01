package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FrameNode

trait FrameTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: FrameNode): Seq[Node.Statement] = {
    insn.`type` match {
      case Opcodes.F_FULL =>
        // Full frame...
        // Make sure the current local variable set is what we already expect

        // * ignore what it says about local variables for now
        // * make temp vars for everything

    }
    Nil
  }
}
