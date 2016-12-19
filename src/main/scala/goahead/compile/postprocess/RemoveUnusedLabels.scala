package goahead.compile
package postprocess

import goahead.Logger
import goahead.ast.{Node, NodeWalker}
import goahead.ast.Node.Statement
import goahead.compile.MethodCompiler._

trait RemoveUnusedLabels extends PostProcessor with Logger {

  override def apply(ctx: Context, stmts: Seq[Statement]): (Context, Seq[Statement]) = {
    // We trust there are no unused labels inside of labels
    ctx -> NodeWalker.flatMapAllNonRecursive(stmts) {
      case l: Node.LabeledStatement if !ctx.usedLabels.contains(l.label.name) =>
        l.statement match {
          case Node.BlockStatement(Seq()) | Node.EmptyStatement => None
          case stmt => Some(stmt)
        }
    }
  }
}

object RemoveUnusedLabels extends RemoveUnusedLabels