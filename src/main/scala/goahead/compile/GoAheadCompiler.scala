package goahead.compile

import goahead.Logger
import goahead.ast.Node

trait GoAheadCompiler extends Logger {
  import AstDsl._
  import Helpers._

  def compileMainFile(
    importPath: String,
    internalClassName: String,
    classPath: ClassPath,
    mangler: Mangler = Mangler.Simple
  ): Node.File =
    mainCompiler.compile(importPath, internalClassName, classPath, mangler)

  // Note, resulting package name is empty and expected to be filled in by caller
  def compile(
    internalClassNames: Seq[String],
    classPath: ClassPath,
    mangler: Mangler = Mangler.Simple
  ): Node.File = compileClasses(
    internalClassNames.map(n => classPath.getFirstClass(n).cls),
    classPath,
    mangler
  )

  // Note, resulting package name is empty and expected to be filled in by caller
  protected def compileClasses(
    classes: Seq[Cls],
    classPath: ClassPath,
    mangler: Mangler
  ): Node.File = {
    // TODO: parallelize at some point?
    val (imports, decls) = classes.foldLeft(Imports(classPath) -> Seq.empty[Node.Declaration]) {
      case ((imports, prevDecls), cls) =>
        classCompiler.compile(cls, imports, mangler).leftMap { case (imports, decls) =>
          imports -> (prevDecls ++ decls)
        }
    }

    file("", compileImports(imports).toSeq ++ decls)
  }

  protected def compileImports(mports: Imports): Option[Node.Declaration] =
    if (mports.aliases.isEmpty) None else Some(imports(mports.aliases.toSeq))

  protected def classCompiler: ClassCompiler = ClassCompiler

  protected def mainCompiler: MainCompiler = MainCompiler
}

object GoAheadCompiler extends GoAheadCompiler