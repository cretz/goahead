package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.tree.FrameNode

trait FrameTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: FrameNode): Seq[Node.Statement] = {
    Nil
  }
}
