package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.tree.InvokeDynamicInsnNode

trait InvokeDynamicInsnCompiler {
  import AstDsl._
  import MethodCompiler._

  def compile(ctx: Context, insn: InvokeDynamicInsnNode): (Context, Seq[Node.Statement]) = {
    ctx -> "panic".toIdent.call(Seq("Invoke dynamic not yet supported".toLit)).toStmt.singleSeq
  }
}
