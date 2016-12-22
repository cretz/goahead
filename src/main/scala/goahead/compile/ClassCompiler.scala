package goahead.compile

import goahead.Logger
import goahead.ast.Node

import scala.util.control.NonFatal

trait ClassCompiler extends Logger {
  import AstDsl._
  import ClassCompiler._
  import Helpers._

  def compile(cls: Cls, imports: Imports, mangler: Mangler): (Imports, Seq[Node.Declaration]) = {
    logger.debug(s"Compiling class: ${cls.name}")
    try {
      // TODO: limit major version to 1.6+ to avoid issues with JSR/RET deprecation?
      val ctx = initContext(cls, imports, mangler)
      (if (cls.access.isAccessInterface) compileInterface(ctx) else compileClass(ctx)).map { case (ctx, decls) =>
        ctx.imports -> decls
      }
    } catch { case NonFatal(e) => throw new Exception(s"Unable to compile class ${cls.name}", e) }
  }

  protected def initContext(
    cls: Cls,
    imports: Imports,
    mangler: Mangler
  ) = Context(cls, imports, mangler)

  protected def compileInterface(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileInstInterface(ctx).map { case (ctx, instIfaceDecl) =>
      compileInterfaceDefaultMethods(ctx).map { case (ctx, ifaceDefaultDecls) =>
        ctx -> (instIfaceDecl +: ifaceDefaultDecls)
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
    val ctxAndStaticNewElements = ctx.cls.parent match {
      case None => ctx -> Nil
      case Some(superName) => ctx.staticNewExpr(superName).map { case (ctx, superStaticNewExpr) =>
        ctx -> ctx.mangler.implObjectName(superName).toIdent.withValue(superStaticNewExpr).singleSeq
      }
    }

    val ctxAndStmts = ctxAndStaticNewElements.map { case (ctx, staticNewElements) =>
      val defineStmt = "v".toIdent.assignDefine(literal(
        Some(ctx.mangler.implObjectName(ctx.cls.name).toIdent),
        staticNewElements:_*
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

  protected def compileStaticMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    val methods = methodSetManager.staticMethods(ctx.classPath, ctx.cls).methodsWithCovariantReturnDuplicates
    compileMethods(ctx, methods).map {
      case (ctx, funcs) =>

        // As a special case, we have to combine all static inits, so we'll do it with anon
        // functions if there is more than one
        val staticMethodName = ctx.mangler.implMethodName("<clinit>", "()V")
        val (staticInitFns, otherFns) = funcs.partition(_.name.name == staticMethodName)
        if (staticInitFns.length <= 1) ctx -> funcs else {
          val newStaticInitFn = staticInitFns.head.copy(
            body = Some(block(
              staticInitFns.map { staticInitFn =>
                funcType(Nil).toFuncLit(staticInitFn.body.get.statements).call().toStmt
              }
            ))
          )
          ctx -> (newStaticInitFn +: otherFns)
        }
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
    if (ctx.cls.access.isAccessInterface) ctx -> Nil else {
      // Gotta do it for all parents
      val allClasses = ctx.cls.name +: ctx.classPath.allSuperTypes(ctx.cls.name).map(_.cls.name)
      allClasses.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, fields), clsName) =>
        ctx.implTypeExpr(clsName).map { case (ctx, typPtr) =>
          ctx -> (fields :+ field(
            str = ctx.mangler.instanceRawPointerMethodName(clsName),
            typ = funcType(Nil, Some(typPtr))
          ))
        }
      }
    }
  }

  protected def compileInterfaceDefaultMethods(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileMethods(ctx, methodSetManager.instInterfaceDefaultMethods(ctx.classPath, ctx.cls).methods)
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

  protected def compileImplMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    compileImplFieldAccessors(ctx).map { case (ctx, accessorDecls) =>
      val methodSet = methodSetManager.implMethods(ctx.classPath, ctx.cls)
      compileMethods(ctx, methodSet.methods).map { case (ctx, methodDecls) =>
        compileImplRawPointerMethod(ctx).map { case (ctx, rawPointerMethod) =>
          compileImplCovariantReturnDuplicateForwarders(ctx, methodSet.covariantReturnDuplicates).map {
            case (ctx, covariantMethodDecls) =>
              ctx -> (accessorDecls ++ methodDecls ++ covariantMethodDecls :+ rawPointerMethod)
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
        cls = ctx.cls,
        method = node,
        imports = ctx.imports,
        mangler = ctx.mangler
      )
      ctx.copy(imports = newImports) -> (prevFuncs :+ fn)
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
    val allSuperTypes =
      if (ctx.cls.access.isAccessInterface) ctx.imports.classPath.allSuperAndImplementingTypes(ctx.cls.name)
      else ctx.imports.classPath.allSuperTypes(ctx.cls.name)
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
    cls: Cls,
    imports: Imports,
    mangler: Mangler
  ) extends Contextual[Context] { self =>
    override def updatedImports(mports: Imports) = copy(imports = mports)
  }
}