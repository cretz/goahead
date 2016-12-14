package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

trait MethodInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: MethodInsnNode): (Context, Seq[Node.Statement]) = {
    val (argTypes, retType) = IType.getArgumentAndReturnTypes(insn.desc)

    // As a special case, if we call a super version of a method in ourselves, we do not use
    // the dispatcher and we call it on that instance
    def subjectMethod(ctx: Context, subject: TypedExpression): (Context, Node.Expression) = {
      // Doesn't need to be the same desc if it's an init calling an init
      val isThis = subject.isThis
      val isSuperCall = isThis && ctx.method.name == insn.name &&
        (insn.name == "<init>" || ctx.method.desc == insn.desc) && insn.owner != ctx.cls.name
      subject.toExprNode(ctx, IType.getObjectType(insn.owner), noCasting = isSuperCall).map { case (ctx, subject) =>
        if (!isSuperCall && insn.name == "<init>" && !isThis) {
          // Init methods are not on the inst interface, so we type assert first
          // (the isThis check is because we can call init on ourselves)
          ctx.implTypeExpr(insn.owner).map { case (ctx, implType) =>
            ctx -> subject.typeAssert(implType).sel(ctx.mangler.forwardMethodName(insn.name, insn.desc))
          }
        } else if (!isSuperCall) {
          ctx -> subject.sel(ctx.mangler.forwardMethodName(insn.name, insn.desc))
        } else {
          ctx -> subject.sel(ctx.mangler.implObjectName(insn.owner)).sel(
            ctx.mangler.implMethodName(insn.name, insn.desc)
          )
        }
      }
    }

    def argExprs(ctx: Context, args: Seq[TypedExpression]): (Context, Seq[Node.Expression]) = {
      require(args.length == argTypes.length, "Length mismatch between caller and target")
      args.zip(argTypes).foldLeft(ctx -> Seq.empty[Node.Expression]) {
        case ((ctx, prevExprs), (typedExpr, typ)) =>
          typedExpr.toExprNode(ctx, typ).map { case (ctx, newExpr) =>
            ctx -> (prevExprs :+ newExpr)
          }
      }
    }

    def invoke(ctx: Context, method: Node.Expression, args: Seq[TypedExpression]): (Context, Seq[Node.Statement]) = {
      argExprs(ctx, args).map { case (ctx, args) =>
        val call = method.call(args)
        // Put it on the stack if not void
        if (retType == IType.VoidType) ctx -> call.toStmt.singleSeq
        else ctx.stackPushed(TypedExpression(call, retType, cheapRef = false)) -> Nil
      }
    }

    insn.byOpcode {
      case Opcodes.INVOKESTATIC =>
        ctx.staticInstRefExpr(insn.owner).map { case (ctx, staticInstExpr) =>
          ctx.stackPopped(argTypes.length, { case (ctx, args) =>
            invoke(ctx, staticInstExpr.sel(ctx.mangler.implMethodName(insn.name, insn.desc)), args)
          })
        }
      case Opcodes.INVOKEINTERFACE | Opcodes.INVOKESPECIAL | Opcodes.INVOKEVIRTUAL =>
        // Note: we let null pointer exceptions cause a panic instead of checking them
        ctx.stackPopped(argTypes.length + 1, { case (ctx, subject +: args) =>
          subjectMethod(ctx, subject).map { case (ctx, subjectMethod) =>
            invoke(ctx, subjectMethod, args)
          }
        })
    }
  }
}
