package goahead.transpiler

import goahead.ast.Node
import Helpers._

trait MainBuilder {

  def buildMainFile(ctx: MainBuilder.Context): Node.File = {
    val importDecl = Node.GenericDeclaration(
      token = Node.Token.Import,
      specifications = Seq(Node.ImportSpecification(
        name = Some("javaPkg".toIdent),
        path = ctx.importPath.toLit
      ))
    )

    val internalHelpersAlias = ctx.transpileCtx.imports.loadImportAlias("rt").toIdent

    val mainCall = Node.CallExpression(
      // TODO: params please
      function = Node.SelectorExpression(
        expression = Node.CallExpression(Node.SelectorExpression(
          expression = "javaPkg".toIdent,
          selector = goStaticAccessorName(ctx.javaInternalClassName).toIdent
        )),
        selector = goMethodNameFromClasses("main", Void.TYPE, classOf[Array[String]]).toIdent
      ),
      args = Seq(
        Node.CallExpression(
          function = Node.SelectorExpression(
            expression = internalHelpersAlias,
            selector = "OSArgs".toIdent
          )
        )
      )
    )

    val mainDecl = Node.FunctionDeclaration(
      receivers = Nil,
      name = "main".toIdent,
      typ = Node.FunctionType(Nil, Nil),
      body = Some(Node.BlockStatement(Seq(Node.ExpressionStatement(mainCall))))
    )

    Node.File(
      packageName = "main".toIdent,
      declarations = Seq(importDecl, mainDecl)
    )
  }
}

object MainBuilder extends MainBuilder {
  case class Context(
    transpileCtx: Transpiler.Context,
    importPath: String,
    javaInternalClassName: String
  )
}
