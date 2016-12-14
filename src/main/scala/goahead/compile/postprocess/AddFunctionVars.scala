package goahead.compile
package postprocess

import goahead.ast.Node
import goahead.compile.MethodCompiler._

trait AddFunctionVars extends PostProcessor {
  import Helpers._

  override def apply(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Add all function vars not part of the arg list
    val argTypeCount = IType.getArgumentTypes(ctx.method.desc).length
    require(ctx.functionVars.size >= argTypeCount)
    val varsToDecl = ctx.functionVars.drop(argTypeCount)
    if (varsToDecl.isEmpty) ctx -> stmts
    else ctx.createVarDecl(varsToDecl).map { case (ctx, declStmt) => ctx -> (declStmt +: stmts) }
  }
}

object AddFunctionVars extends AddFunctionVars
