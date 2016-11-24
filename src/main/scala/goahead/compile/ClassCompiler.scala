package goahead.compile

import goahead.Logger
import goahead.ast.Node

trait ClassCompiler extends Logger {
  import AstDsl._
  import ClassCompiler._
  import Helpers._

  def compile(cls: Cls, imports: Imports, mangler: Mangler): (Imports, Seq[Node.Declaration]) = {
    logger.debug(s"Compiling class: ${cls.name}")
    val ctx = initContext(cls, imports, mangler)
    (if (cls.access.isAccessInterface) compileInterface(ctx) else compileClass(ctx)).leftMap { case (ctx, decls) =>
      ctx.imports -> decls
    }
  }

  protected def initContext(
    cls: Cls,
    imports: Imports,
    mangler: Mangler
  ) = Context(cls, imports, mangler)

  protected def compileInterface(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileInstInterface(ctx).leftMap { case (ctx, instIfaceDecl) =>
      ctx -> instIfaceDecl.singleSeq
    }
  }

  protected def compileClass(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStatic(ctx).leftMap { case (ctx, staticDecls) =>
      compileDispatch(ctx).leftMap { case (ctx, dispatchDecls) =>
        compileInstInterface(ctx).leftMap { case (ctx, instDecl) =>
          compileImpl(ctx).leftMap { case (ctx, implDecls) =>
            ctx -> (staticDecls ++ dispatchDecls ++ instDecl.singleSeq ++ implDecls)
          }
        }
      }
    }
  }

  protected def compileStatic(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStaticStruct(ctx).leftMap { case (ctx, staticStruct) =>
      compileStaticVar(ctx).leftMap { case (ctx, staticVar) =>
        compileStaticAccessor(ctx).leftMap { case (ctx, staticAccessor) =>
          compileStaticNew(ctx).leftMap { case (ctx, staticNew) =>
            compileStaticMethods(ctx).leftMap { case (ctx, staticMethods) =>
              ctx -> (Seq(staticStruct, staticVar, staticAccessor) ++ staticNew ++ staticMethods)
            }
          }
        }
      }
    }
  }

