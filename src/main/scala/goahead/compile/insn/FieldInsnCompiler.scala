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
          val declarer = ctx.imports.classPath.getFieldDeclarer(insn.owner, insn.name, static = false)
          ctx.stackPushed(
            TypedExpression(
              expr = item.expr.sel(ctx.mangler.fieldGetterName(declarer.cls.name, insn.name)).call(),
              typ = IType.getType(insn.desc),
              cheapRef = true
            )
          ) -> Nil
        }
      case Opcodes.GETSTATIC =>
        ctx.staticInstRefExpr(insn.owner).leftMap { case (ctx, expr) =>
          ctx.stackPushed(TypedExpression(
            expr = expr.sel(ctx.mangler.fieldName(insn.owner, insn.name)),
            typ = IType.getType(insn.desc),
            cheapRef = true
          )) -> Nil
        }
      case Opcodes.PUTFIELD =>
        ctx.stackPopped(2, { case (ctx, Seq(objectRef, value)) =>
          val declarer = ctx.imports.classPath.getFieldDeclarer(insn.owner, insn.name, static = false)
          value.toExprNode(ctx, IType.getType(insn.desc)).leftMap { case (ctx, value) =>
            ctx -> objectRef.expr.sel(ctx.mangler.fieldSetterName(declarer.cls.name, insn.name)).
              call(Seq(value)).toStmt.singleSeq
          }
        })
      case Opcodes.PUTSTATIC =>
        ctx.staticInstRefExpr(insn.owner).leftMap { case (ctx, expr) =>
          ctx.stackPopped { case (ctx, value) =>
            value.toExprNode(ctx, IType.getType(insn.desc)).leftMap { case (ctx, value) =>
              ctx -> expr.sel(ctx.mangler.fieldName(insn.owner, insn.name)).assignExisting(value).singleSeq
            }
          }
        }
    }
  }
}
