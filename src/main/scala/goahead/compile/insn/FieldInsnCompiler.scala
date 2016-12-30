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
          instanceFieldAccessorRef(ctx, insn.name, insn.desc, insn.owner, item, getter = true).map {
            case (ctx, ref) =>
              ctx.stackPushed(
                TypedExpression(
                  expr = ref.call(),
                  typ = IType.getType(insn.desc),
                  cheapRef = false
                )
              ) -> Nil
          }
        }
      case Opcodes.GETSTATIC =>
        staticFieldRef(ctx, insn.name, insn.desc, insn.owner).map { case (ctx, ref) =>
          ctx.stackPushed(TypedExpression(
            expr = ref,
            typ = IType.getType(insn.desc),
            cheapRef = false
          )) -> Nil
        }
      case Opcodes.PUTFIELD =>
        ctx.stackPopped(2, { case (ctx, Seq(objectRef, value)) =>
          instanceFieldAccessorRef(ctx, insn.name, insn.desc, insn.owner, objectRef, getter = false).map {
            case (ctx, ref) =>
              value.toExprNode(ctx, IType.getType(insn.desc)).map { case (ctx, value) =>
                ctx -> ref.call(Seq(value)).toStmt.singleSeq
              }
          }
        })
      case Opcodes.PUTSTATIC =>
        staticFieldRef(ctx, insn.name, insn.desc, insn.owner).map { case (ctx, ref) =>
          ctx.stackPopped { case (ctx, value) =>
            value.toExprNode(ctx, IType.getType(insn.desc)).map { case (ctx, value) =>
              ctx -> ref.assignExisting(value).singleSeq
            }
          }
        }
    }
  }

  protected def instanceFieldAccessorRef(
    ctx: Context,
    name: String,
    desc: String,
    owner: String,
    subject: TypedExpression,
    getter: Boolean
  ): (Context, Node.Expression) = {
    val field = ctx.classPath.findFieldOnDeclarer(owner, name, static = false)
    // If the field is private, we have to cast the instance to its impl
    val ctxAndSubject =
      if (!field.access.isAccessPrivate) subject.toExprNode(ctx, IType.getObjectType(field.cls.name))
      else ctx.instToImpl(subject, owner)
    ctxAndSubject.map { case (ctx, subject) =>
      if (getter) ctx -> subject.sel(ctx.mangler.fieldGetterName(field.cls.name, name))
      else ctx -> subject.sel(ctx.mangler.fieldSetterName(field.cls.name, name))
    }
  }

  protected def staticFieldRef(
    ctx: Context,
    name: String,
    desc: String,
    owner: String
  ): (Context, Node.Expression) = {
    val field = ctx.classPath.findFieldOnDeclarer(owner, name, static = true)
    ctx.staticInstRefExpr(field.cls.name).map { case (ctx, expr) =>
      ctx -> expr.sel(ctx.mangler.fieldName(field.cls.name, name))
    }
  }
}
