package goahead.compile
package postprocess

import goahead.Logger
import goahead.ast.Node._
import goahead.compile.MethodCompiler._

trait ReduceCodeSize extends PostProcessor with Logger {

  override def apply(ctx: Context, stmts: Seq[Statement]): (Context, Seq[Statement]) = {
    // We're only running this on top-level statements, not walking
    // Basically we can remove all non-label-multi block statements
    ctx -> stmts.flatMap {
      // Labeled statements w/ block statements of a single statement (or a comment +
      // statement) can have the block removed
      case l @ LabeledStatement(_, s: BlockStatement) => maybeReduceBlock(ctx, s) match {
        case Some(Seq(stmt)) => Seq(l.copy(statement = stmt))
        case Some(Seq(comment: CommentStatement, stmt)) => Seq(comment, l.copy(statement = stmt))
        case Some(stmts) if stmts.size > 1 => l.copy(statement = stmts.head) +: stmts.tail
        case _ => Seq(l)
      }
      case b: BlockStatement => maybeReduceBlock(ctx, b).getOrElse(Seq(b))
      case other => Seq(other)
    }
  }

  protected def maybeReduceBlock(ctx: Context, s: BlockStatement): Option[Seq[Statement]] = {
    // If there are no local var decls, we can reduce it
    val hasVarDecl = s.statements.exists {
      case DeclarationStatement(_: GenericDeclaration) => true
      case _ => false
    }
    if (hasVarDecl) None else Some(s.statements)
  }
}

object ReduceCodeSize extends ReduceCodeSize
