package goahead.compile

import java.util.Collections

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.{ClassNode, InvokeDynamicInsnNode}

import scala.util.control.NonFatal

trait ClassCompiler extends Logger {
  import AstDsl._
  import ClassCompiler._
  import Helpers._

  def compile(conf: Config, cls: Cls, imports: Imports, mangler: Mangler): (Imports, Seq[Node.Declaration]) = {
    logger.debug(s"Compiling class: ${cls.name}")
    try {
      // TODO: limit major version to 1.6+ to avoid issues with JSR/RET deprecation?
      compile(initContext(conf, cls, imports, mangler)).map { case (ctx, decls) => ctx.imports -> decls }
    } catch { case NonFatal(e) => throw new Exception(s"Unable to compile class ${cls.name}", e) }
  }

  protected def initContext(
    conf: Config,
    cls: Cls,
    imports: Imports,
    mangler: Mangler
  ) = Context(conf, cls, imports, mangler)

  protected def compile(ctx: Context): (Context, Seq[Node.Declaration]) = {
    if (ctx.cls.access.isAccessInterface) compileInterface(ctx) else compileClass(ctx)
  }

  protected def compileInterface(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStatic(ctx).map { case (ctx, staticDecls) =>
      compileInstInterface(ctx).map { case (ctx, instIfaceDecl) =>
        compileInterfaceDefaultMethods(ctx).map { case (ctx, ifaceDefaultDecls) =>
          compileFunctionalInterfaceProxy(ctx).map { case (ctx, ifaceProxyDecls) =>
            ctx -> (staticDecls ++ instIfaceDecl.singleSeq ++ ifaceDefaultDecls ++ ifaceProxyDecls)
          }
        }
      }
    }
  }

  protected def compileClass(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStatic(ctx).map { case (ctx, staticDecls) =>
      compileDispatch(ctx).map { case (ctx, dispatchDecls) =>
        compileInstInterface(ctx).map { case (ctx, instDecl) =>
          compileImpl(ctx).map { case (ctx, implDecls) =>
            ctx -> (staticDecls ++ dispatchDecls ++ instDecl.singleSeq ++ implDecls)
          }
        }
      }
    }
  }

  protected def compileStatic(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStaticStruct(ctx).map { case (ctx, staticStruct) =>
      compileStaticVar(ctx).map { case (ctx, staticVar) =>
        compileStaticAccessor(ctx).map { case (ctx, staticAccessor) =>
          compileStaticNew(ctx).map { case (ctx, staticNew) =>
            compileStaticMethods(ctx).map { case (ctx, staticMethods) =>
              ctx -> (Seq(staticStruct, staticVar, staticAccessor) ++ staticNew ++ staticMethods)
            }
          }
        }
      }
    }
  }

  protected def compileStaticStruct(ctx: Context): (Context, Node.GenericDeclaration) = {
    compileFields(ctx, clsFields(ctx).filter(_.access.isAccessStatic)).map { case (ctx, fields) =>
      // Need a sync once
      val ctxAndStaticFieldOpt = if (!ctx.cls.hasStaticInit) ctx -> None else {
        ctx.withImportAlias("sync").map { case (ctx, syncAlias) =>
          ctx -> Some(field("init", syncAlias.toIdent.sel("Once")))
        }
      }

      ctxAndStaticFieldOpt.map { case (ctx, staticFieldOpt) =>
        ctx -> struct(ctx.mangler.staticObjectName(ctx.cls.name), fields ++ staticFieldOpt)
      }
    }
  }

  protected def compileStaticVar(ctx: Context): (Context, Node.GenericDeclaration) = {
    ctx -> varDecl(ctx.mangler.staticVarName(ctx.cls.name), ctx.mangler.staticObjectName(ctx.cls.name).toIdent)
  }

  protected def compileStaticAccessor(ctx: Context): (Context, Node.FunctionDeclaration) = {
    // We call the static init in a single sync once block
    val staticVarName = ctx.mangler.staticVarName(ctx.cls.name).toIdent
    val initStmtOpt = if (!ctx.cls.hasStaticInit) None else Some {
      val syncOnceField = staticVarName.sel("init")
      syncOnceField.sel("Do").call(
        staticVarName.sel(ctx.mangler.implMethodName("<clinit>", "()V")).singleSeq
      ).toStmt
    }
    ctx.staticInstTypeExpr(ctx.cls.name).map { case (ctx, staticTyp) =>
      ctx -> funcDecl(
        rec = None,
        name = ctx.mangler.staticAccessorName(ctx.cls.name),
        params = Nil,
        results = Some(staticTyp),
        stmts = initStmtOpt.toSeq ++ staticVarName.unary(Node.Token.And).ret.singleSeq
      )
    }
  }

