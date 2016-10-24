package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.tree.LineNumberNode

trait LineNumberTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: LineNumberNode): Seq[Node.Statement] = {
    Seq.empty
  }
}
