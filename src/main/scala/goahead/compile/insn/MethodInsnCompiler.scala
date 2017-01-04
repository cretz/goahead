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
        ctx.stackPopped(argTypes.length, { case (ctx, args) =>
          argExprs(ctx, args).map { case (ctx, args) =>
            resolveStaticMethodRef(ctx, insn.name, insn.desc, insn.owner).map { case (ctx, (method, methodExpr)) =>
              invokeStmt(ctx, method, methodExpr, args, retType)
            }
          }
        })
      case Opcodes.INVOKEINTERFACE | Opcodes.INVOKEVIRTUAL | Opcodes.INVOKESPECIAL =>
        ctx.stackPopped(argTypes.length + 1, { case (ctx, subject +: args) =>
          argExprs(ctx, args).map { case (ctx, args) =>
            val resolved =
              if (insn.getOpcode == Opcodes.INVOKESPECIAL)
                resolveSpecialMethodRef(ctx, insn.name, insn.desc, subject, insn.itf, insn.owner)
              else
                resolveInterfaceOrVirtualMethodRef(ctx, insn.name, insn.desc, subject, insn.owner)
            resolved.map {
              // Default methods include the subject in the call
              case (ctx, (method, methodExpr)) if method.isDefault =>
                subject.toExprNode(ctx, IType.getObjectType(method.cls.name)).map { case (ctx, subjectExpr) =>
                  invokeStmt(ctx, method, methodExpr, subjectExpr +: args, retType)
                }
              case (ctx, (method, methodExpr)) =>
                invokeStmt(ctx, method, methodExpr, args, retType)
            }
          }
        })
    }
  }

  protected def resolveStaticMethodRef(
    ctx: Context,
    name: String,
    desc: String,
    owner: String
  ): (Context, (Method, Node.Expression)) = {
    // The method can be on a parent class actually
    val method = resolveMethod(ctx, name, desc, owner, static = true)
    ctx.staticInstRefExpr(method.cls.name).map { case (ctx, staticInstExpr) =>
      ctx -> (method -> staticInstExpr.sel(ctx.mangler.implMethodName(method.name, method.desc)))
    }
  }

  protected def resolveInterfaceOrVirtualMethodRef(
    ctx: Context,
    name: String,
    desc: String,
    subject: TypedExpression,
    owner: String
  ): (Context, (Method, Node.Expression)) = {
    // TODO: we need the actual object ref type please, not just the typed expression version of
    // subject since it can be wrong when local vars are reused
    val typName = subject.typ.maybeMakeMoreSpecific(ctx.classPath, IType.getObjectType(owner)).internalName
    resolveInstanceMethodRef(ctx, name, desc, subject, typName, virtual = true)
  }

  protected def resolveSpecialMethodRef(
    ctx: Context,
    name: String,
    desc: String,
    subject: TypedExpression,
    itf: Boolean,
    owner: String
  ): (Context, (Method, Node.Expression)) = {
    val ownerName =
      if (name != "<init>" && !itf && owner != ctx.cls.name)
        ctx.cls.parent.getOrElse(sys.error("Expected parent"))
      else owner
    resolveInstanceMethodRef(ctx, name, desc, subject, ownerName, virtual = false)
  }

  protected def resolveInstanceMethodRef(
    ctx: Context,
    name: String,
    desc: String,
    subject: TypedExpression,
    resolutionStartType: String,
    virtual: Boolean
  ): (Context, (Method, Node.Expression)) = {
    // If the start type is an array, we expect it to mean object
    val properStartType = if (resolutionStartType.startsWith("[")) "java/lang/Object" else resolutionStartType
    val method = resolveMethod(ctx, name, desc, properStartType, static = false)
    // For default interface impls, we invoke statically
    if (method.isDefault) {
      val defName = ctx.mangler.interfaceDefaultMethodName(method.cls.name, method.name, method.desc)
      ctx.importQualifiedName(method.cls.name, defName).map { case (ctx, defName) =>
        ctx -> (method -> defName)
      }
    } else {
      // Virtual calls forward the method, otherwise do a direct impl reference
      val methodName =
        if (virtual) ctx.mangler.forwardMethodName(method.name, method.desc)
        else ctx.mangler.implMethodName(method.name, method.desc)
      // Interfaces are simple type assertions (unless "this" which is already a pointer),
      // whereas non ifaces fetch the pointer
      val ctxAndSubject =
        if (method.cls.access.isAccessInterface) {
          if (subject.isThis) ctx -> subject.expr
          else subject.toExprNode(ctx, IType.getObjectType(method.cls.name))
        } else ctx.instToImpl(subject, method.cls.name)

      ctxAndSubject.map { case (ctx, subject) => ctx -> (method -> subject.sel(methodName)) }
    }
  }

  protected def resolveMethod(
    ctx: Context,
    name: String,
    desc: String,
    startAtType: String,
    static: Boolean
  ): Method = {
    // Iterator's ++ is lazy so we use that and then turn it to a stream for multiple use
    val allSuperAndImplementingTypes = (Iterator(ctx.classPath.getFirstClass(startAtType)) ++
      ctx.classPath.allSuperAndImplementingTypes(startAtType)).toStream
    val allSuperAndImplementingTypeMethods = allSuperAndImplementingTypes.flatMap(_.cls.methods)
    val possibleMethods = allSuperAndImplementingTypeMethods.filter(m =>
      m.name == name && (m.desc == desc || m.isSignaturePolymorphic) && m.access.isAccessStatic == static
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
    if (retType == IType.VoidType) ctx -> called.toStmt.singleSeq else {
      // As a special case, sigpoly methods actually return something else despite the sig, so we cast
      val ctxAndRetExpr =
        if (!method.isSignaturePolymorphic) ctx -> TypedExpression(called, retType, cheapRef = false)
        else TypedExpression(called, IType.getReturnType(method.desc), cheapRef = false).toExprNode(ctx, retType).map {
          case (ctx, expr) => ctx -> TypedExpression(expr, retType, cheapRef = false)
        }
      ctxAndRetExpr.map { case (ctx, expr) => ctx.stackPushed(expr) -> Nil }
    }
  }
}