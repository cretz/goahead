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

    def argExprs(ctx: Context, args: Seq[TypedExpression]): (Context, Seq[Node.Expression]) = {
      require(args.length == argTypes.length, "Length mismatch between caller and target")
      args.zip(argTypes).foldLeft(ctx -> Seq.empty[Node.Expression]) {
        case ((ctx, prevExprs), (typedExpr, typ)) =>
          typedExpr.toExprNode(ctx, typ).map { case (ctx, newExpr) =>
            ctx -> (prevExprs :+ newExpr)
          }
      }
    }

    insn.byOpcode {
      case Opcodes.INVOKESTATIC =>
        ctx.staticInstRefExpr(insn.owner).map { case (ctx, staticInstExpr) =>
          ctx.stackPopped(argTypes.length, { case (ctx, args) =>
            argExprs(ctx, args).map { case (ctx, args) =>
              invoke(ctx, staticInstExpr.sel(ctx.mangler.implMethodName(insn.name, insn.desc)), args, retType)
            }
          })
        }
      case Opcodes.INVOKEINTERFACE | Opcodes.INVOKESPECIAL | Opcodes.INVOKEVIRTUAL =>
        // Note: we let null pointer exceptions cause a panic instead of checking them
        ctx.stackPopped(argTypes.length + 1, { case (ctx, subject +: args) =>
          argExprs(ctx, args).map { case (ctx, args) =>
            if (insn.getOpcode == Opcodes.INVOKESPECIAL) invokeSpecial(ctx, insn, subject, args, retType)
            else invokeInterfaceOrVirtual(ctx, insn, subject, args, retType)
          }
        })
    }
  }

  protected def invokeInterfaceOrVirtual(
    ctx: Context,
    insn: MethodInsnNode,
    subject: TypedExpression,
    args: Seq[Node.Expression],
    retType: IType
  ): (Context, Seq[Node.Statement]) = {
    // TODO: we need the actual object ref type please, not just the typed expression version of
    // subject since it can be wrong when local vars are reused
    val typName = subject.typ.maybeMakeMoreSpecific(ctx.classPath, IType.getObjectType(insn.owner)).internalName
    invokeInstance(ctx, insn, subject, args, retType, typName)
  }

  protected def invokeSpecial(
    ctx: Context,
    insn: MethodInsnNode,
    subject: TypedExpression,
    args: Seq[Node.Expression],
    retType: IType
  ): (Context, Seq[Node.Statement]) = {
    val ownerName =
      if (insn.name != "<init>" && !insn.itf) ctx.cls.parent.getOrElse(sys.error("Expected parent"))
      else insn.owner
    invokeInstance(ctx, insn, subject, args, retType, ownerName)
  }

  protected def invokeInstance(
    ctx: Context,
    insn: MethodInsnNode,
    subject: TypedExpression,
    args: Seq[Node.Expression],
    retType: IType,
    resolutionStartType: String
  ): (Context, Seq[Node.Statement]) = {
    // Find the method to invoke
    def findMethodFromParent(name: String): Option[Method] = {
      val cls = ctx.classPath.getFirstClass(name).cls
      cls.methods.find(m => m.name == insn.name && m.desc == insn.desc).
        orElse(cls.parent.flatMap(findMethodFromParent))
    }
    val method = findMethodFromParent(resolutionStartType).getOrElse {
      // Find any non-static, non-abstract impl in any interface...we trust javac didn't let them compile
      // one w/ ambiguity
      ctx.classPath.allSuperAndImplementingTypes(resolutionStartType).view.
        filter(_.cls.access.isAccessInterface).flatMap(_.cls.methods).
        find({ m =>
          m.name == insn.name && m.desc == insn.desc && !m.access.isAccessStatic && !m.access.isAccessAbstract
        }).
        getOrElse(sys.error(s"Unable to resolve method ${insn.name}${insn.desc}"))
    }

    subject.toExprNode(ctx, IType.getObjectType(method.cls.name)).map { case (ctx, subjectExpr) =>
      if (method.name == "<init>" && !subject.isThis) {
        // If it's an init call, but not on "this", then we have to type assert since we don't
        // put init methods on inst ifaces. We check it's not "this" to prevent calling it on ourselves
        ctx.implTypeExpr(method.cls.name).map { case (ctx, implType) =>
          val m = subjectExpr.typeAssert(implType).sel(ctx.mangler.forwardMethodName(method.name, method.desc))
          invoke(ctx, m, args, retType)
        }
      } else if (method.cls.access.isAccessInterface &&
          !method.access.isAccessStatic && !method.access.isAccessAbstract) {
        // For default interface impls, we invoke statically
        val defName = ctx.mangler.interfaceDefaultMethodName(method.cls.name, method.name, method.desc)
        ctx.importQualifiedName(method.cls.name, defName).map { case (ctx, defName) =>
          invoke(ctx, defName, subjectExpr +: args, retType)
        }
//      } else if (subject.isThis && ctx.method.name == insn.name && ctx.cls.name != method.cls.name &&
//          (ctx.method.name == "<init>" || ctx.method.desc == insn.desc)) {
      } else if (subject.isThis && ctx.cls.name != method.cls.name) {
        // If we are calling on ourselves and inside the same method, we do it on the specific impl
        val m = subjectExpr.sel(ctx.mangler.implObjectName(method.cls.name)).sel(
          ctx.mangler.implMethodName(insn.name, insn.desc)
        )
        invoke(ctx, m, args, retType)
      } else {
        invoke(ctx, subjectExpr.sel(ctx.mangler.forwardMethodName(insn.name, insn.desc)), args, retType)
      }
    }
  }

  protected def invoke(
    ctx: Context,
    method: Node.Expression,
    args: Seq[Node.Expression],
    retType: IType
  ): (Context, Seq[Node.Statement]) = {
    val called = method.call(args)
    if (retType == IType.VoidType) ctx -> called.toStmt.singleSeq
    else ctx.stackPushed(TypedExpression(called, retType, cheapRef = false)) -> Nil
  }
}
