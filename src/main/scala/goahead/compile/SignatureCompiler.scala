package goahead.compile

import goahead.ast.Node

trait SignatureCompiler {
  import AstDsl._, Helpers._

  def buildFuncDecl[T <: Contextual[T]](
    ctx: T,
    method: Method,
    stmts: Seq[Node.Statement],
    nameOverride: Option[String] = None
  ): (T, Node.FunctionDeclaration) = {
    val ctxAndNonPointerRecTyp =
      if (method.access.isAccessStatic) ctx.staticInstTypeExpr(ctx.cls.name)
      else ctx.instTypeExpr(ctx.cls.name)

    ctxAndNonPointerRecTyp.leftMap { case (ctx, nonPointerRecTyp) =>

      buildFuncType(ctx, method, includeParamNames = true).leftMap { case (ctx, funcType) =>

        ctx -> funcDecl(
          rec = Some(field("this", nonPointerRecTyp.star)),
          name = nameOverride.getOrElse(ctx.mangler.methodName(method.name, method.desc)),
          funcType = funcType,
          stmts = stmts
        )
      }
    }
  }

  def buildFuncType[T <: Contextual[T]](
    ctx: T,
    method: Method,
    includeParamNames: Boolean
  ): (T, Node.FunctionType) = {
    // 0 is "this" in non-static
    val indexAdd = if (method.access.isAccessStatic) 0 else 1

    val ctxWithParams =
      IType.getArgumentTypes(method.desc).zipWithIndex.foldLeft(ctx -> Seq.empty[Node.Field]) {
        case ((ctx, params), (argType, argIndex)) =>
          ctx.typeToGoType(argType).leftMap { case (ctx, typ) =>
            val param =
              if (includeParamNames) field("var" + (argIndex + indexAdd), typ)
              else typ.namelessField
            ctx -> (params :+ param)
          }
      }

    ctxWithParams.leftMap { case (ctx, params) =>
      val ctxWithResultTypOpt = IType.getReturnType(method.desc) match {
        case IType.VoidType => ctx -> None
        case retTyp => ctx.typeToGoType(retTyp).leftMap { case (ctx, typ) => ctx -> Some(typ) }
      }

      ctxWithResultTypOpt.leftMap { case (ctx, resultTypOpt) => ctx -> funcTypeWithFields(params, resultTypOpt) }
    }
  }
}

object SignatureCompiler extends SignatureCompiler