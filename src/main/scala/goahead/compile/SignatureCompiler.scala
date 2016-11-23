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
    val ctxAndRecTyp =
      if (method.access.isAccessStatic) ctx.staticInstTypeExpr(ctx.cls.name)
      else ctx.implTypeExpr(ctx.cls.name)

    ctxAndRecTyp.leftMap { case (ctx, recTyp) =>

      buildFuncType(ctx, method, includeParamNames = true).leftMap { case (ctx, funcType) =>

        ctx -> funcDecl(
          rec = Some(field("this", recTyp)),
          name = nameOverride.getOrElse(ctx.mangler.implMethodName(method.name, method.desc)),
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
    val ctxWithParams =
      IType.getArgumentTypes(method.desc).zipWithIndex.foldLeft(ctx -> Seq.empty[Node.Field]) {
        case ((ctx, params), (argType, argIndex)) =>
          ctx.typeToGoType(argType).leftMap { case (ctx, typ) =>
            val param = if (includeParamNames) field(s"var$argIndex", typ) else typ.namelessField
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

  def buildFieldGetterFuncType[T <: Contextual[T]](ctx: T, field: Field): (T, Node.FunctionType) = {
    ctx.typeToGoType(IType.getType(field.desc)).leftMap { case (ctx, fieldType) =>
      ctx -> funcType(params = Nil, result = Some(fieldType))
    }
  }

  def buildFieldSetterFuncType[T <: Contextual[T]](
    ctx: T,
    field: Field,
    includeParamNames: Boolean
  ): (T, Node.FunctionType) = {
    ctx.typeToGoType(IType.getType(field.desc)).leftMap { case (ctx, fieldType) =>
      ctx -> funcType(params = Seq("v" -> fieldType), result = None)
    }
  }
}

object SignatureCompiler extends SignatureCompiler