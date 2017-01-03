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
        // We are going to make a temp var of the new type. Then we are either going to assign nil to it or we are
        // going to do a type assertion.
        ctx.stackPopped { case (ctx, item) =>
          val typ = IType.getObjectType(insn.desc)
          ctx.getTempVar(typ).map { case (ctx, tempVar) =>
            ctx.typeToGoType(typ).map { case (ctx, goType) =>
              ctx.importRuntimeQualifiedName("NewClassCastEx").map { case (ctx, classCastEx) =>
                ctx.stackPushed(tempVar) -> (item.typ match {
                  case _: IType.Simple =>
                    // As a special case, "this" is a concrete pointer already
                    val toCheck =
                      if (item.isThis) {
                        // But the interface version needs to ask for the raw pointer first
                        val thisExpr =
                          if (!ctx.cls.access.isAccessInterface) item.expr
                          else item.expr.sel(ctx.mangler.instanceRawPointerMethodName("java/lang/Object")).call()
                        thisExpr.sel(ctx.mangler.forwardSelfMethodName()).call()
                      } else item.expr
                    iff(init = None, lhs = item.expr, op = Node.Token.Eql, rhs = NilExpr, body = Seq(
                      tempVar.expr.assignExisting(NilExpr)
                    )).els(iff(
                      init = Some(assignDefineMultiple(
                        left = Seq("casted".toIdent, "castOk".toIdent),
                        right = toCheck.typeAssert(goType).singleSeq
                      )),
                      cond = "castOk".toIdent.unary(Node.Token.Not),
                      body = "panic".toIdent.call(Seq(classCastEx.call())).toStmt.singleSeq
                    ).els(tempVar.expr.assignExisting("casted".toIdent))).singleSeq
                  case _ =>
                    // Not simple means do nothing
                    Nil
                })
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
                // As a special case, "this" is a concrete pointer already
                val toCheck =
                  if (typedExpr.isThis) typedExpr.expr.sel(ctx.mangler.forwardSelfMethodName()).call()
                  else typedExpr.expr
                val checkStmt = assignExistingMultiple(
                  left = Seq("_".toIdent, tempVar.expr),
                  right = toCheck.typeAssert(goType).singleSeq
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
