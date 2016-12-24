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

    ctxAndRecTyp.map { case (ctx, recTyp) =>

      buildFuncType(ctx, method, includeParamNames = true).map { case (ctx, funcType) =>

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
      // Signature polymorphic should be varargs empty interface
      if (method.isSignaturePolymorphic) {
        ctx -> Seq(field("var0", emptyInterface.ellipsis))
      } else {
        IType.getArgumentTypes(method.desc).zipWithIndex.foldLeft(ctx -> Seq.empty[Node.Field]) {
          case ((ctx, params), (argType, argIndex)) =>
            ctx.typeToGoType(argType).map { case (ctx, typ) =>
              val param = if (includeParamNames) field(s"var$argIndex", typ) else typ.namelessField
              ctx -> (params :+ param)
            }
        }
      }

    ctxWithParams.map { case (ctx, params) =>
      val ctxWithResultTypOpt =
        if (method.isSignaturePolymorphic) ctx -> Some(emptyInterface)
        else IType.getReturnType(method.desc) match {
          case IType.VoidType => ctx -> None
          case retTyp => ctx.typeToGoType(retTyp).map { case (ctx, typ) => ctx -> Some(typ) }
        }

      ctxWithResultTypOpt.map { case (ctx, resultTypOpt) => ctx -> funcTypeWithFields(params, resultTypOpt) }
    }
  }

  def buildFieldGetterFuncType[T <: Contextual[T]](ctx: T, field: Field): (T, Node.FunctionType) = {
    ctx.typeToGoType(IType.getType(field.desc)).map { case (ctx, fieldType) =>
      ctx -> funcType(params = Nil, result = Some(fieldType))
    }
  }

  def buildFieldSetterFuncType[T <: Contextual[T]](
    ctx: T,
    field: Field,
    includeParamNames: Boolean
  ): (T, Node.FunctionType) = {
    ctx.typeToGoType(IType.getType(field.desc)).map { case (ctx, fieldType) =>
      ctx -> funcType(params = Seq("v" -> fieldType), result = None)
    }
  }
}

object SignatureCompiler extends SignatureCompiler