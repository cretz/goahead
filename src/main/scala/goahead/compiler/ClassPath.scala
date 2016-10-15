package goahead.compiler

case class ClassPath(entries: Seq[ClassPath.Entry]) {
}

object ClassPath {
  sealed trait Entry {
    def findClass(internalClassName: String): Option[Array[Byte]]
    def close(): Unit
  }

  object Entry {
    def fromString(classPath: String): Seq[Entry] = ???
  }
}
