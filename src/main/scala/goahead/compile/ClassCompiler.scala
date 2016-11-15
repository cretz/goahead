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
    // Simple interface that embeds the dispatch and nothing more
    ctx -> interface(
      name = ctx.mangler.instanceObjectName(ctx.cls.name),
      fields = ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent.namelessField.singleSeq
    )
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
    compileMethods(ctx, methodNodes(ctx).filter(_.access.isAccessStatic)).leftMap { case (ctx, funcs) =>

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
    compileMethods(ctx, methodNodes(ctx).filterNot(_.access.isAccessStatic))
  }

  protected def compileStructSuperFields(ctx: Context): (Context, Seq[Node.Field]) = {
    val ctxAndSuperFields = (ctx.cls.interfaceNames ++ Option(ctx.cls.superName)).foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, fields), sup) =>
        // Interfaces should not be considered to have java/lang/Object as a super name
        if (ctx.cls.access.isAccessInterface && sup == "java/lang/Object") ctx -> fields
        else  ctx.typeToGoType(IType.getObjectType(sup)).leftMap { case (ctx, superType) =>
          ctx -> (fields :+ superType.namelessField)
        }
    }
    // Also need dispatch iface
    ctxAndSuperFields.leftMap { case (ctx, superFields) =>
      ctx -> (superFields :+ ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent.namelessField)
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

  protected def methodCompiler: MethodCompiler = MethodCompiler

  protected def compileDispatch(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileDispatchInterface(ctx).leftMap { case (ctx, ifaceDecl) =>
      compileDispatchInit(ctx).leftMap { case (ctx, initDecl) =>
        ctx -> (Seq(ifaceDecl) ++ initDecl)
      }
    }
  }

  protected def compileDispatchInterface(ctx: Context): (Context, Node.Declaration) = {
    val allSuperTypes = ctx.imports.classPath.allSuperAndImplementingTypes(ctx.cls.name)
    // Interfaces use parent interfaces, classes just use the parent class if present
    val superNames =
      if (ctx.cls.access.isAccessInterface) ctx.cls.interfaceNames else Option(ctx.cls.superName).toSeq
    val ctxAndSuperDispatchRefs = superNames.foldLeft(ctx -> Seq.empty[Node.Field]) { case ((ctx, fields), superName) =>
      ctx.importQualifiedName(superName, ctx.mangler.dispatchInterfaceName(superName)).leftMap {
        case (ctx, superDispatchInterfaceRef) => ctx -> (fields :+ superDispatchInterfaceRef.namelessField)
      }
    }
    ctxAndSuperDispatchRefs.leftMap { case (ctx, superDispatchRefs) =>
      val dispatchMethodNodes = methodNodes(ctx).filter { method =>
        // No static, no init
        if (method.access.isAccessStatic || method.name == "<init>") false else {
          // No methods that are also defined in a super interface
          !allSuperTypes.exists(_.methods.exists(m => m.name == method.name && m.desc == method.desc))
        }
      }
      val ctxAndDispatchMethods = dispatchMethodNodes.foldLeft(ctx -> Seq.empty[Node.Field]) {
        case ((ctx, fields), methodNode) => compileInterfaceFunc(ctx, methodNode).leftMap { case (ctx, field) =>
          ctx -> (fields :+ field)
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
      val setDispatchStmt =
        "this".toIdent.sel(ctx.mangler.dispatchInterfaceName(ctx.cls.name)).assignExisting("v".toIdent)
      ctx -> Some(funcDecl(
        rec = Some("this" -> ctx.mangler.instanceObjectName(ctx.cls.name).toIdent.star),
        name = ctx.mangler.distpatchInitMethodName(ctx.cls.name),
        params = Seq("v" -> ctx.mangler.dispatchInterfaceName(ctx.cls.name).toIdent),
        results = None,
        stmts = superStmtOpt.toSeq :+ setDispatchStmt
      ))
    }
  }

  protected def compileInterfaceFunc(ctx: Context, m: MethodNode): (Context, Node.Field) = {
    val ctxWithParams = IType.getArgumentTypes(m.desc).foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, params), argType) => ctx.typeToGoType(argType).leftMap { case (ctx, typ) =>
        ctx -> (params :+ typ.namelessField)
      }
    }

    ctxWithParams.leftMap { case (ctx, params) =>
      val ctxWithResultTypOpt = IType.getReturnType(m.desc) match {
        case IType.VoidType => ctx -> None
        case retTyp => ctx.typeToGoType(retTyp).leftMap { case (ctx, typ) => ctx -> Some(typ) }
      }

      ctxWithResultTypOpt.leftMap { case (ctx, resultTypOpt) =>
        ctx -> field(ctx.mangler.dispatchMethodName(m.name, m.desc), funcTypeWithFields(params, resultTypOpt))
      }
    }
  }

  protected def fieldNodes(ctx: Context): Seq[FieldNode] = ctx.cls.fieldNodes

  protected def methodNodes(ctx: Context): Seq[MethodNode] = ctx.cls.methodNodes
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