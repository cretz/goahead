package goahead.compile
package postprocess

import goahead.ast.{Node, NodeWalker}
import goahead.compile.MethodCompiler._

trait AddFunctionVars extends PostProcessor {
  import Helpers._
  import AstDsl._

  override def apply(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Add all local vars not part of the args
    val argTypeCount = IType.getArgumentTypes(ctx.method.desc).length
    val (localVarsUsed, localVarsUnused) =
      ctx.localVars.allTimeVars.drop(argTypeCount).partition(ctx.localVars.isUsedVar)
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
