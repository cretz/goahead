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
              val method = ctx.classPath.getFirstClass(insn.owner).cls.methods.
                find(m => m.name == insn.name && m.desc == insn.desc).
                getOrElse(sys.error(s"Unable to find static method ${insn.name}${insn.desc} on ${insn.owner}"))
              val methodExpr = staticInstExpr.sel(ctx.mangler.implMethodName(insn.name, insn.desc))
              invokeStmt(ctx, method, methodExpr, args, retType)
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
      if (insn.name != "<init>" && !insn.itf && insn.owner != ctx.cls.name)
        ctx.cls.parent.getOrElse(sys.error("Expected parent"))
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
    // If the start type is an array, we expect it to mean object
    val properStartType = if (resolutionStartType.startsWith("[")) "java/lang/Object" else resolutionStartType
    val method = resolveInstanceMethod(ctx, insn.name, insn.desc, properStartType)
    subject.toExprNode(ctx, IType.getObjectType(method.cls.name)).map { case (ctx, subjectExpr) =>
      if (method.name == "<init>" && !subject.isThis) {
        // If it's an init call, but not on "this", then we have to type assert since we don't
        // put init methods on inst ifaces. We check it's not "this" to prevent calling it on ourselves
        ctx.implTypeExpr(method.cls.name).map { case (ctx, implType) =>
          val m = subjectExpr.typeAssert(implType).sel(ctx.mangler.forwardMethodName(method.name, method.desc))
          invokeStmt(ctx, method, m, args, retType)
        }
      } else if (method.cls.access.isAccessInterface &&
          !method.access.isAccessStatic && !method.access.isAccessAbstract) {
        // For default interface impls, we invoke statically
        val defName = ctx.mangler.interfaceDefaultMethodName(method.cls.name, method.name, method.desc)
        ctx.importQualifiedName(method.cls.name, defName).map { case (ctx, defName) =>
          invokeStmt(ctx, method, defName, subjectExpr +: args, retType)
        }
      } else if (method.access.isAccessPrivate || (subject.isThis && ctx.cls.name != method.cls.name)) {
        // Private or calling ourselves but on a different class means we target
        // the method directly, no dispatch
        ctx.instToImpl(subjectExpr, method.cls.name).map { case (ctx, impl) =>
          invokeStmt(ctx, method, impl.sel(ctx.mangler.implMethodName(insn.name, insn.desc)), args, retType)
        }
      } else {
        invokeStmt(ctx, method, subjectExpr.sel(ctx.mangler.forwardMethodName(insn.name, insn.desc)), args, retType)
      }
    }
  }

  protected def resolveInstanceMethod(ctx: Context, name: String, desc: String, startAtType: String): Method = {
    // Iterator's ++ is lazy so we use that and then turn it to a stream for multiple use
    val allSuperAndImplementingTypes = (Iterator(ctx.classPath.getFirstClass(startAtType)) ++
      ctx.classPath.allSuperAndImplementingTypes(startAtType)).toStream
    val allSuperAndImplementingTypeMethods = allSuperAndImplementingTypes.flatMap(_.cls.methods)
    val possibleMethods = allSuperAndImplementingTypeMethods.filter(m =>
      m.name == name && (m.desc == desc || m.isSignaturePolymorphic)
    )
    // By going non-iface -> abstract -> any we are basically going:
    //   class -> iface abstract -> iface default impl
    @inline def findMethodNonIface = possibleMethods.find(m => !m.cls.access.isAccessInterface)
    @inline def findMethodAbstract = possibleMethods.find(_.access.isAccessAbstract)
    @inline def findMethod = possibleMethods.headOption
    findMethodNonIface.orElse(findMethodAbstract).orElse(findMethod).getOrElse {
      sys.error(s"Unable to resolve method $name$desc, tried classes: " +
        allSuperAndImplementingTypes.map(_.cls.name).mkString(", "))
    }
  }

  protected def invokeStmt(
    ctx: Context,
    method: Method,
    methodExpr: Node.Expression,
    args: Seq[Node.Expression],
    retType: IType
  ): (Context, Seq[Node.Statement]) = {
    val called = methodExpr.call(args)
    if (retType == IType.VoidType) ctx -> called.toStmt.singleSeq
    else ctx.stackPushed(TypedExpression(called, retType, cheapRef = false)) -> Nil
  }
}
