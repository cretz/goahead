package goahead.compile
package postprocess

import goahead.ast.{Node, NodeWalker}
import goahead.compile.MethodCompiler._

trait AddFunctionVars extends PostProcessor {
  import Helpers._
  import AstDsl._

  override def apply(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Add all local vars not part of the args...
    // We know that args are the first and are never overwritten, so we just filter out the ones
    // with the arg names
    val localVars = {
      val argNames = IType.getArgumentTypes(ctx.method.desc).indices.map("var" + _).toSet
      ctx.localVars.allTimeVars.filterNot(v => argNames.contains(v.name))
    }
    val (localVarsUsed, localVarsUnused) = localVars.partition(ctx.localVars.isUsedVar)
    val varsToDecl = ctx.functionVars ++ localVarsUsed
    val ctxAndStmts =
      if (varsToDecl.isEmpty) ctx -> stmts
      else ctx.createVarDecl(varsToDecl).map { case (ctx, declStmt) => ctx -> (declStmt +: stmts) }
    // We have to remove the name for all unused local vars
    if (localVarsUnused.isEmpty) ctxAndStmts else ctxAndStmts.map { case (ctx, stmts) =>
      val localVarNames = localVarsUnused.map(_.name).toSet
      ctx -> NodeWalker.flatMapAllNonRecursive(stmts) {
        case s @ Node.AssignStatement(Seq(Node.Identifier(name)), _, _) if localVarNames.contains(name) =>
          // If right is a NilExpr, we elide it to prevent "use of untyped nil"
          s.right match {
            case Seq(NilExpr) =>
              None
            case _ =>
              Some(s.copy(left = "_".toIdent.singleSeq))
          }
      }
    }
  }
}

object AddFunctionVars extends AddFunctionVars
