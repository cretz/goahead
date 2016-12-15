package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.tree.IincInsnNode

trait IincInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: IincInsnNode): (Context, Seq[Node.Statement]) = {
    ctx.getLocalVar(insn.`var`, IType.IntType, forWriting = false).map { case (ctx, localVar) =>
      localVar.toExprNode(ctx, IType.IntType).map { case (ctx, localVar) =>
        ctx -> localVar.binary(Node.Token.AddAssign, insn.incr.toLit).toStmt.singleSeq
      }
    }
  }
}
