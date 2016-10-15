package goahead.compiler

import java.nio.file.Path

import goahead.Logger
import goahead.ast.Node

class Compiler(
  val classPath: ClassPath,
  val config: Compiler.Config = Compiler.Config()
) extends Logger {

  def classFileToOutFile(bytes: Array[Byte]): Compiler.OutFile = ???
}

object Compiler {
  case class Config()

  case class OutFile(
    dir: Path,
    filename: String,
    file: Node.File,
    bootstrapMain: Option[Node.File]
  )
}