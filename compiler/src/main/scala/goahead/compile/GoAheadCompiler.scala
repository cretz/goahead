package goahead.compile

import goahead.Logger
import goahead.ast.Node

trait GoAheadCompiler extends Logger {
  import AstDsl._
  import Helpers._

  def compileMainFile(
    conf: Config,
    importPath: String,
    internalClassName: String,
    classPath: ClassPath,
    mangler: Mangler = new Mangler.Compact(packagePrivateUnexported = true) //Mangler.Simple
  ): Node.File =
    mainCompiler.compile(conf, importPath, internalClassName, classPath, mangler)

  // Note, resulting package name is empty and expected to be filled in by caller
  def compile(
    conf: Config,
    internalClassNames: Seq[String],
    classPath: ClassPath,
    mangler: Mangler = new Mangler.Compact(packagePrivateUnexported = true) //Mangler.Simple
  ): Node.File = compileClasses(
    conf,
    internalClassNames.map(n => classPath.getFirstClass(n).cls),
    classPath,
    mangler
  )

  // Note, resulting package name is empty and expected to be filled in by caller
  protected def compileClasses(
    conf: Config,
    classes: Seq[Cls],
    classPath: ClassPath,
    mangler: Mangler
  ): Node.File = {
    // TODO: parallelize at some point?
    val importsAndDecls = classes.foldLeft(Imports(classPath) -> Seq.empty[Node.Declaration]) {
      case ((imports, prevDecls), cls) =>
        classCompiler.compile(conf, cls, imports, mangler).map { case (imports, decls) =>
          imports -> (prevDecls ++ decls)
        }
    }

    importsAndDecls.map { case (mports, decls) =>
      compileInit(conf, mports, classes, mangler).map { case (mports, initDeclOpt) =>
        file("", compileImports(mports).toSeq ++ (decls ++ initDeclOpt))
      }
    }
  }

  protected def compileImports(mports: Imports): Option[Node.Declaration] =
    if (mports.aliases.isEmpty) None else Some(imports(mports.aliases.toSeq))

  protected def compileInit(
    conf: Config,
    mports: Imports,
    classes: Seq[Cls],
    mangler: Mangler
  ): (Imports, Option[Node.Declaration]) = {
    if (!conf.reflectionSupport) mports -> None else {
      // We need to create a bunch of ClassInfo structures for reflection purposes
      mports.withRuntimeImportAlias.map { case (mports, rtAlias) =>
        val fn = if (rtAlias.isEmpty) "AddStaticRefs".toIdent else rtAlias.toIdent.sel("AddStaticRefs")
        val valTyp = if (rtAlias.isEmpty) "ClassInfoProvider".toIdent else rtAlias.toIdent.sel("ClassInfoProvider")
        val classNameMap = classes.map(c => c.runtimeClassName.toLit -> mangler.staticVarName(c.name).toIdent.addressOf)
        val javaImplMap = classes.map(c => mangler.implObjectName(c.name).toStringLit -> c.runtimeClassName.toLit)
        val call = fn.call(Seq(
          mapLit("string".toIdent, valTyp, classNameMap),
          mapLit("string".toIdent, "string".toIdent, javaImplMap)
        ))
        mports -> Some(funcDecl(None, "init", funcType(Nil), Seq(call.toStmt)))
      }
    }
  }

  protected def classCompiler: ClassCompiler = ClassCompiler

  protected def mainCompiler: MainCompiler = MainCompiler
}

object GoAheadCompiler extends GoAheadCompiler