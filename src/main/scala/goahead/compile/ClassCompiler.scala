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
    compileStatic(initContext(cls, imports, mangler)).leftMap { case (ctx, staticDecls) =>
      compileInst(ctx).leftMap { case (ctx, instDecls) =>
        ctx.imports -> (staticDecls ++ instDecls)
      }
    }
  }

  protected def initContext(
    cls: ClassNode,
    imports: Imports,
    mangler: Mangler
  ) = Context(cls, imports, mangler)

  protected def compileStatic(ctx: Context): (Context, Seq[Node.Declaration]) = {
    compileStaticStruct(ctx).leftMap { case (ctx, staticStruct) =>
      compileStaticVar(ctx).leftMap { case (ctx, staticVar) =>
        compileStaticAccessor(ctx).leftMap { case (ctx, staticAccessor) =>
          compileStaticNew(ctx).leftMap { case (ctx, staticNew) =>
            compileStaticMethods(ctx).leftMap { case (ctx, staticMethods) =>
              ctx -> (Seq(staticStruct, staticVar, staticAccessor, staticNew) ++ staticMethods)
            }
          }
        }
      }
    }
  }

  protected def compileStaticStruct(ctx: Context): (Context, Node.GenericDeclaration) = {
    compileFields(ctx, ctx.cls.fieldNodes.filter(_.access.isAccessStatic)).leftMap { case (ctx, fields) =>
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

  protected def compileStaticNew(ctx: Context): (Context, Node.FunctionDeclaration) = {
    ctx.staticNewExpr(ctx.cls.superName).leftMap { case (ctx, superStaticNewExpr) =>
      ctx -> funcDecl(
        rec = Some("this" -> ctx.mangler.staticObjectName(ctx.cls.name).toIdent.star),
        name = "New",
        params = Nil,
        results = Some(ctx.mangler.instanceObjectName(ctx.cls.name).toIdent.star),
        stmts = Seq {
          val structLit = literal(
            Some(ctx.mangler.instanceObjectName(ctx.cls.name).toIdent),
            ctx.mangler.instanceObjectName(ctx.cls.superName).toIdent.withValue(superStaticNewExpr)
          )
          structLit.unary(Node.Token.And).ret
        }
      )
    }
  }

  protected def compileStaticMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    compileMethods(ctx, ctx.cls.methodNodes.filter(_.access.isAccessStatic)).leftMap { case (ctx, funcs) =>

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
    compileFields(ctx, ctx.cls.fieldNodes.filterNot(_.access.isAccessStatic)).leftMap { case (ctx, fields) =>
      // Super classes are embedded
      ctx.typeToGoType(IType.getObjectType(ctx.cls.superName)).leftMap { case (ctx, superType) =>
        ctx -> struct(
          ctx.mangler.instanceObjectName(ctx.cls.name),
          superType.namelessField +: fields
        )
      }
    }
  }

  protected def compileInstMethods(ctx: Context): (Context, Seq[Node.FunctionDeclaration]) = {
    compileMethods(ctx, ctx.cls.methodNodes.filterNot(_.access.isAccessStatic))
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