  protected def compileStaticNew(ctx: Context): (Context, Option[Node.FunctionDeclaration]) = {
    if (ctx.cls.access.isAccessInterface) ctx -> None else {
      val ctxAndStaticNewElements = ctx.cls.parent match {
        case None => ctx -> Nil
        case Some(superName) => ctx.staticNewExpr(superName).map { case (ctx, superStaticNewExpr) =>
          ctx -> ctx.mangler.implObjectName(superName).toIdent.withValue(superStaticNewExpr).singleSeq
        }
      }

      val ctxAndStmts = ctxAndStaticNewElements.map { case (ctx, staticNewElements) =>
        val defineStmt = "v".toIdent.assignDefine(literal(
          Some(ctx.mangler.implObjectName(ctx.cls.name).toIdent),
          staticNewElements: _*
        ).unary(Node.Token.And))
        val initDispatchStmt = "v".toIdent.sel(
          ctx.mangler.dispatchInitMethodName(ctx.cls.name)
        ).call("v".toIdent.singleSeq).toStmt
        val retStmt = "v".toIdent.ret
        ctx -> Seq(defineStmt, initDispatchStmt, retStmt)
      }

      ctxAndStmts.map { case (ctx, stmts) =>
        ctx -> Some(funcDecl(
          rec = Some("this" -> ctx.mangler.staticObjectName(ctx.cls.name).toIdent.star),
          name = "New",
          params = Nil,
          results = Some(ctx.mangler.implObjectName(ctx.cls.name).toIdent.star),
          stmts = stmts
        ))
      }
    }
  }

  protected def compileStaticMethods(ctx: Context): (Context, Seq[Node.Declaration]) = {
    val methods = methodSetManager.staticMethods(ctx.classPath, ctx.cls).methodsWithCovariantReturnDuplicates
    compileMethods(ctx, methods).map { case (ctx, funcs) =>

      // As a special case, we have to combine all static inits, so we'll do it with anon
      // functions if there is more than one
      val staticMethodName = ctx.mangler.implMethodName("<clinit>", "()V")
      val (staticInitFns, otherFns) = funcs.partition(_.name.name == staticMethodName)
      val staticFuncs = if (staticInitFns.length <= 1) funcs else {
        val newStaticInitFn = staticInitFns.head.copy(
          body = Some(block(
            staticInitFns.map { staticInitFn =>
              funcType(Nil).toFuncLit(staticInitFn.body.get.statements).call().toStmt
            }
          ))
        )
        newStaticInitFn +: otherFns
      }

      compileInvokeDynamicSyncVars(ctx, methods).map { case (ctx, invokeDynVars) =>
        ctx -> (staticFuncs ++ invokeDynVars)
      }
    }
  }

