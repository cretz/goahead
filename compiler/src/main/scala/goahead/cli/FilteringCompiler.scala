package goahead.cli

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
        val ctxAndFields = conf.classManips.goFields(ctx.cls).foldLeft(ctx -> fields) {
          case ((ctx, prevFields), (name, (Some(pkg), goType))) =>
            ctx.withImportAlias(pkg).map { case (ctx, alias) =>
              ctx -> (prevFields :+ field(name, alias.toIdent.sel(goType)))
            }
          case ((ctx, prevFields), (name, (None, goType))) =>
            ctx -> (prevFields :+ field(name, goType.toIdent))
        }
        forwarders.get(ctx.cls.name).flatMap(_.filter(_.instance == !static)).foldLeft(ctxAndFields) {
          case ((ctx, fields), fwd) => ctx -> (fields :+ field("Fwd_", fwd.goStruct.toIdent))
        }
      }
    }

    override protected val methodSetManager = new MethodSetManager.Filtered() {
      override def filter(method: Method, forImpl: Boolean) = {
        if (conf.classManips.isExcluded(method, FilteringCompiler.this)) {
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
