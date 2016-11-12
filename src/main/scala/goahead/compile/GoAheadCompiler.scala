package goahead.compile

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

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
    classes: Seq[Array[Byte]],
    classPath: ClassPath,
    mangler: Mangler = Mangler.Simple
  ): Node.File = compileNodes(classes.map(classBytesToNode), classPath, mangler)

  protected def classBytesToNode(bytes: Array[Byte]): ClassNode = {
    val node = new ClassNode()
    new ClassReader(bytes).accept(node, 0)
    node
  }

  // Note, resulting package name is empty and expected to be filled in by caller
  protected def compileNodes(
    classes: Seq[ClassNode],
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

    file("", compileImports(imports) +: decls)
  }

  protected def compileImports(mports: Imports): Node.Declaration = imports(mports.aliases.toSeq)

  protected def classCompiler: ClassCompiler = ClassCompiler

  protected def mainCompiler: MainCompiler = MainCompiler
}

object GoAheadCompiler extends GoAheadCompiler {
  trait WithOnlyPanicMethodCompiler extends GoAheadCompiler {
    override def classCompiler: ClassCompiler = ClassCompiler.WithOnlyPanicMethodCompiler
  }

  object WithOnlyPanicMethodCompiler extends WithOnlyPanicMethodCompiler
}