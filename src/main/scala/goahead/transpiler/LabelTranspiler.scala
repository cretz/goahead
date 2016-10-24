package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.LabelNode

trait LabelTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: LabelNode): Seq[Node.Statement] = {
    ctx.wrapNextStatement = Some(stmt => Node.LabeledStatement(insn.getLabel.toString.toIdent, Some(stmt)))
    Nil
  }
}
