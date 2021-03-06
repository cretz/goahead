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
          name = nameOverride.getOrElse(ctx.mangler.implMethodName(method)),
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
    // Signature polymorphic should be varargs empty interface, but still return the same value
    if (method.isSignaturePolymorphic) {
      ctx.typeToGoType(ObjectType).map { case (ctx, objTyp) =>
        val ctxWithResultTypOpt = method.returnType match {
          case IType.VoidType => ctx -> None
          case retTyp => ctx.typeToGoType(retTyp).map { case (ctx, typ) => ctx -> Some(typ) }
        }
        ctxWithResultTypOpt.map { case (ctx, resultTypOpt) =>
          ctx -> funcTypeWithFields(
            params = Seq(
              if (includeParamNames) field("var0", emptyInterface.ellipsis)
              else emptyInterface.ellipsis.namelessField
            ),
            result = resultTypOpt
          )
        }
      }
    } else buildFuncType(ctx, method.desc, includeParamNames)
  }

  def buildFuncType[T <: Contextual[T]](
    ctx: T,
    methodDesc: String,
    includeParamNames: Boolean
  ): (T, Node.FunctionType) = {
    val argTypes = IType.getArgumentTypes(methodDesc)
    buildFuncType(ctx, argTypes, IType.getReturnType(methodDesc),
      if (includeParamNames) Some(argTypes.indices.map(i => s"var$i")) else None)
  }

  def buildFuncType[T <: Contextual[T]](
    ctx: T,
    argTypes: Seq[IType],
    retType: IType,
    paramNames: Option[Seq[String]]
  ): (T, Node.FunctionType) = {
    val ctxWithParams = argTypes.zipWithIndex.foldLeft(ctx -> Seq.empty[Node.Field]) {
      case ((ctx, params), (argType, argIndex)) =>
        ctx.typeToGoType(argType).map { case (ctx, typ) =>
          val param = paramNames.map(p => field(p(argIndex), typ)).getOrElse(typ.namelessField)
          ctx -> (params :+ param)
        }
    }

    ctxWithParams.map { case (ctx, params) =>
      val ctxWithResultTypOpt = retType match {
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