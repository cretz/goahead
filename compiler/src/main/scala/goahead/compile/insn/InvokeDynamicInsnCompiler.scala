package goahead.compile
package insn

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.tree.{InvokeDynamicInsnNode, MethodInsnNode}
import org.objectweb.asm.util.Printer
import org.objectweb.asm.{Handle, Opcodes, Type}

trait InvokeDynamicInsnCompiler extends Logger { self: MethodInsnCompiler with FieldInsnCompiler =>
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: InvokeDynamicInsnNode): (Context, Seq[Node.Statement]) = {
    if (ctx.isOptimizable(insn)) pushDirectProxy(ctx, insn)
    else pushInvokedCallSite(ctx, insn)
  }

  protected def pushDirectProxy(ctx: Context, insn: InvokeDynamicInsnNode): (Context, Seq[Node.Statement]) = {
    val handle = insn.bsmArgs(1).asInstanceOf[Handle]
    val ifaceTyp = IType.getReturnType(insn.desc)
    val ifaceMethod = methodSetManager.functionalInterfaceMethod(
      ctx.classPath,
      ctx.classPath.getFirstClass(ifaceTyp.internalName).cls
    ).getOrElse(sys.error(s"Expected ${ifaceTyp.internalName} to be a functional interface as target of dyn insn"))
    val allHandleArgTypes = IType.getArgumentTypes(handle.getDesc)
    val (fixedArgTypes, nonFixedArgTypes) = allHandleArgTypes.splitAt(allHandleArgTypes.size - ifaceMethod.argTypes.size)
    // In some cases, "this" is a non fixed arg
    val instantiatedArgTypes = IType.getArgumentTypes(insn.bsmArgs(2).asInstanceOf[Type].getDescriptor)
    val thisArgTypeOpt =
      if (instantiatedArgTypes.size == nonFixedArgTypes.size) None
      else if (instantiatedArgTypes.size == nonFixedArgTypes.size + 1) Some(instantiatedArgTypes.head)
      else sys.error(s"Expected arg count of $instantiatedArgTypes to be within one of $nonFixedArgTypes")
    val (sourceArgTypes, sourceRetType) =
      IType.getArgumentAndReturnTypes(insn.bsmArgs(2).asInstanceOf[Type].getDescriptor)
    val (_, handleResult) = handleArgAndRetTypes(handle)
    buildDirectProxyFuncRef(ctx, handle, fixedArgTypes, nonFixedArgTypes, thisArgTypeOpt).map { case (ctx, funcRef) =>
      // We have to convert twice. First from handle -> subject, then from subject -> target iface
      funcRefToFuncRefExact(
        ctx,
        funcRef,
        thisArgTypeOpt.toSeq ++ nonFixedArgTypes,
        handleResult,
        sourceArgTypes,
        sourceRetType
      ).map { case (ctx, funcRef) =>
        funcRefToFuncRefExact(
          ctx,
          funcRef,
          sourceArgTypes,
          sourceRetType,
          ifaceMethod.argTypes,
          ifaceMethod.returnType
        ).map { case (ctx, funcRef) =>
          ctx.staticInstRefExpr(ifaceTyp.internalName).map { case (ctx, staticInst) =>
            ctx.stackPushed(TypedExpression(
              expr = staticInst.sel(
                ctx.mangler.funcInterfaceProxyCreateMethodName(ifaceTyp.internalName)
              ).call(Seq(funcRef)),
              typ = ifaceTyp,
              cheapRef = true
            )) -> Nil
          }
        }
      }
    }
  }

  protected def buildDirectProxyFuncRef(
    ctx: Context,
    handle: Handle,
    fixedArgTypes: Seq[IType],
    nonFixedArgTypes: Seq[IType],
    thisArgType: Option[IType]
  ): (Context, Node.Expression) = {
    logger.trace(s"Building direct proxy refs for fixed args of $fixedArgTypes and non-fixed of $nonFixedArgTypes")
    val retType = IType.getReturnType(handle.getDesc)
    val stackPopCount = handle.getTag match {
      case Opcodes.H_GETSTATIC | Opcodes.H_INVOKESTATIC |
           Opcodes.H_PUTSTATIC | Opcodes.H_NEWINVOKESPECIAL => fixedArgTypes.size
      case _ if thisArgType.isDefined => fixedArgTypes.size
      case _ => fixedArgTypes.size + 1
    }
    def convArgTypes(ctx: Context, args: Seq[TypedExpression]): (Context, Seq[Node.Expression]) = {
      require(args.length == fixedArgTypes.size, "Expected fixed arg count of certain size")
      args.zip(fixedArgTypes).foldLeft(ctx -> Seq.empty[Node.Expression]) {
        case ((ctx, prevArgs), (arg, argType)) =>
          arg.toExprNode(ctx, argType).map { case (ctx, argExpr) => ctx -> (prevArgs :+ argExpr) }
      }
    }
    ctx.stackPopped(stackPopCount, { case (ctx, stk) =>
      val ctxAndFuncRef = handle.getTag match {
        case Opcodes.H_GETFIELD =>
          require(stk.size == 1)
          instanceFieldAccessorRef(ctx, handle.getName, handle.getDesc, handle.getOwner, stk.head, getter = true)
        case Opcodes.H_GETSTATIC =>
          require(stk.isEmpty)
          staticFieldRef(ctx, handle.getName, handle.getDesc, handle.getOwner).map { case (ctx, ref) =>
            ctx.typeToGoType(IType.getType(handle.getDesc)).map { case (ctx, retTyp) =>
              ctx -> funcType(Nil, Some(retTyp)).toFuncLit(Seq(ref.ret))
            }
          }
        case Opcodes.H_PUTFIELD =>
          require(stk.size == 1)
          instanceFieldAccessorRef(ctx, handle.getName, handle.getDesc, handle.getOwner, stk.head, getter = false)
        case Opcodes.H_PUTSTATIC =>
          require(stk.isEmpty)
          staticFieldRef(ctx, handle.getName, handle.getDesc, handle.getOwner).map { case (ctx, ref) =>
            ctx.typeToGoType(IType.getType(handle.getDesc)).map { case (ctx, paramTyp) =>
              ctx -> funcType(Seq("v" -> paramTyp)).toFuncLit(Seq(ref.assignExisting("v".toIdent)))
            }
          }
        case Opcodes.H_INVOKEVIRTUAL | Opcodes.H_INVOKEINTERFACE | Opcodes.H_INVOKESPECIAL =>
          val ctxAndSubject = thisArgType match {
            case None => ctx -> stk.head
            case Some(typ) => ctx.nextUnusedVarName().map {
              case (ctx, name) => ctx -> TypedExpression.namedVar(name, typ)
            }
          }
          ctxAndSubject.map { case (ctx, subject) =>
            val resolved =
              if (handle.getTag == Opcodes.H_INVOKESPECIAL)
                resolveSpecialMethodRef(ctx, handle.getName, handle.getDesc,
                  subject, handle.isInterface, handle.getOwner)
              else
                resolveInterfaceOrVirtualMethodRef(ctx, handle.getName, handle.getDesc, subject, handle.getOwner)
            val ctxAndMethodWithFuncRef = resolved.map {
              // Default includes the subject as an arg which means we need a new func lit
              case (ctx, (method, methodRef)) if method.isDefault =>
                // We make our own param names to prevent clash
                signatureCompiler.buildFuncType(ctx, method, includeParamNames = false).map { case (ctx, funcTyp) =>
                  subject.toExprNode(ctx, IType.getObjectType(method.cls.name)).map { case (ctx, subject) =>
                    ctx.nextUnusedVarNames(funcTyp.parameters.size).map { case (ctx, paramNames) =>
                      val funcTypWithNames = funcTyp.copy(parameters = funcTyp.parameters.zipWithIndex.map {
                        case (param, i) => param.copy(names = Seq(paramNames(i).toIdent))
                      })
                      val call = methodRef.call(subject +: method.argTypes.indices.map(i => paramNames(i).toIdent))
                      val callStmt = method.returnType match {
                        case IType.VoidType => call.toStmt
                        case _ => call.ret
                      }
                      ctx -> (method -> funcTypWithNames.toFuncLit(Seq(callStmt)))
                    }
                  }
                }
              case v => v
            }
            // When we have a "this" type, we have to wrap in a completely new func ref accepting a new "this" type
            thisArgType match {
              case None => ctxAndMethodWithFuncRef.map { case (ctx, (_, funcRef)) => ctx -> funcRef }
              case Some(thisArgType) => ctxAndMethodWithFuncRef.map { case (ctx, (method, funcRef)) =>
                // Just create a copy of the func type with our new "this" type, and forward it
                ctx.nextUnusedVarNames(method.argTypes.size).map { case (ctx, paramNames) =>
                  signatureCompiler.buildFuncType(ctx, thisArgType +: method.argTypes,
                    method.returnType, Some(subject.name +: paramNames)).map { case (ctx, funcTyp) =>
                      val call = funcRef.call(method.argTypes.indices.map(i => paramNames(i).toIdent))
                      val callStmt = method.returnType match {
                        case IType.VoidType => call.toStmt
                        case _ => call.ret
                      }
                      ctx -> funcTyp.toFuncLit(Seq(callStmt))
                    }
                }
              }
            }
          }
        case Opcodes.H_INVOKESTATIC =>
          resolveStaticMethodRef(ctx, handle.getName, handle.getDesc, handle.getOwner).map {
            case (ctx, (method, methodRef)) => ctx -> methodRef
          }
        case Opcodes.H_NEWINVOKESPECIAL =>
          ctx.staticNewExpr(handle.getOwner).map { case (ctx, newExpr) =>
            val retTyp = IType.getObjectType(handle.getOwner)
            val newTyped = TypedExpression.namedVar("v", retTyp)
            resolveSpecialMethodRef(ctx, handle.getName, handle.getDesc, newTyped, handle.isInterface, handle.getOwner).
              map { case (ctx, (_, initRef)) =>
                val ctxAndArgTypes = IType.getArgumentTypes(handle.getDesc).foldLeft(ctx -> Seq.empty[Node.Expression]) {
                  case ((ctx, argTypes), argTyp) =>
                    ctx.typeToGoType(argTyp).map { case (ctx, typ) => ctx -> (argTypes :+ typ) }
                }
                ctxAndArgTypes.map { case (ctx, argTypes) =>
                  ctx.typeToGoType(retTyp).map { case (ctx, retTypExpr) =>
                    ctx.nextUnusedVarNames(argTypes.size).map { case (ctx, paramNames) =>
                      ctx -> funcType(
                        params = argTypes.zipWithIndex.map { case (arg, index) => paramNames(index) -> arg },
                        result = Some(retTypExpr)
                      ).toFuncLit(Seq(
                        varDecl(newTyped.name, retTypExpr, Some(newExpr)).toStmt,
                        initRef.call(argTypes.indices.map(i => paramNames(i).toIdent)).toStmt,
                        newTyped.expr.ret
                      ))
                    }
                  }
                }
              }
          }
      }

      // Take the func ref and map fixed args onto it and make non-fixed args the ones accepted.
      // If there are no fixed args, we can just return the ref
      if (fixedArgTypes.isEmpty) ctxAndFuncRef else ctxAndFuncRef.map { case (ctx, funcRef) =>
        convArgTypes(ctx, stk.takeRight(fixedArgTypes.size)).map { (ctx, fixedArgExprs) =>
          ctx.nextUnusedVarNames(nonFixedArgTypes.size).map { case (ctx, nonFixedNames) =>
            signatureCompiler.buildFuncType(ctx, nonFixedArgTypes, retType, Some(nonFixedNames)).map {
              case (ctx, newFuncTyp) =>
                val call = funcRef.call(fixedArgExprs ++ nonFixedArgTypes.indices.map(i => nonFixedNames(i).toIdent))
                ctx -> newFuncTyp.toFuncLit(Seq(retType match {
                  case IType.VoidType => call.toStmt
                  case _ => call.ret
                }))
            }
          }
        }
      }
    })
  }

  protected def funcRefToFuncRefExact(
    ctx: Context,
    source: Node.Expression,
    sourceArgs: Seq[IType],
    sourceResult: IType,
    targetArgs: Seq[IType],
    targetResult: IType
  ): (Context, Node.Expression) = {
    logger.trace(s"Translating func from ($sourceArgs)$sourceResult to ($targetArgs)$targetResult")
    require(sourceArgs.size == targetArgs.size, "Expect same size when translating func refs")
    if (sourceArgs == targetArgs && sourceResult == targetResult) ctx -> source else {
      val ctxAndConvArgsWithFields = ctx.nextUnusedVarNames(targetArgs.size).map { case (ctx, paramNames) =>
        val init = ctx -> Seq.empty[(Node.Expression, Node.Field)]
        sourceArgs.zip(targetArgs).zipWithIndex.foldLeft(init) {
          case ((ctx, argsWithFields), ((sourceArg, targetArg), index)) =>
            val name = paramNames(index)
            TypedExpression(name.toIdent, targetArg, cheapRef = true).toExprNode(ctx, sourceArg).map {
              case (ctx, arg) =>
                ctx.typeToGoType(targetArg).map { case (ctx, targetTyp) =>
                  ctx -> (argsWithFields :+ (arg -> field(name, targetTyp)))
                }
            }

        }
      }
      ctxAndConvArgsWithFields.map { case (ctx, convArgsWithFields) =>
        val call = source.call(convArgsWithFields.map(_._1))
        val ctxAndCallStmtWithResultTypOpt = targetResult match {
          case IType.VoidType => ctx -> (call.toStmt -> None)
          case typ => TypedExpression(call, sourceResult, cheapRef = true).toExprNode(ctx, typ).map {
            case (ctx, call) => ctx.typeToGoType(typ).map { case (ctx, targetRetTyp) =>
              ctx -> (call.ret -> Some(targetRetTyp))
            }
          }
        }
        ctxAndCallStmtWithResultTypOpt.map { case (ctx, (callStmt, resultTypOpt)) =>
          ctx -> funcTypeWithFields(convArgsWithFields.map(_._2), resultTypOpt).toFuncLit(Seq(callStmt))
        }
      }
    }
  }

  protected def handleArgAndRetTypes(handle: Handle): (Seq[IType], IType) = {
    handle.getTag match {
      case Opcodes.H_GETFIELD | Opcodes.H_GETSTATIC =>
        Nil -> IType.getType(handle.getDesc)
      case Opcodes.H_PUTFIELD | Opcodes.H_PUTSTATIC =>
        Seq(IType.getType(handle.getDesc)) -> IType.VoidType
      case Opcodes.H_INVOKEVIRTUAL | Opcodes.H_INVOKEINTERFACE | Opcodes.H_INVOKESPECIAL | Opcodes.H_INVOKESTATIC =>
        IType.getArgumentAndReturnTypes(handle.getDesc)
      case Opcodes.H_NEWINVOKESPECIAL =>
        IType.getArgumentTypes(handle.getDesc) -> IType.getObjectType(handle.getOwner)
    }
  }

  protected def pushInvokedCallSite(ctx: Context, insn: InvokeDynamicInsnNode): (Context, Seq[Node.Statement]) = {
    pushBootstrappedCallSite(ctx, insn).map { case (ctx, stmts) =>
      ctx.stackPopped { case (ctx, callSite) =>
        val (argTyps, retTyp) = IType.getArgumentAndReturnTypes(insn.desc)
        ctx.stackPopped(argTyps.size, { case (ctx, args) =>
          ctx.typeToGoType(retTyp).map { case (ctx, retTypExpr) =>
            val call = callSite.expr.sel(
              ctx.mangler.forwardMethodName("dynamicInvoker", "()Ljava/lang/invoke/MethodHandle;", None, isPriv = false)
            ).call().sel(
              ctx.mangler.forwardMethodName("invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", None, isPriv = false)
            ).call(args.map(_.expr)).typeAssert(retTypExpr)
            ctx.stackPushed(TypedExpression(
              expr = call,
              typ = retTyp,
              cheapRef = false
            )) -> stmts
          }
        })
      }
    }
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
        ctx -> syncVarName.toIdent.sel("Do").call(Seq(
          funcType(Nil).toFuncLit(Seq(
            callSiteVarName.toIdent.assignExisting(bootstrapCall.expr)
          ))
        )).toStmt.singleSeq
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
      val forwardInsn = new MethodInsnNode(methodOpcode, insn.bsm.getOwner, insn.bsm.getName, insn.bsm.getDesc, false)
      logger.trace(s"Forwarding invokedynamic call as method insn ${forwardInsn.pretty}: ${ctx.prettyAppend}")
      compile(ctx, forwardInsn)._1
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
        case v: java.lang.Float => v.toFloat.toTypedLit(ctx).map { case (ctx, lit) => ctx.stackPushed(lit) }
        case v: java.lang.Long => ctx.stackPushed(v.toLong.toTypedLit)
        case v: java.lang.Double => v.toDouble.toTypedLit(ctx).map { case (ctx, lit) => ctx.stackPushed(lit) }
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
      compile(ctx, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, method.cls.name, method.name, method.desc, false))._1
    }
  }

  protected def signatureCompiler: SignatureCompiler = SignatureCompiler
  protected def methodSetManager: MethodSetManager = MethodSetManager.Default
}
