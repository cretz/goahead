package goahead.compile

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.tree.{ClassNode, FieldNode, MethodNode}

trait ClassCompiler extends Logger {
  import AstDsl._
  import ClassCompiler._
  import Helpers._

  def compile(cls: ClassNode, imports: Imports, mangler: Mangler): (Imports, Seq[Node.Declaration]) = {
    logger.debug(s"Compiling class: ${cls.name}")
    val ctx = initContext(cls, imports, mangler)
    (if (cls.access.isAccessInterface) compileInterface(ctx) else compileClass(ctx)).leftMap { case (ctx, decls) =>
      ctx.imports -> decls
    }
  }

  protected def initContext(
    cls: ClassNode,
    imports: Imports,
    mangler: Mangler
  ) = Context(cls, imports, mangler)

  protected def compileInterface(ctx: Context): (Context, Seq[Node.Declaration]) = {
    // We need a dispatch
    compileDispatch(ctx).leftMap { case (ctx, dispatchDecls) =>
      compileInterfaceStruct(ctx).leftMap { case (ctx, ifaceStruct) =>
        ctx -> (dispatchDecls :+ ifaceStruct)
      }
    }
  }

  protected def compileInterfaceStruct(ctx: Context): (Context, Node.Declaration) = {
    // Embed the dispatch and all the forwarding methods
    val ctxAndMethodSigs = dispatchMethodsForForwarding(ctx).foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, methodSigs), method) =>
        signatureCompiler.buildFuncType(ctx, method, includeParamNames = false).leftMap { case (ctx, funcType) =>
          ctx -> (methodSigs :+ field(ctx.mangler.forwardMethodName(method.name, method.desc), funcType))
        }
    }
    ctxAndMethodSigs.leftMap { case (ctx, methodSigs) =>
      ctx -> interface(
        name = ctx.mangler.instanceObjectName(ctx.cls.name),
        fields = ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent.namelessField +: methodSigs
      )
    }
  }

  protected def compileClass(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStatic(ctx).leftMap { case (ctx, staticDecls) =>
      compileDispatch(ctx).leftMap { case (ctx, dispatchDecls) =>
        compileInst(ctx).leftMap { case (ctx, instDecls) =>
          ctx -> (staticDecls ++ dispatchDecls ++ instDecls)
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
    compileFields(ctx, fieldNodes(ctx).filter(_.access.isAccessStatic)).leftMap { case (ctx, fields) =>
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
        staticVarName.sel(ctx.mangler.methodName("<clinit>", "()V")).singleSeq
      ).toStmt
    }
    ctx.staticInstTypeExpr(ctx.cls.name).leftMap { case (ctx, staticTyp) =>
      ctx -> funcDecl(
        rec = None,
        name = ctx.mangler.staticAccessorName(ctx.cls.name),
        params = Nil,
        results = Some(staticTyp.star),
        stmts = initStmtOpt.toSeq ++ staticVarName.unary(Node.Token.And).ret.singleSeq
      )
    }
  }

  protected def compileStaticNew(ctx: Context): (Context, Option[Node.FunctionDeclaration]) = {
    val ctxAndStaticNewElements = Option(ctx.cls.superName) match {
      case None => ctx -> Nil
      case Some(superName) => ctx.staticNewExpr(superName).leftMap { case (ctx, superStaticNewExpr) =>
        ctx -> ctx.mangler.instanceObjectName(superName).toIdent.withValue(superStaticNewExpr).singleSeq
      }
    }

    val ctxAndStmts = ctxAndStaticNewElements.leftMap { case (ctx, staticNewElements) =>
      val defineStmt = "v".toIdent.assignDefine(literal(
        Some(ctx.mangler.instanceObjectName(ctx.cls.name).toIdent),
        staticNewElements:_*
      ).unary(Node.Token.And))
      val initDispatchStmt = "v".toIdent.sel(
        ctx.mangler.distpatchInitMethodName(ctx.cls.name)
      ).call("v".toIdent.singleSeq).toStmt
      val retStmt = "v".toIdent.ret
      ctx -> Seq(defineStmt, initDispatchStmt, retStmt)
    }

    ctxAndStmts.leftMap { case (ctx, stmts) =>
      ctx -> Some(funcDecl(
        rec = Some("this" -> ctx.mangler.staticObjectName(ctx.cls.name).toIdent.star),
        name = "New",
        params = Nil,
        results = Some(ctx.mangler.instanceObjectName(ctx.cls.name).toIdent.star),
        stmts = stmts
      ))
    }
  }

  protected def compileStaticMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    compileMethods(ctx, methodNodes(ctx, forDispatch = false).filter(_.access.isAccessStatic)).leftMap {
      case (ctx, funcs) =>

        // As a special case, we have to combine all static inits, so we'll do it with anon
        // functions if there is more than one
        val staticMethodName = ctx.mangler.methodName("<clinit>", "()V")
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

  protected def compileInst(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileInstStruct(ctx).leftMap { case(ctx, struct) =>
      compileInstMethods(ctx).leftMap { case(ctx, methods) =>
        ctx -> (struct +: methods)
      }
    }
  }

  protected def compileInstStruct(ctx: Context): (Context, Node.Declaration) = {
    compileFields(ctx, fieldNodes(ctx).filterNot(_.access.isAccessStatic)).leftMap { case (ctx, fields) =>
      compileStructSuperFields(ctx).leftMap { case (ctx, superFields) =>
        ctx -> struct(ctx.mangler.instanceObjectName(ctx.cls.name), superFields ++ fields)
      }
    }
  }

  protected def compileInstMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    compileMethods(ctx, methodNodes(ctx, forDispatch = false).filterNot(_.access.isAccessStatic))
  }

  protected def compileStructSuperFields(ctx: Context): (Context, Seq[Node.Field]) = {
    val superNames =
      if (ctx.cls.access.isAccessInterface) ctx.cls.interfaceNames else Option(ctx.cls.superName).toSeq
    val ctxAndSuperFields = superNames.foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, fields), sup) =>
        // Interfaces should not be considered to have java/lang/Object as a super name
        if (ctx.cls.access.isAccessInterface && sup == "java/lang/Object") ctx -> fields
        else  ctx.typeToGoType(IType.getObjectType(sup)).leftMap { case (ctx, superType) =>
          ctx -> (fields :+ superType.namelessField)
        }
    }
    // Also need dispatch iface
    ctxAndSuperFields.leftMap { case (ctx, superFields) =>
      ctx -> (superFields :+ field("_dispatch", ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent))
    }
  }

  protected def compileFields(ctx: Context, fields: Seq[FieldNode]): (Context, Seq[Node.Field]) = {
    fields.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, prevFields), node) =>
      ctx.typeToGoType(IType.getType(node.desc)).leftMap { case (ctx, typ) =>
        ctx -> (prevFields :+ field(ctx.mangler.fieldName(ctx.cls.name, node.name), typ))
      }
    }
  }

  protected def compileMethods(ctx: Context, methods: Seq[MethodNode]): (Context, Seq[Node.FunctionDeclaration]) = {
    methods.foldLeft(ctx -> Seq.empty[Node.FunctionDeclaration]) { case ((ctx, prevFuncs), node) =>
      val (newImports, fn) = methodCompiler.compile(
        cls = ctx.cls,
        method = Method(node),
        imports = ctx.imports,
        mangler = ctx.mangler
      )
      ctx.copy(imports = newImports) -> (prevFuncs :+ fn)
    }
  }

  //protected def methodCompiler: MethodCompiler = MethodCompiler
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
      if (ctx.cls.access.isAccessInterface) ctx.cls.interfaceNames else Option(ctx.cls.superName).toSeq
    val ctxAndSuperDispatchRefs = superNames.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, fields), superName) =>
      ctx.importQualifiedName(superName, ctx.mangler.dispatchInterfaceName(superName)).leftMap {
        case (ctx, superDispatchInterfaceRef) => ctx -> (fields :+ superDispatchInterfaceRef.namelessField)
      }
    }
    ctxAndSuperDispatchRefs.leftMap { case (ctx, superDispatchRefs) =>
      val dispatchMethodNodes = methodNodes(ctx, forDispatch = true).filter { method =>
        // No static
        if (method.access.isAccessStatic) false else {
          // No methods that are also defined in a super interface
          !allSuperTypes.exists(_.methods.exists(m => m.name == method.name && m.desc == method.desc))
        }
      }
      val ctxAndDispatchMethods = dispatchMethodNodes.foldLeft(ctx -> Seq.empty[Node.Field]) {
        case ((ctx, fields), methodNode) =>
          signatureCompiler.buildFuncType(ctx, Method(methodNode), includeParamNames = false).leftMap {
            case (ctx, funcType) =>
              ctx -> (fields :+ field(ctx.mangler.methodName(methodNode.name, methodNode.desc), funcType))
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
      val superStmtOpt = Option(ctx.cls.superName).map { superName =>
        "this".toIdent.sel(ctx.mangler.instanceObjectName(superName)).
          sel(ctx.mangler.distpatchInitMethodName(superName)).call("v".toIdent.singleSeq).toStmt
      }
      val setDispatchStmt = "this".toIdent.sel("_dispatch").assignExisting("v".toIdent)
      ctx -> Some(funcDecl(
        rec = Some("this" -> ctx.mangler.instanceObjectName(ctx.cls.name).toIdent.star),
        name = ctx.mangler.distpatchInitMethodName(ctx.cls.name),
        params = Seq("v" -> ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent),
        results = None,
        stmts = superStmtOpt.toSeq :+ setDispatchStmt
      ))
    }
  }

  protected def compileDispatchForwardMethods(ctx: Context): (Context, Seq[Node.Declaration]) = {
    // No dispatch methods for interfaces
    if (ctx.cls.access.isAccessInterface) ctx -> Nil else {
      dispatchMethodsForForwarding(ctx).foldLeft(ctx -> Seq.empty[Node.Declaration]) { case ((ctx, methods), method) =>
        val call = "this".toIdent.sel("_dispatch").sel(ctx.mangler.methodName(method.name, method.desc)).call(
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

  protected def dispatchMethodsForForwarding(ctx: Context): Seq[Method] = {
    // We build dispatch methods ONLY for methods we declare the first time
    val superTypes =
      if (ctx.cls.access.isAccessInterface) ctx.imports.classPath.allInterfaceTypes(ctx.cls.name)
      else ctx.imports.classPath.allSuperTypes(ctx.cls.name)
    val dispatchMethodNodes = methodNodes(ctx, forDispatch = true).filter { method =>
      // No static
      if (method.access.isAccessStatic) false else {
        // No methods that are also defined in a super interface
        !superTypes.exists(_.methods.exists(m => m.name == method.name && m.desc == method.desc))
      }
    }

    dispatchMethodNodes.map(Method.apply)
  }

  // TODO: slow
  protected def fieldNodes(ctx: Context): Seq[FieldNode] = ctx.cls.fieldNodes.sortBy(_.name)

  // TODO: slow
  protected def methodNodes(ctx: Context, forDispatch: Boolean): Seq[MethodNode] =
    ctx.cls.methodNodes.sortBy(m => m.name -> m.desc)

  protected def signatureCompiler: SignatureCompiler = SignatureCompiler
}

object ClassCompiler extends ClassCompiler {
  case class Context(
    cls: ClassNode,
    imports: Imports,
    mangler: Mangler
  ) extends Contextual[Context] { self =>
    override def updatedImports(mports: Imports) = copy(imports = mports)
  }
}