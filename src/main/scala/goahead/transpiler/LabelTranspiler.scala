package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.LabelNode

trait LabelTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: LabelNode): Seq[Node.Statement] = {
    ctx.pushStatementHandler { stmts =>
      if (stmts.isEmpty) Nil
      else stmts.headOption.map({ firstStmt =>
        ctx.popStatementHandler()
        Node.LabeledStatement(insn.getLabel.toString.toIdent, Some(firstStmt))
      }).toSeq ++ stmts.tail
    }
    Nil
  }
}
