package goahead.transpiler

case class ClassPath(
  classesToDir: Map[String, String],
  entries: Seq[ClassPath.Entry]
) {
  def findClassDir(classInternalName: String): Option[String] = {
    classesToDir.get(classInternalName)
  }

  def isInterface(classInternalName: String): Boolean = {
    import Helpers._
    findFirstClass(classInternalName).exists(_.access.isAccessInterface)
  }

  def findFirstClass(classInternalName: String): Option[ClassPath.ClassDetails] =
    entries.collectFirst(Function.unlift(_.getClassDetails(classInternalName)))
}

object ClassPath {
  // Must be thread safe!
  sealed trait Entry {
    def getClassDetails(internalClassName: String): Option[ClassDetails]
    def close(): Unit
  }

  object Entry {
    def fromString(classPath: String): Seq[Entry] = ???
  }

  sealed trait ClassDetails {
    def access: Int
  }
}
