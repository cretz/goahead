package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.tree.MultiANewArrayInsnNode

trait MultiANewArrayInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: MultiANewArrayInsnNode): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped(insn.dims, { case (ctx, counts) =>
      // TODO: find a cheaper way to initialize multi-dim slices
      // What we do here, sadly, is make the first array of the expected size. Then we, using nested loops,
      // create the children if they are sized.

      val jvmType = IType.getObjectType(insn.desc)
      ctx.getTempVar(jvmType).map { case (ctx, tempVar) =>
        val ctxAndForStmtOpt = if (counts.size <= 1) ctx -> None else {
          createInnerSlice(ctx, tempVar.expr, counts.tail.map(_.expr), 0, jvmType).map { case (ctx, stmt) =>
            ctx -> Some(stmt)
          }
        }
        ctxAndForStmtOpt.map { case (ctx, forStmtOpt) =>
          jvmType.arrayNewFn(ctx).map { case (ctx, arrayNewFn) =>
            ctx.stackPushed(tempVar) ->
              (Seq(tempVar.expr.assignExisting(arrayNewFn.call(Seq(counts.head.expr)))) ++ forStmtOpt)
          }
        }
      }
    })
  }

  protected def createInnerSlice(
    ctx: Context,
    lhsPreIndex: Node.Expression,
    nextSizes: Seq[Node.Expression],
    depth: Int,
    parentType: IType
  ): (Context, Node.Statement) = {
    ctx.typeToGoType(parentType.elementType).map { case (ctx, arrType) =>
      parentType.elementType.arrayNewFn(ctx).map { case (ctx, arrayNewFn) =>
        val lhsAsserted = lhsPreIndex.typeAssert(arrType)
        val indexVar = s"i$depth".toIdent
        //val indexedVar = lhsPreIndex.indexed(indexVar)
        val indexedVar = lhsAsserted.sel("Get").call(Seq(indexVar))
        val newStmt = lhsAsserted.sel("Set").call(Seq(indexVar, arrayNewFn.call(Seq(nextSizes.head)))).toStmt
        // More?
        val ctxAndInnerStmts = if (nextSizes.size == 1) ctx -> Seq(newStmt) else {
          createInnerSlice(ctx, indexedVar, nextSizes.tail, depth + 1, parentType.elementType).map {
            case (ctx, stmt) => ctx -> Seq(newStmt, stmt)
          }
        }

        ctxAndInnerStmts.map { case (ctx, innerStmts) =>
          // Assemble the loop
          ctx -> lhsAsserted.sel("Raw").call().loopOver(
            key = Some(indexVar),
            value = None,
            stmts = innerStmts
          )
        }
      }
    }
  }
}
