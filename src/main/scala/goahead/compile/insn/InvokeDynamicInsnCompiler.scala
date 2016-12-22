package goahead.compile
package insn

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.{Handle, Opcodes, Type}
import org.objectweb.asm.tree.{InvokeDynamicInsnNode, MethodInsnNode}

trait InvokeDynamicInsnCompiler extends Logger { self: MethodInsnCompiler =>
  import AstDsl._
  import MethodCompiler._
  import Helpers._

  def compile(ctx: Context, insn: InvokeDynamicInsnNode): (Context, Seq[Node.Statement]) = {
    logger.info("Got invoke dynamic:\n" +
      s" Name: ${insn.name}\n" +
      s" Desc: ${insn.desc}\n" +
      s" Bootstrap: ${insn.bsm}\n" +
      s" Bootstrap Args: \n" + insn.bsmArgs.map(a => s"   $a (of type ${a.getClass})").mkString("\n")
    )

    // TODO: Get callsite
    val ctcAndCallSiteCall = insn.bsm.getTag match {
      case Opcodes.H_INVOKESTATIC =>
        pushLookupClass(ctx).map(_.pushString(insn.name)).map(pushMethodType(_, insn.desc))
      case _ => sys.error(s"Unsupported dynamic tag: ${insn.bsm.getTag}")
    }

    val retTyp = IType.getReturnType(insn.desc)
    val resultCtx =
      if (retTyp == IType.VoidType) ctx
      else ctx.getTempVar(retTyp).map(_.stackPushed(_))
    resultCtx -> "panic".toIdent.call(Seq("Invoke dynamic not yet supported".toLit)).toStmt.singleSeq
  }

  def pushLookupClass(ctx: Context): Context = {
    // We know this doesn't create statements
    compile(ctx, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodHandles",
      "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", false))._1
  }

  def pushMethodType(ctx: Context, desc: String): Context = {
    // We know this doesn't create statements
    compile(ctx.pushString(desc).stackPushed(NilTypedExpr),
      new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
        "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;",
        false))._1
  }

  def pushBootstrapMethodStaticArgs(ctx: Context, args: Array[Object]): Context = {
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

  def pushHandleToMethodHandle(ctx: Context, h: Handle): Context = {
    h.getTag match {
      case Opcodes.H_GETFIELD =>
        // findGetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          "findGetter",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_PUTFIELD =>
        // findSetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          "findSetter",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_GETSTATIC =>
        // findStaticGetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          "findStaticGetter",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_PUTSTATIC =>
        // findStaticSetter(Class<?> refc, String name, Class<?> type)
        pushLookupCall(
          "findStaticSetter",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(_.pushTypeLit(IType.getType(h.getDesc)))
        )
      case Opcodes.H_INVOKEINTERFACE | Opcodes.H_INVOKEVIRTUAL =>
        // findVirtual(Class<?> refc, String name, MethodType type)
        pushLookupCall(
          "findVirtual",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(pushMethodType(_, h.getDesc))
        )
      case Opcodes.H_INVOKESTATIC =>
        // findStatic(Class<?> refc, String name, MethodType type)
        pushLookupCall(
          "findStatic",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(pushMethodType(_, h.getDesc))
        )
      case Opcodes.H_INVOKESPECIAL =>
        // findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller)
        pushLookupCall(
          "findSpecial",
          pushClassName(ctx, h.getOwner).map(_.pushString(h.getName)).map(pushMethodType(_, h.getDesc)).
            map(pushClassName(_, ctx.cls.name))
        )
      case Opcodes.H_NEWINVOKESPECIAL =>
        // findConstructor(Class<?> refc, MethodType type)
        pushLookupCall(
          "findConstructor",
          pushClassName(ctx, h.getOwner).map(pushMethodType(_, h.getDesc))
        )
    }
  }

  def pushClassName(ctx: Context, className: String): Context = ctx.pushTypeLit(IType.getObjectType(className))


  def pushLookupCall(methodName: String, ctxWithArgs: Context): Context = {
    pushLookupClass(ctxWithArgs).stackPopped { case (ctx, lookup) =>
      val method = ctx.classPath.getFirstClass("java/lang/invoke/MethodHandles$Lookup").cls.
        methods.find(_.name == methodName).get
      compile(ctx, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, method.cls.name,
        method.name, method.desc, false))._1
    }
  }
}
