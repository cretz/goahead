package goahead.cli

import goahead.ast.Node
import goahead.compile.{ClassPath, Cls, Config, Mangler}

trait FileTransformer {
  def apply(
    conf: Config,
    classes: Seq[Cls],
    classPath: ClassPath,
    mangler: Mangler,
    file: Node.File
  ): Node.File
}