  protected def compileFunctionalInterfaceProxy(ctx: Context): (Context, Seq[Node.Declaration]) = {
    methodSetManager.functionalInterfaceMethod(ctx.classPath, ctx.cls) match {
      case None => ctx -> Nil
      case Some(ifaceMethod) =>
        // The way we do this is to "extend" the current class with no overrides, create a field for the call site,
        // and then manually create the create function on the static part of this class
        val extension = new ClassNode()
        ctx.cls.asInstanceOf[Cls.Asm].node.accept(extension)
        extension.access &= ~Opcodes.ACC_INTERFACE
        extension.name = ctx.cls.name + ctx.mangler.funcInterfaceProxySuffix(ctx.cls.name)
        extension.superName = "java/lang/Object"
        extension.interfaces = Collections.singletonList(ctx.cls.name)
        extension.visibleAnnotations = null
        extension.invisibleAnnotations = null
        extension.visibleTypeAnnotations = null
        extension.invisibleTypeAnnotations = null
        extension.innerClasses.clear()
        extension.fields.clear()
        extension.methods.clear()

        // Set the new class, inject into class path, run compile again
        val origCtx = ctx
        val extensionCls = Cls(extension)
        val newCtx = origCtx.copy(
          cls = extensionCls,
          imports = origCtx.imports.copy(classPath = origCtx.classPath.copy(
            entries = origCtx.classPath.entries :+ ClassPath.Entry.SingleClassEntry(
              extensionCls,
              origCtx.classPath.getFirstClass(ctx.cls.name).relativeCompiledDir
            )
          ))
        )
        compile(newCtx).map { case (extCtx, decls) =>
          // Put the original class and class path back, but keep everything else
          val ctx = extCtx.copy(cls = origCtx.cls, imports = extCtx.imports.copy(classPath = origCtx.classPath))

          // Take the impl struct and add a field to it for the function
          val implStructName = ctx.mangler.implObjectName(extensionCls.name)
          val ctxAndDecls = decls.foldLeft(ctx -> Seq.empty[Node.Declaration]) {
            case ((ctx, decls), Node.GenericDeclaration(
              Node.Token.Type,
              Seq(Node.TypeSpecification(Node.Identifier(ident), Node.StructType(fields)))
            )) if ident == implStructName =>
              signatureCompiler.buildFuncType(ctx, ifaceMethod, includeParamNames = false).map { case (ctx, funcTyp) =>
                ctx -> (decls :+ struct(ident, fields :+ field("fn", funcTyp)))
              }
            case ((ctx, decls), decl) =>
              ctx -> (decls :+ decl)
          }

          ctxAndDecls.map { case (ctx, decls) =>
            compileFunctionalInterfaceProxyCreator(ctx, extensionCls, ifaceMethod).map { case (ctx, creatorDecl) =>
              compileFunctionalInterfaceProxyMethod(ctx, extensionCls, ifaceMethod).map { case (ctx, methodDecl) =>
                ctx -> (decls :+ creatorDecl :+ methodDecl)
              }
            }
          }
        }
    }
  }

  protected def compileFunctionalInterfaceProxyCreator(
    ctx: Context,
    extension: Cls,
    ifaceMethod: Method
  ): (Context, Node.FunctionDeclaration) = {
    ctx.staticInstTypeExpr(ctx.cls.name).map { case (ctx, staticTyp) =>
      signatureCompiler.buildFuncType(ctx, ifaceMethod, includeParamNames = false).map { case (ctx, funcTyp) =>
        ctx.typeToGoType(IType.getObjectType(ctx.cls.name)).map { case (ctx, retTyp) =>
          ctx.staticNewExpr(extension.parent.get).map { case (ctx, superStaticNewExpr) =>
            val parentImplObjName = ctx.mangler.implObjectName(extension.parent.get)
            val defineStmt = "v".toIdent.assignDefine(literal(
              Some(ctx.mangler.implObjectName(extension.name).toIdent),
              parentImplObjName.toIdent.withValue(superStaticNewExpr),
              "fn".toIdent.withValue("fn".toIdent)
            ).unary(Node.Token.And))
            val initDispatchStmt = "v".toIdent.sel(
              ctx.mangler.dispatchInitMethodName(extension.name)
            ).call("v".toIdent.singleSeq).toStmt
            val constructStmt = "v".toIdent.sel(parentImplObjName).
              sel(ctx.mangler.implMethodName("<init>", "()V")).call().toStmt
            val retStmt = "v".toIdent.ret
            ctx -> funcDecl(
              rec = Some("_" -> staticTyp),
              name = ctx.mangler.funcInterfaceProxyCreateMethodName(ctx.cls.name),
              params = Seq("fn" -> funcTyp),
              results = Some(retTyp),
              stmts = Seq(defineStmt, initDispatchStmt, constructStmt, retStmt)
            )
          }
        }
      }
    }
  }

  protected def compileFunctionalInterfaceProxyMethod(
    ctx: Context,
    extension: Cls,
    ifaceMethod: Method
  ): (Context, Node.FunctionDeclaration) = {
    // Just forward the call to the call site passing each param and type asserting the result
    val call = "this".toIdent.sel("fn").call(ifaceMethod.argTypes.indices.map(i => s"var$i".toIdent))
    val callStmt = ifaceMethod.returnType match {
      case IType.VoidType => call.toStmt
      case _ => call.ret
    }
    signatureCompiler.buildFuncDecl(ctx, ifaceMethod, Seq(callStmt)).map { case (ctx, funcDecl) =>
      ctx -> funcDecl.copy(receivers = Seq(field("this", ctx.mangler.implObjectName(extension.name).toIdent.star)))
    }
  }

