package goahead.transpiler

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

class Transpiler(val ctx: Transpiler.Context) extends Logger {

  def this(classPath: ClassPath) = this(Transpiler.Context(classPath))

  // Resulting file does not have a package name
  def classesToFile(classes: Seq[Array[Byte]]): Node.File = {
    // We will do a new import manager each time
    val fileCtx: Transpiler.Context = ctx.copy(imports = new MutableImportManager)

    // Run the transpiler for the given classes
    // TODO: can parallelize this btw...
    val declarations = classes.flatMap(bytes => fileCtx.classTranspiler.transpile(fileCtx, classBytesToNode(bytes)))

    Node.File(
      packageName = Node.Identifier(""),
      // Create imports based on the manager
      declarations = fileCtx.imports.buildDeclaration() +: declarations
    )
  }

  def classBytesToNode(bytes: Array[Byte]): ClassNode = {
    val node = new ClassNode()
    new ClassReader(bytes).accept(node, 0)
    node
  }

  def buildMainFile(importPath: String, javaInternalClassName: String): Node.File = {
    // New import manager for the main file
    val fileCtx: Transpiler.Context = ctx.copy(imports = new MutableImportManager)

    // Build main file
    val mainFile = MainBuilder.buildMainFile(
      MainBuilder.Context(
        transpileCtx = fileCtx,
        importPath = importPath,
        javaInternalClassName = javaInternalClassName
      )
    )

    // Add imports
    mainFile.copy(declarations = fileCtx.imports.buildDeclaration() +: mainFile.declarations)
  }
}

object Transpiler {
  case class Context(
    classPath: ClassPath,
    imports: MutableImportManager = new MutableImportManager,
    classTranspiler: ClassTranspiler = ClassTranspiler,
    methodTranspiler: MethodTranspiler = MethodTranspiler
  )
}