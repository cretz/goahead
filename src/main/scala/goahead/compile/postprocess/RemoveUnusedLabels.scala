package goahead.compile
package postprocess

import goahead.Logger
import goahead.ast.Node
import goahead.ast.Node.Statement
import goahead.compile.MethodCompiler._

trait RemoveUnusedLabels extends PostProcessor with Logger {
  import AstDsl._

  override def apply(ctx: Context, stmts: Seq[Statement]): (Context, Seq[Statement]) = {
    ctx -> removeUnusedLabels(ctx, stmts)
  }

  protected def removeUnusedLabels(ctx: Context, stmts: Seq[Node.Statement]): Seq[Node.Statement] = {
    // This shouldn't blow up the stack too much, no need reworking to tailrec
    // We know the labeled statements are not in if statements, TODO: right?
    stmts.foldLeft(Seq.empty[Node.Statement]) { case (stmts, stmt) =>
      stmt match {
        case labelStmt @ Node.LabeledStatement(Node.Identifier(label), innerStmt) =>
          val newInnerStmts = removeUnusedLabels(ctx, Seq(innerStmt))
          if (!ctx.usedLabels.contains(label)) stmts ++ newInnerStmts else {
            require(newInnerStmts.length <= 1)
            stmts :+ labelStmt.copy(statement = newInnerStmts.headOption.getOrElse(block(Nil)))
          }
        case Node.BlockStatement(blockStmts) =>
          // If the block is empty, just remove it
          if (blockStmts.isEmpty) stmts else {
            // Only put a block around the statement if it's not already a single block
            removeUnusedLabels(ctx, blockStmts) match {
              case Seq(Node.BlockStatement(innerStmts)) => stmts ++ innerStmts
              case newStmts => stmts :+ block(newStmts)
            }
          }
        case Node.ExpressionStatement(expr) =>
          stmts :+ removeUnusedLabels(ctx, expr).toStmt
        case a: Node.AssignStatement =>
          stmts :+ a.copy(right = a.right.map(removeUnusedLabels(ctx, _)))
        case other =>
          stmts :+ other
      }
    }
  }

  protected def removeUnusedLabels(ctx: Context, expr: Node.Expression): Node.Expression = expr match {
    // Only handle these for now
    case c @ Node.CallExpression(fn: Node.FunctionLiteral, args) =>
      // Go ahead and test inside the function literal
      val newBlock = removeUnusedLabels(ctx, Seq(fn.body)) match {
        case Seq(b: Node.BlockStatement) => b
        case newStmts => block(newStmts)
      }
      fn.copy(body = newBlock).call(args)
    case other => other
  }
}

object RemoveUnusedLabels extends RemoveUnusedLabels