  protected def compileInstInterface(ctx: Context): (Context, Node.Declaration) = {
    // Only non-private, non-init methods including parents
    // Note, the set of methods also includes covariant returns
    val methods = methodSetManager.instInterfaceMethods(ctx.classPath, ctx.cls).methodsWithCovariantReturnDuplicates
    val ctxAndMethodSigs = methods.foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, methodSigs), method) =>
        signatureCompiler.buildFuncType(ctx, method, includeParamNames = false).map {
          case (ctx, funcType) =>
            ctx -> (methodSigs :+ field(ctx.mangler.forwardMethodName(method.name, method.desc), funcType))
        }
    }

    // Add non-private, non-static field accessors (even of parents)
    val ctxAndAllFields = ctxAndMethodSigs.map { case (ctx, methodSigs) =>
      val fields = clsFields(ctx, includeParentFields = true).
        filterNot(f => f.access.isAccessStatic || f.access.isAccessPrivate)
      fields.foldLeft(ctx -> methodSigs) { case ((ctx, fields), node) =>
        signatureCompiler.buildFieldGetterFuncType(ctx, node).map { case (ctx, getterType) =>
          signatureCompiler.buildFieldSetterFuncType(ctx, node, includeParamNames = false).map {
            case (ctx, setterType) =>
              ctx -> (fields ++ Seq(
                field(ctx.mangler.fieldGetterName(node.cls.name, node.name), getterType),
                field(ctx.mangler.fieldSetterName(node.cls.name, node.name), setterType)
              ))
          }
        }
      }
    }

    ctxAndAllFields.map { case (ctx, fields) =>
      compileInstInterfaceRawPointerMethodSigs(ctx).map { case (ctx, rawSigs) =>
        ctx -> interface(name = ctx.mangler.instanceInterfaceName(ctx.cls.name), fields = fields ++ rawSigs)
      }
    }
  }

  protected def compileInstInterfaceRawPointerMethodSigs(ctx: Context): (Context, Seq[Node.Field]) = {
      // Gotta do it for all parents (and myself if I'm not an interface)
      val allClasses =
        if (ctx.cls.access.isAccessInterface) ctx.classPath.allSuperTypes(ctx.cls.name).map(_.cls.name)
        else ctx.cls.name +: ctx.classPath.allSuperTypes(ctx.cls.name).map(_.cls.name)
      allClasses.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, fields), clsName) =>
        ctx.implTypeExpr(clsName).map { case (ctx, typPtr) =>
          ctx -> (fields :+ field(
            str = ctx.mangler.instanceRawPointerMethodName(clsName),
            typ = funcType(Nil, Some(typPtr))
          ))
        }
      }
  }

  protected def compileInterfaceDefaultMethods(ctx: Context): (Context, Seq[Node.Declaration]) = {
    val methods = methodSetManager.instInterfaceDefaultMethods(ctx.classPath, ctx.cls).methods
    compileMethods(ctx, methods).map { case (ctx, funcs) =>
      compileInvokeDynamicSyncVars(ctx, methods).map { case (ctx, invokeDynVars) =>
        ctx -> (funcs ++ invokeDynVars)
      }
    }
  }

  protected def compileImpl(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileImplStruct(ctx).map { case (ctx, struct) =>
      compileImplMethods(ctx).map { case (ctx, methods) =>
        compileImplDefaultMethodForwarders(ctx).map { case (ctx, defaultForwarders) =>
          ctx -> (struct +: (methods ++ defaultForwarders))
        }
      }
    }
  }

  protected def compileImplStruct(ctx: Context): (Context, Node.Declaration) = {
    compileFields(ctx, clsFields(ctx).filterNot(_.access.isAccessStatic)).map { case (ctx, fields) =>
      compileStructSuperFields(ctx).map { case (ctx, superFields) =>
        ctx -> struct(ctx.mangler.implObjectName(ctx.cls.name), superFields ++ fields)
      }
    }
  }

  protected def compileImplMethods(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileImplFieldAccessors(ctx).map { case (ctx, accessorDecls) =>
      val methodSet = methodSetManager.implMethods(ctx.classPath, ctx.cls)
      compileMethods(ctx, methodSet.methods).map { case (ctx, methodDecls) =>
        compileInvokeDynamicSyncVars(ctx, methodSet.methods).map { case (ctx, invokeDynVars) =>
          compileImplRawPointerMethod(ctx).map { case (ctx, rawPointerMethod) =>
            compileImplCovariantReturnDuplicateForwarders(ctx, methodSet.covariantReturnDuplicates).map {
              case (ctx, covariantMethodDecls) =>
                ctx -> (accessorDecls ++ methodDecls ++ invokeDynVars ++ covariantMethodDecls :+ rawPointerMethod)
            }
          }
        }
      }
    }
  }

  protected def compileImplCovariantReturnDuplicateForwarders(
    ctx: Context,
    methods: Seq[(Method, Seq[Method])]
  ): (Context, Seq[Node.FunctionDeclaration]) = {
    methods.foldLeft(ctx -> Seq.empty[Node.FunctionDeclaration]) { case ((ctx, funcDecls), (target, dupes)) =>
      dupes.foldLeft(ctx -> funcDecls) { case ((ctx, funcDecls), dupe) =>
        val call = "this".toIdent.sel(ctx.mangler.implMethodName(target.name, target.desc)).
          call(IType.getArgumentTypes(target.desc).indices.map(i => s"var$i".toIdent))
        signatureCompiler.buildFuncDecl(ctx, dupe, Seq(call.ret)).map { case (ctx, funcDecl) =>
          ctx -> (funcDecls :+ funcDecl)
        }
      }
    }
  }

  protected def compileImplRawPointerMethod(ctx: Context): (Context, Node.FunctionDeclaration) = {
    ctx -> funcDecl(
      rec = Some("this" -> ctx.mangler.implObjectName(ctx.cls.name).toIdent.star),
      name = ctx.mangler.instanceRawPointerMethodName(ctx.cls.name),
      params = Nil,
      results = Some(ctx.mangler.implObjectName(ctx.cls.name).toIdent.star),
      stmts = "this".toIdent.ret.singleSeq
    )
  }

  protected def compileImplDefaultMethodForwarders(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    val methods = methodSetManager.implDefaultForwarderMethods(ctx.classPath, ctx.cls).methods
    methods.foldLeft(ctx -> Seq.empty[Node.FunctionDeclaration]) {
      case ((ctx, funcDecls), defaultMethod) =>
        val defName = ctx.mangler.interfaceDefaultMethodName(
          defaultMethod.cls.name, defaultMethod.name, defaultMethod.desc
        )
        ctx.importQualifiedName(defaultMethod.cls.name, defName).map { case (ctx, defName) =>
          val forwardCall = defName.call("this".toIdent +:
            IType.getArgumentTypes(defaultMethod.desc).indices.map(i => s"var$i".toIdent))
          val forwardStmts = Seq(
            IType.getReturnType(defaultMethod.desc) match {
              case IType.VoidType => forwardCall.toStmt
              case _ => forwardCall.ret
            }
          )
          signatureCompiler.buildFuncDecl(ctx, defaultMethod, forwardStmts).map { case (ctx, funcDecl) =>
            ctx -> (funcDecls :+ funcDecl)
          }
        }
    }
  }

  protected def compileStructSuperFields(ctx: Context): (Context, Seq[Node.Field]) = {
    val superNames =
      if (ctx.cls.access.isAccessInterface) ctx.cls.interfaces else ctx.cls.parent.toSeq
    val ctxAndSuperFields = superNames.foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, fields), sup) =>
        val ctxAndSuper =
          if (ctx.cls.access.isAccessInterface) ctx.typeToGoType(IType.getObjectType(sup))
          else ctx.implTypeExpr(sup)
        ctxAndSuper.map { case (ctx, superType) =>
          ctx -> (fields :+ superType.namelessField)
        }
    }
    // Also need dispatch iface
    ctxAndSuperFields.map { case (ctx, superFields) =>
      ctx -> (superFields :+ field("_dispatch", ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent))
    }
  }

  protected def compileFields(ctx: Context, fields: Seq[Field]): (Context, Seq[Node.Field]) = {
    fields.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, prevFields), node) =>
      logger.debug(s"Compiling field ${ctx.cls.name}.${node.name}")
      ctx.typeToGoType(IType.getType(node.desc)).map { case (ctx, typ) =>
        ctx -> (prevFields :+ field(ctx.mangler.fieldName(ctx.cls.name, node.name), typ))
      }
    }
  }

  protected def compileImplFieldAccessors(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    // All non-static fields get getters and setters, even private fields
    val fields = clsFields(ctx).filterNot(_.access.isAccessStatic)
    fields.foldLeft(ctx -> Seq.empty[Node.FunctionDeclaration]) { case ((ctx, decls), node) =>
      ctx.implTypeExpr(ctx.cls.name).map { case (ctx, thisType) =>
        signatureCompiler.buildFieldGetterFuncType(ctx, node).map { case (ctx, getterType) =>
          signatureCompiler.buildFieldSetterFuncType(ctx, node, includeParamNames = true).map {
            case (ctx, setterType) => ctx -> (decls ++ Seq(
              funcDecl(
                rec = Some(field("this", thisType)),
                name = ctx.mangler.fieldGetterName(node.cls.name, node.name),
                funcType = getterType,
                stmts = "this".toIdent.sel(ctx.mangler.fieldName(ctx.cls.name, node.name)).ret.singleSeq
              ),
              funcDecl(
                rec = Some(field("this", thisType)),
                name = ctx.mangler.fieldSetterName(node.cls.name, node.name),
                funcType = setterType,
                stmts = "this".toIdent.sel(ctx.mangler.fieldName(ctx.cls.name, node.name)).
                  assignExisting("v".toIdent).singleSeq
              )
            ))
          }
        }
      }
    }
  }

  protected def compileMethods(ctx: Context, methods: Seq[Method]): (Context, Seq[Node.FunctionDeclaration]) = {
    methods.foldLeft(ctx -> Seq.empty[Node.FunctionDeclaration]) { case ((ctx, prevFuncs), node) =>
      val (newImports, fn) = methodCompiler.compile(
        conf = ctx.conf,
        cls = ctx.cls,
        method = node,
        imports = ctx.imports,
        mangler = ctx.mangler
      )
      ctx.copy(imports = newImports) -> (prevFuncs :+ fn)
    }
  }

  protected def compileInvokeDynamicSyncVars(
    ctx: Context,
    methods: Seq[Method]
  ): (Context, Seq[Node.GenericDeclaration]) = {
    // Each invoke dynamic instruction gets a sync.Once var to synchronize the invoke dynamic build, then it also
    // gets a call site var to store the permanent result globally
    methods.foldLeft(ctx -> Seq.empty[Node.GenericDeclaration]) { case (ctxAndPrevVars, method) =>
      // This only applies to non-optimizable insns
      val invokeInsns = method.instructions.zipWithIndex.collect {
        case (n: InvokeDynamicInsnNode, i) if !ctx.isOptimizable(n) => n -> i
      }
      invokeInsns.foldLeft(ctxAndPrevVars) { case ((ctx, prevVars), (insn, index)) =>
        ctx.withImportAlias("sync").map { case (ctx, syncAlias) =>
          ctx.typeToGoType(IType.getObjectType("java/lang/invoke/CallSite")).map { case (ctx, callSiteTyp) =>
            ctx -> (prevVars :+ varDecls(
              ctx.mangler.invokeDynamicSyncVarName(ctx.cls.name, method.name, method.desc, index) ->
                syncAlias.toIdent.sel("Once"),
              ctx.mangler.invokeDynamicCallSiteVarName(ctx.cls.name, method.name, method.desc, index) ->
                callSiteTyp
            ))
          }
        }
      }
    }
  }

  protected def methodCompiler: MethodCompiler = MethodCompiler

  protected def compileDispatch(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileDispatchInterface(ctx).map { case (ctx, ifaceDecl) =>
      compileDispatchInit(ctx).map { case (ctx, initDecl) =>
        compileDispatchForwardMethods(ctx).map { case (ctx, funcDecls) =>
          ctx -> (Seq(ifaceDecl) ++ initDecl ++ funcDecls)
        }
      }
    }
  }

  protected def compileDispatchInterface(ctx: Context): (Context, Node.Declaration) = {
    // Interfaces use parent interfaces, classes just use the parent class if present
    val superNames =
      if (ctx.cls.access.isAccessInterface) ctx.cls.interfaces else ctx.cls.parent.toSeq
    val ctxAndSuperDispatchRefs = superNames.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, fields), superName) =>
      ctx.importQualifiedName(superName, ctx.mangler.dispatchInterfaceName(superName)).map {
        case (ctx, superDispatchInterfaceRef) => ctx -> (fields :+ superDispatchInterfaceRef.namelessField)
      }
    }
    ctxAndSuperDispatchRefs.map { case (ctx, superDispatchRefs) =>
      val dispatchMethodNodes =
        methodSetManager.dispatchInterfaceMethods(ctx.classPath, ctx.cls).methodsWithCovariantReturnDuplicates
      val ctxAndDispatchMethods = dispatchMethodNodes.foldLeft(ctx -> Seq.empty[Node.Field]) {
        case ((ctx, fields), methodNode) =>
          signatureCompiler.buildFuncType(ctx, methodNode, includeParamNames = false).map {
            case (ctx, funcType) =>
              ctx -> (fields :+ field(ctx.mangler.implMethodName(methodNode.name, methodNode.desc), funcType))
          }
      }

      ctxAndDispatchMethods.map { case (ctx, dispatchMethods) =>
        ctx -> interface(
          name = ctx.mangler.dispatchInterfaceName(ctx.cls.name),
          fields = superDispatchRefs ++ dispatchMethods
        )
      }
    }
  }

  protected def compileDispatchInit(ctx: Context): (Context, Option[Node.Declaration]) = {
    // No dispatch init for interfaces at the moment
    if (ctx.cls.access.isAccessInterface) ctx -> None else {
      val ctxAndSuperStmtOpt = ctx.cls.parent match {
        case None => ctx -> None
        case Some(superName) =>
          ctx.implTypeExpr(superName).map { case (ctx, superImplType) =>
            ctx -> Some("this".toIdent.sel(ctx.mangler.implObjectName(superName)).
              sel(ctx.mangler.dispatchInitMethodName(superName)).
              call("v".toIdent.singleSeq).toStmt)
          }
      }

      ctxAndSuperStmtOpt.map { case (ctx, superStmtOpt) =>
        val setDispatchStmt = "this".toIdent.sel("_dispatch").assignExisting("v".toIdent)
        ctx -> Some(funcDecl(
          rec = Some("this" -> ctx.mangler.implObjectName(ctx.cls.name).toIdent.star),
          name = ctx.mangler.dispatchInitMethodName(ctx.cls.name),
          params = Seq("v" -> ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent),
          results = None,
          stmts = superStmtOpt.toSeq :+ setDispatchStmt
        ))
      }
    }
  }

  protected def compileDispatchForwardMethods(ctx: Context): (Context, Seq[Node.Declaration]) = {
    // No dispatch methods for interfaces
    if (ctx.cls.access.isAccessInterface) ctx -> Nil else {
      // We need regular methods AND all covariant return methods that are on an interface
      val methodSet = methodSetManager.dispatchForwardingMethods(ctx.classPath, ctx.cls)
      val methods = methodSet.methodsWithCovariantReturnDuplicates
      methods.foldLeft(ctx -> Seq.empty[Node.Declaration]) { case ((ctx, methods), method) =>
        val call = "this".toIdent.sel("_dispatch").sel(ctx.mangler.implMethodName(method.name, method.desc)).call(
          IType.getArgumentTypes(method.desc).zipWithIndex.map("var" + _._2).map(_.toIdent)
        )
        val stmt = if (IType.getReturnType(method.desc) == IType.VoidType) call.toStmt else call.ret
        signatureCompiler.buildFuncDecl(
          ctx = ctx,
          method = method,
          stmts = stmt.singleSeq,
          nameOverride = Some(ctx.mangler.forwardMethodName(method.name, method.desc))
        ).map { case (ctx, funcDecl) =>
          ctx -> (methods :+ funcDecl)
        }
      }
    }
  }

  // TODO: slow
  protected def clsFields(ctx: Context, includeParentFields: Boolean = false): Seq[Field] = {
    if (!includeParentFields || ctx.cls.access.isAccessInterface) {
      ctx.cls.fields.sortBy(_.name)
    } else {
      (ctx.cls.fields ++ ctx.imports.classPath.allSuperTypes(ctx.cls.name).flatMap(_.cls.fields)).sortBy(_.name)
    }
  }

  protected def methodSetManager: MethodSetManager = MethodSetManager.Default

  protected def signatureCompiler: SignatureCompiler = SignatureCompiler
}

object ClassCompiler extends ClassCompiler {
  case class Context(
    conf: Config,
    cls: Cls,
    imports: Imports,
    mangler: Mangler
  ) extends Contextual[Context] { self =>
    override def updatedImports(mports: Imports) = copy(imports = mports)
  }
}