package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode

trait FieldInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: FieldInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.GETFIELD =>
        ctx.stackPopped { case (ctx, item) =>
          val field = ctx.classPath.findFieldOnDeclarer(insn.owner, insn.name, static = false)
          // If the field is private, we have to cast the instance to its impl
          val ctxAndSubject = if (!field.access.isAccessPrivate) ctx -> item.expr else ctx.instToImpl(item, insn.owner)
          ctxAndSubject.map { case (ctx, subject) =>
            ctx.stackPushed(
              TypedExpression(
                expr = subject.sel(ctx.mangler.fieldGetterName(field.cls.name, insn.name)).call(),
                typ = IType.getType(insn.desc),
                cheapRef = false
              )
            ) -> Nil
          }
        }
      case Opcodes.GETSTATIC =>
        ctx.staticInstRefExpr(insn.owner).map { case (ctx, expr) =>
          ctx.stackPushed(TypedExpression(
            expr = expr.sel(ctx.mangler.fieldName(insn.owner, insn.name)),
            typ = IType.getType(insn.desc),
            cheapRef = false
          )) -> Nil
        }
      case Opcodes.PUTFIELD =>
        ctx.stackPopped(2, { case (ctx, Seq(objectRef, value)) =>
          // If the field is private, we have to cast the instance to its impl
          val field = ctx.classPath.findFieldOnDeclarer(insn.owner, insn.name, static = false)
          // If the field is private, we have to cast the instance to its impl
          val ctxAndSubject =
            if (!field.access.isAccessPrivate) ctx -> objectRef.expr
            else ctx.instToImpl(objectRef, insn.owner)
          ctxAndSubject.map { case (ctx, subject) =>
            value.toExprNode(ctx, IType.getType(insn.desc)).map { case (ctx, value) =>
              ctx -> subject.sel(ctx.mangler.fieldSetterName(field.cls.name, insn.name)).
                call(Seq(value)).toStmt.singleSeq
            }
          }
        })
      case Opcodes.PUTSTATIC =>
        ctx.staticInstRefExpr(insn.owner).map { case (ctx, expr) =>
          ctx.stackPopped { case (ctx, value) =>
            value.toExprNode(ctx, IType.getType(insn.desc)).map { case (ctx, value) =>
              ctx -> expr.sel(ctx.mangler.fieldName(insn.owner, insn.name)).assignExisting(value).singleSeq
            }
          }
        }
    }
  }
}