  protected def compileStaticStruct(ctx: Context): (Context, Node.GenericDeclaration) = {
    compileFields(ctx, clsFields(ctx).filter(_.access.isAccessStatic)).leftMap { case (ctx, fields) =>
      // Need a sync once
      val ctxAndStaticFieldOpt = if (!ctx.cls.hasStaticInit) ctx -> None else {
        ctx.withImportAlias("sync").leftMap { case (ctx, syncAlias) =>
          ctx -> Some(field("init", syncAlias.toIdent.sel("Once")))
        }
      }

      ctxAndStaticFieldOpt.leftMap { case (ctx, staticFieldOpt) =>
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
    ctx.staticInstTypeExpr(ctx.cls.name).leftMap { case (ctx, staticTyp) =>
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
      case Some(superName) => ctx.staticNewExpr(superName).leftMap { case (ctx, superStaticNewExpr) =>
        ctx -> ctx.mangler.implObjectName(superName).toIdent.withValue(superStaticNewExpr).singleSeq
      }
    }

    val ctxAndStmts = ctxAndStaticNewElements.leftMap { case (ctx, staticNewElements) =>
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

    ctxAndStmts.leftMap { case (ctx, stmts) =>
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
    compileMethods(ctx, clsMethods(ctx, forDispatch = false).filter(_.access.isAccessStatic)).leftMap {
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
    val methods = clsMethods(ctx, forDispatch = true, includeParentInstMethodsOnClass = true).
      filterNot(m => m.access.isAccessStatic || m.access.isAccessPrivate || m.name == "<init>")
    val ctxAndMethodSigs = methods.foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, methodSigs), method) =>
        signatureCompiler.buildFuncType(ctx, method, includeParamNames = false).leftMap {
          case (ctx, funcType) =>
            ctx -> (methodSigs :+ field(ctx.mangler.forwardMethodName(method.name, method.desc), funcType))
        }
    }

    // Add non-private, non-static field accessors (even of parents)
    val ctxAndAllFields = ctxAndMethodSigs.leftMap { case (ctx, methodSigs) =>
      val fields = clsFields(ctx, includeParentFields = true).
        filterNot(f => f.access.isAccessStatic || f.access.isAccessPrivate)
      fields.foldLeft(ctx -> methodSigs) { case ((ctx, fields), node) =>
        signatureCompiler.buildFieldGetterFuncType(ctx, node).leftMap { case (ctx, getterType) =>
          signatureCompiler.buildFieldSetterFuncType(ctx, node, includeParamNames = false).leftMap {
            case (ctx, setterType) =>
              ctx -> (fields ++ Seq(
                field(ctx.mangler.fieldGetterName(node.owner, node.name), getterType),
                field(ctx.mangler.fieldSetterName(node.owner, node.name), setterType)
              ))
          }
        }
      }
    }

    ctxAndAllFields.leftMap { case (ctx, fields) =>
      ctx -> interface(name = ctx.mangler.instanceInterfaceName(ctx.cls.name), fields = fields)
    }
  }

  protected def compileImpl(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileImplStruct(ctx).leftMap { case(ctx, struct) =>
      compileImplMethods(ctx).leftMap { case(ctx, methods) =>
        ctx -> (struct +: methods)
      }
    }
  }

  protected def compileImplStruct(ctx: Context): (Context, Node.Declaration) = {
    compileFields(ctx, clsFields(ctx).filterNot(_.access.isAccessStatic)).leftMap { case (ctx, fields) =>
      compileStructSuperFields(ctx).leftMap { case (ctx, superFields) =>
        ctx -> struct(ctx.mangler.implObjectName(ctx.cls.name), superFields ++ fields)
      }
    }
  }

  protected def compileImplMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    compileImplFieldAccessors(ctx).leftMap { case (ctx, accessorDecls) =>
      compileMethods(ctx, clsMethods(ctx, forDispatch = false).filterNot(_.access.isAccessStatic)).leftMap {
        case (ctx, methodDecls) => ctx -> (accessorDecls ++ methodDecls)
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
        ctxAndSuper.leftMap { case (ctx, superType) =>
          ctx -> (fields :+ superType.namelessField)
        }
    }
    // Also need dispatch iface
    ctxAndSuperFields.leftMap { case (ctx, superFields) =>
      ctx -> (superFields :+ field("_dispatch", ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent))
    }
  }

  protected def compileFields(ctx: Context, fields: Seq[Field]): (Context, Seq[Node.Field]) = {
    fields.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, prevFields), node) =>
      logger.debug(s"Compiling field ${ctx.cls.name}.${node.name}")
      ctx.typeToGoType(IType.getType(node.desc)).leftMap { case (ctx, typ) =>
        ctx -> (prevFields :+ field(ctx.mangler.fieldName(ctx.cls.name, node.name), typ))
      }
    }
  }

  protected def compileImplFieldAccessors(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    // All non-static fields get getters and setters, even private fields
    val fields = clsFields(ctx).filterNot(_.access.isAccessStatic)
    fields.foldLeft(ctx -> Seq.empty[Node.FunctionDeclaration]) { case ((ctx, decls), node) =>
      ctx.implTypeExpr(ctx.cls.name).leftMap { case (ctx, thisType) =>
        signatureCompiler.buildFieldGetterFuncType(ctx, node).leftMap { case (ctx, getterType) =>
          signatureCompiler.buildFieldSetterFuncType(ctx, node, includeParamNames = true).leftMap {
            case (ctx, setterType) => ctx -> (decls ++ Seq(
              funcDecl(
                rec = Some(field("this", thisType)),
                name = ctx.mangler.fieldGetterName(node.owner, node.name),
                funcType = getterType,
                stmts = "this".toIdent.sel(ctx.mangler.fieldName(ctx.cls.name, node.name)).ret.singleSeq
              ),
              funcDecl(
                rec = Some(field("this", thisType)),
                name = ctx.mangler.fieldSetterName(node.owner, node.name),
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
    compileDispatchInterface(ctx).leftMap { case (ctx, ifaceDecl) =>
      compileDispatchInit(ctx).leftMap { case (ctx, initDecl) =>
        compileDispatchForwardMethods(ctx).leftMap { case (ctx, funcDecls) =>
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
      ctx.importQualifiedName(superName, ctx.mangler.dispatchInterfaceName(superName)).leftMap {
        case (ctx, superDispatchInterfaceRef) => ctx -> (fields :+ superDispatchInterfaceRef.namelessField)
      }
    }
    ctxAndSuperDispatchRefs.leftMap { case (ctx, superDispatchRefs) =>
      val dispatchMethodNodes = clsMethods(ctx, forDispatch = true).filter { method =>
        // No static
        if (method.access.isAccessStatic) false else {
          // No methods that are also defined in a super interface
          !allSuperTypes.exists(_.cls.methods.exists(m => m.name == method.name && m.desc == method.desc))
        }
      }
      val ctxAndDispatchMethods = dispatchMethodNodes.foldLeft(ctx -> Seq.empty[Node.Field]) {
        case ((ctx, fields), methodNode) =>
          signatureCompiler.buildFuncType(ctx, methodNode, includeParamNames = false).leftMap {
            case (ctx, funcType) =>
              ctx -> (fields :+ field(ctx.mangler.implMethodName(methodNode.name, methodNode.desc), funcType))
          }
      }

      ctxAndDispatchMethods.leftMap { case (ctx, dispatchMethods) =>
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
          ctx.implTypeExpr(superName).leftMap { case (ctx, superImplType) =>
            ctx -> Some("this".toIdent.sel(ctx.mangler.implObjectName(superName)).
              sel(ctx.mangler.dispatchInitMethodName(superName)).
              call("v".toIdent.singleSeq).toStmt)
          }
      }

      ctxAndSuperStmtOpt.leftMap { case (ctx, superStmtOpt) =>
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
      dispatchMethodsForForwarding(ctx).foldLeft(ctx -> Seq.empty[Node.Declaration]) { case ((ctx, methods), method) =>
        val call = "this".toIdent.sel("_dispatch").sel(ctx.mangler.implMethodName(method.name, method.desc)).call(
            IType.getArgumentTypes(method.desc).zipWithIndex.map("var" + _._2).map(_.toIdent)
          )
        val stmt = if (IType.getReturnType(method.desc) == IType.VoidType) call.toStmt else call.ret
        signatureCompiler.buildFuncDecl(
          ctx = ctx,
          method = method,
          stmts = stmt.singleSeq,
          nameOverride = Some(ctx.mangler.forwardMethodName(method.name, method.desc))
        ).leftMap { case (ctx, funcDecl) =>
          ctx -> (methods :+ funcDecl)
        }
      }
    }
  }

  protected def dispatchMethodsForForwarding(
    ctx: Context,
    // Only applies for non-interface class nodes
    alsoExcludeMethodsInSuperInterfaces: Boolean = false
  ): Seq[Method] = {
    // We build dispatch methods ONLY for methods we declare the first time
    val superTypes =
      if (ctx.cls.access.isAccessInterface) ctx.imports.classPath.allInterfaceTypes(ctx.cls.name)
      else if (alsoExcludeMethodsInSuperInterfaces) ctx.imports.classPath.allSuperAndImplementingTypes(ctx.cls.name)
      else ctx.imports.classPath.allSuperTypes(ctx.cls.name)
    clsMethods(ctx, forDispatch = true).filter { method =>
      // No static
      if (method.access.isAccessStatic) false else {
        // No methods that are also defined in a super interface
        !superTypes.exists(_.cls.methods.exists(m => m.name == method.name && m.desc == method.desc))
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

  // TODO: slow
  protected def clsMethods(
    ctx: Context,
    forDispatch: Boolean,
    // Does not apply to interface
    includeParentInstMethodsOnClass: Boolean = false
  ): Seq[Method] = {
    if (!includeParentInstMethodsOnClass || ctx.cls.access.isAccessInterface) {
      ctx.cls.methods.sortBy(m => m.name -> m.desc)
    } else {
      val methods = ctx.cls.methods ++
        ctx.imports.classPath.allSuperTypes(ctx.cls.name).flatMap(_.cls.methods).filterNot(_.access.isAccessStatic)
      // Make them distinct by name and desc
      methods.groupBy(m => m.name -> m.desc).map(_._2.head).toSeq.sortBy(m => m.name -> m.desc)
    }
  }

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