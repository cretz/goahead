package goahead.compile
package insn

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.{Handle, Opcodes, Type}
import org.objectweb.asm.tree.{InvokeDynamicInsnNode, MethodInsnNode}
import org.objectweb.asm.util.Printer

trait InvokeDynamicInsnCompiler extends Logger { self: MethodInsnCompiler =>
  import AstDsl._
  import MethodCompiler._
  import Helpers._

  def compile(ctx: Context, insn: InvokeDynamicInsnNode): (Context, Seq[Node.Statement]) = {
    pushBootstrappedCallSite(ctx, insn)
  }

  protected def pushBootstrappedCallSite(
    ctx: Context,
    insn: InvokeDynamicInsnNode
  ): (Context, Seq[Node.Statement]) = {
    // We run the one time call and then we reference the var
    val insnIndex = ctx.method.instructions.indexOf(insn)
    val syncVarName =
      ctx.mangler.invokeDynamicSyncVarName(ctx.cls.name, ctx.method.name, ctx.method.desc, insnIndex)
    val callSiteVarName =
      ctx.mangler.invokeDynamicCallSiteVarName(ctx.cls.name, ctx.method.name, ctx.method.desc, insnIndex)
    pushBootstrapCall(ctx, insn).stackPopped { case (ctx, bootstrapCall) =>
      val funcIface = IType.getReturnType(insn.desc)
      ctx.stackPushed(TypedExpression(callSiteVarName.toIdent, funcIface, cheapRef = true)).map { ctx =>
        ctx.staticInstRefExpr(funcIface.internalName).map { case (ctx, staticInst) =>
          ctx -> syncVarName.toIdent.sel("Do").call(Seq(
            funcType(Nil).toFuncLit(Seq(
              callSiteVarName.toIdent.assignExisting(
                staticInst.sel(ctx.mangler.funcInterfaceProxyCreateMethodName(funcIface.internalName)).
                  call(Seq(bootstrapCall.expr))
              )
            ))
          )).toStmt.singleSeq
        }
      }
    }
  }

  protected def pushBootstrapCall(ctx: Context, insn: InvokeDynamicInsnNode): Context = {
    pushAllBootstrapArgs(ctx, insn).map { ctx =>
      val methodOpcode = insn.bsm.getTag match {
        case Opcodes.H_INVOKESTATIC => Opcodes.INVOKESTATIC
        case Opcodes.H_NEWINVOKESPECIAL => Opcodes.INVOKESPECIAL
        case t => sys.error("Unsupported bootstrap tag: " + Printer.HANDLE_TAG(t))
      }
      // We know there are no stmts
      compile(
        ctx,
        new MethodInsnNode(Opcodes.INVOKESTATIC, insn.bsm.getOwner, insn.bsm.getName, insn.bsm.getDesc, false)
      )._1
    }
  }

  protected def pushAllBootstrapArgs(ctx: Context, insn: InvokeDynamicInsnNode): Context = {
    pushLookupClass(ctx).map(_.pushString(insn.name)).map(pushMethodType(_, insn.desc)).
      map(pushBootstrapMethodStaticArgs(_, insn.bsmArgs))
  }

  protected def pushLookupClass(ctx: Context): Context = {
    // We know this doesn't create statements
    compile(ctx, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodHandles",
      "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", false))._1
  }

  protected def pushMethodType(ctx: Context, desc: String): Context = {
    // We know this doesn't create statements
    compile(ctx.pushString(desc).stackPushed(NilTypedExpr),
      new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
        "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;",
        false))._1
  }

  protected def pushBootstrapMethodStaticArgs(ctx: Context, args: Array[AnyRef]): Context = {
    args.foldLeft(ctx) { case (ctx, arg) =>
      arg match {
        case v: java.lang.Integer => ctx.stackPushed(v.toInt.toTypedLit)
        case v: java.lang.Float => ctx.stackPushed(v.toFloat.toTypedLit)
        case v: java.lang.Long => ctx.stackPushed(v.toLong.toTypedLit)
        case v: java.lang.Double => ctx.stackPushed(v.toDouble.toTypedLit)
        case v: String => ctx.pushString(v)
        case v: Type => v.getSort match {
          case Type.OBJECT | Type.ARRAY =>
            ctx.typedTypeLit(IType(v)).map(_.stackPushed(_))
          case Type.METHOD =>
            pushMethodType(ctx, v.getDescriptor)
        }
        case v: Handle =>
          pushHandleToMethodHandle(ctx, v)
      }
    }
  }

  protected def pushHandleToMethodHandle(ctx: Context, h: Handle): Context = {
    h.getTag match {
      case Opcodes.H_GETFIELD =>
        // findGetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          ctx,
          "findGetter",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_PUTFIELD =>
        // findSetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          ctx,
          "findSetter",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_GETSTATIC =>
        // findStaticGetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          ctx,
          "findStaticGetter",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_PUTSTATIC =>
        // findStaticSetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          ctx,
          "findStaticSetter",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_INVOKEINTERFACE | Opcodes.H_INVOKEVIRTUAL =>
        // findVirtual(Class<?> refc, String name, MethodType type)
        pushLookupCall(
          ctx,
          "findVirtual",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(pushMethodType(_, h.getDesc))
        )
      case Opcodes.H_INVOKESTATIC =>
        // findStatic(Class<?> refc, String name, MethodType type)
        pushLookupCall(
          ctx,
          "findStatic",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(pushMethodType(_, h.getDesc))
        )
      case Opcodes.H_INVOKESPECIAL =>
        // findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller)
        pushLookupCall(
          ctx,
          "findSpecial",
          pushClassName(_, h.getOwner).map(_.pushString(h.getName)).map(pushMethodType(_, h.getDesc)).
            map(pushClassName(_, ctx.cls.name))
        )
      case Opcodes.H_NEWINVOKESPECIAL =>
        // findConstructor(Class<?> refc, MethodType type)
        pushLookupCall(
          ctx,
          "findConstructor",
          pushClassName(_, h.getOwner).map(pushMethodType(_, h.getDesc))
        )
    }
  }

  protected def pushClassName(ctx: Context, className: String): Context =
    ctx.pushTypeLit(IType.getObjectType(className))


  protected def pushLookupCall(ctx: Context, methodName: String, pushArgs: Context => Context): Context = {
    pushLookupClass(ctx).map(pushArgs).map { ctx =>
      val method = ctx.classPath.getFirstClass("java/lang/invoke/MethodHandles$Lookup").cls.
        methods.find(_.name == methodName).get
      println(s"CHECKING ${method.cls.name}.${method.name}${method.desc}")
      compile(ctx, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, method.cls.name, method.name, method.desc, false))._1
    }
  }
}
