package goahead.compile

import goahead.ast.Node
import org.objectweb.asm.Type

trait MainCompiler {

  import AstDsl._
  import Helpers._

  def compile(
    importPath: String,
    internalClassName: String,
    classPath: ClassPath,
    mangler: Mangler
  ): Node.File = {
    val (mports, javaPkgAlias, rtAlias) = Imports(classPath).withImportAlias(importPath).map {
      case (mports, javaPkgAlias) => mports.withRuntimeImportAlias.map {
        case (mports, rtAlias) => (mports, javaPkgAlias.toIdent, rtAlias.toIdent)
      }
    }
    val mainName = mangler.implMethodName(
      "main",
      Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(classOf[Array[String]]))
    )
    val mainRef = javaPkgAlias.sel(mangler.staticAccessorName(internalClassName)).call().sel(mainName)
    val mainCall = mainRef.call(Seq(rtAlias.sel("OSArgs").call()))

    val mainDecl = funcDecl(
      rec = None,
      name = "main",
      params = Nil,
      results = None,
      stmts = mainCall.toStmt.singleSeq
    )
    file("main", Seq(imports(mports.aliases.toSeq), mainDecl))
  }
}

object MainCompiler extends MainCompiler
