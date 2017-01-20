package goahead.cli

import goahead.compile.ClassCompiler.Context
import goahead.compile._
import goahead.compile.ClassPath.ClassDetails

class FilteringCompiler(
  val conf: CompileConfig,
  val classPath: ClassPath,
  val includedClasses: Seq[ClassDetails],
  val forwarders: Map[String, Seq[Forwarders]]
) extends GoAheadCompiler {
  import AstDsl._
  import Helpers._

  override val classCompiler = new ClassCompiler {

    def methodStr(m: Method) = s"$m (impl name ${conf.manglerInst.implMethodName(m)})"

    override protected def needsStaticSyncOnceField(ctx: Context) =
      // Also needs a sync once field if there is a forwarder impl field
      super.needsStaticSyncOnceField(ctx) ||
        forwarders.get(ctx.cls.name).exists(_.exists(v => !v.instance && v.hasImplField))

    override protected def compileStaticInitOnceFunc(ctx: Context) = {
      // Inject the impl assignment
      forwarders.get(ctx.cls.name).flatMap(_.find(v => !v.instance && v.hasImplField)) match {
        case None =>
          super.compileStaticInitOnceFunc(ctx)
        case Some(fwd) =>
          super.compileStaticInitOnceFunc(ctx).map { case (ctx, funcOpt) =>
            // We need a func lit to add the impl field set
            val staticVar = ctx.mangler.staticVarName(ctx.cls.name).toIdent
            ctx -> Some(funcType(Nil).toFuncLit(
              Seq(staticVar.sel(fwd.forwardFieldName).sel("impl").assignExisting(staticVar.addressOf)) ++
                funcOpt.map(_.call().toStmt)
            ))
          }
      }
    }

    override protected def compileStaticNew(ctx: Context) = {
      // Inject the impl assignment
      forwarders.get(ctx.cls.name).flatMap(_.find(v => v.instance && v.hasImplField)) match {
        case None =>
          super.compileStaticNew(ctx)
        case Some(fwd) =>
          // Inject the field set as the second-to-last statement
          super.compileStaticNew(ctx).map { case (ctx, funcDeclOpt) =>
            ctx -> funcDeclOpt.map { funcDecl =>
              funcDecl.copy(body = funcDecl.body.map { block =>
                block.copy(statements = block.statements.init ++ Seq(
                  "v".toIdent.sel(fwd.forwardFieldName).sel("impl").assignExisting("v".toIdent),
                  block.statements.last
                ))
              })
            }
          }
      }
    }

    override protected def clsFields(
      ctx: ClassCompiler.Context,
      forImpl: Boolean,
      includeParentFields: Boolean = false
    ): Seq[Field] =
      super.clsFields(ctx, forImpl, includeParentFields).filter { field =>
        if (conf.classManips.isExcluded(field, FilteringCompiler.this)) {
          logger.debug(s"Excluding field $field by rule")
          false
        } else true
      }

    override protected def compileFields(ctx: ClassCompiler.Context, fields: Seq[Field], static: Boolean) = {
      super.compileFields(ctx, fields, static).map { case (ctx, fields) =>
        conf.classManips.goFields(ctx).map { case (ctx, goFields) =>
          val forwarderFieldOpt = forwarders.get(ctx.cls.name).flatMap(_.find(_.instance == !static)).map { fwd =>
            field(fwd.forwardFieldName, fwd.goStruct.toIdent)
          }
          ctx -> (fields ++ goFields ++ forwarderFieldOpt)
        }
      }
    }

    override protected val methodSetManager = new MethodSetManager.Filtered() {
      override def filter(method: Method, forImpl: Boolean) = {
        if (conf.classManips.isExcluded(method, FilteringCompiler.this, forImpl)) {
          logger.debug(s"Excluding method $method by rule")
          false
        } else true
      }
    }

    override val methodCompiler = new MethodCompiler {
      override def compile(conf: Config, cls: Cls, method: Method, mports: Imports, mangler: Mangler) = {
        FilteringCompiler.this.conf.classManips.transform(
          cmp = FilteringCompiler.this,
          method = method,
          ctx = initContext(conf, cls, method, mports, mangler, Nil)
        ) match {
          case Some((ctx, stmts)) => buildFuncDecl(ctx, stmts).map { case (ctx, funcDecl) => ctx.imports -> funcDecl }
          case None => super.compile(conf, cls, method, mports, mangler)
        }
      }
    }
  }
}
