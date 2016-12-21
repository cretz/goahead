package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.TypeInsnNode

trait TypeInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: TypeInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.ANEWARRAY =>
        ctx.stackPopped { case (ctx, sizeTypeExpr) =>
          sizeTypeExpr.toExprNode(ctx, IType.IntType).map { case (ctx, sizeExpr) =>
            val jvmType = IType.getObjectType("java/lang/Object").asArray
            jvmType.arrayNewFn(ctx).map { case (ctx, arrayNewFn) =>
              ctx.stackPushed {
                TypedExpression(
                  arrayNewFn.call(Seq(sizeExpr)),
                  jvmType,
                  cheapRef = false
                )
              } -> Nil
            }
          }
        }
      case Opcodes.CHECKCAST =>
        // Check the stack entry is not null and not of a certain type
        ctx.stackPopped { case (ctx, item) =>
          // As with DUP, we need a temp var if it's not a cheap ref
          val ctxAndTypedExprWithAssignStmtOpt =
            if (item.cheapRef) ctx -> (item -> None)
            else {
              ctx.getTempVar(item.typ).map { case (ctx, tempVar) =>
                ctx -> (tempVar -> Some(tempVar.name.toIdent.assignExisting(item.expr)))
              }
            }
          ctxAndTypedExprWithAssignStmtOpt.map { case (ctx, (typedExpr, tempAssignStmtOpt)) =>
            ctx.typeToGoType(IType.getObjectType(insn.desc)).map { case (ctx, goType) =>
              ctx.importRuntimeQualifiedName("NewClassCastEx").map { case (ctx, classCastEx) =>
                // Create conditional first to make sure it's not nil, then inside to do the type check
                val ifStmt = iff(init = None, lhs = typedExpr.expr, op = Node.Token.Neq, rhs = NilExpr, body = Seq(iff(
                  init = Some(assignDefineMultiple(
                    left = Seq("_".toIdent, "castOk".toIdent),
                    right = typedExpr.expr.typeAssert(goType).singleSeq
                  )),
                  cond = "castOk".toIdent.unary(Node.Token.Not),
                  body = "panic".toIdent.call(Seq(classCastEx.call())).toStmt.singleSeq
                )))
                ctx.stackPushed(typedExpr) -> (tempAssignStmtOpt.toSeq :+ ifStmt)
              }
            }
          }
        }
      case Opcodes.INSTANCEOF =>
        // Check the stack entry is of a certain type
        ctx.stackPopped { case (ctx, item) =>
          // As with DUP, we need a temp var if it's not a cheap ref
          val ctxAndTypedExprWithAssignStmtOpt =
            if (item.cheapRef) ctx -> (item -> None)
            else {
              ctx.getTempVar(item.typ).map { case (ctx, tempVar) =>
                ctx -> (tempVar -> Some(tempVar.name.toIdent.assignExisting(item.expr)))
              }
            }
          ctxAndTypedExprWithAssignStmtOpt.map { case (ctx, (typedExpr, tempAssignStmtOpt)) =>
            // We need a temp bool to store the cast result
            ctx.getTempVar(IType.BooleanType).map { case (ctx, tempVar) =>
              ctx.typeToGoType(IType.getObjectType(insn.desc)).map { case (ctx, goType) =>
                val checkStmt = assignExistingMultiple(
                  left = Seq("_".toIdent, tempVar.expr),
                  right = typedExpr.expr.typeAssert(goType).singleSeq
                )
                ctx.stackPushed(tempVar) -> (tempAssignStmtOpt.toSeq :+ checkStmt)
              }
            }
          }
        }
      case Opcodes.NEW =>
        // Just create the struct and put the entire instantiation on the stack
        ctx.staticNewExpr(insn.desc).map { case (ctx, newExpr) =>
          ctx.stackPushed(
            TypedExpression(newExpr, IType.getObjectType(insn.desc), cheapRef = false)
          ) -> Nil
        }
    }
  }
}
