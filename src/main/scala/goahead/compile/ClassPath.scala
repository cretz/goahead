package goahead.compile

import java.nio.file.{Files, Paths}
import java.util.jar.JarFile
import java.util.zip.ZipEntry

import com.google.common.io.ByteStreams
import goahead.compile.Helpers._
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

// Guaranteed thread safe
case class ClassPath(entries: Seq[ClassPath.Entry]) {
  def findClassRelativeCompiledDir(classInternalName: String): Option[String] = {
    findFirstClass(classInternalName).map(_.relativeCompiledDir)
  }

  def isInterface(classInternalName: String): Boolean = {
    getFirstClass(classInternalName).access.isAccessInterface
  }

  def classHasSuper(childInternalName: String, parentInternalName: String): Boolean = {
    val details = getFirstClass(childInternalName)
    val possibles = details.interfaceInternalNames ++ details.superInternalName
    possibles.exists { possible =>
      possible == parentInternalName || classHasSuper(possible, parentInternalName)
    }
  }

  def findFirstClass(classInternalName: String): Option[ClassPath.ClassDetails] =
    entries.collectFirst(Function.unlift(_.findClass(classInternalName)))

  def getFirstClass(classInternalName: String): ClassPath.ClassDetails =
    findFirstClass(classInternalName) match {
      case Some(entry) => entry
      case None => sys.error(s"Unable to find class $classInternalName")
    }

  def close(): Unit = entries.foreach(e => swallowException(e.close()))
}

object ClassPath {

  sealed trait ClassDetails {
    def superInternalName: Option[String]
    def interfaceInternalNames: Seq[String]
    def relativeCompiledDir: String
    def access: Int
  }

  // Must be thread safe!
  sealed trait Entry {
    def findClass(internalClassName: String): Option[ClassDetails]
    def close(): Unit
  }

  object Entry {

    lazy val javaRuntimeJarPath = {
      val jdkPossible = Paths.get(System.getProperty("java.home"), "jre", "lib", "rt.jar")
      val jrePossible = Paths.get(System.getProperty("java.home"), "lib", "rt.jar")
      if (Files.exists(jdkPossible)) jdkPossible.toAbsolutePath.toString
      else if (Files.exists(jrePossible)) jrePossible.toAbsolutePath.toString
      else sys.error("Unable to find rt.jar")
    }

    def fromString(classPath: String): Seq[Entry] = ???

    def fromJar(fileName: String, relativeCompiledDir: String) =
      new SingleDirJarEntry(new JarFile(fileName), relativeCompiledDir)

    def fromLocalClass(cls: Class[_], relativeCompiledDir: String) = {
      val fileName = cls.getName.substring(cls.getName.lastIndexOf('.') + 1) + ".class"
      val inStream = cls.getResourceAsStream(fileName)
      require(inStream != null, s"Unable to find class file $fileName")
      fromClass(
        try { ByteStreams.toByteArray(inStream) } finally { swallowException(inStream.close()) },
        relativeCompiledDir
      )
    }

    def fromClass(bytes: Array[Byte], relativeCompiledDir: String) = SingleClassEntry(bytes, relativeCompiledDir)

    class SingleDirJarEntry(jar: JarFile, relativeCompiledDir: String) extends Entry {
      private[this] var cache = Map.empty[String, Option[SingleClassEntry]]

      override def findClass(internalClassName: String): Option[ClassDetails] = synchronized {
        cache.getOrElse(s"$internalClassName.class", {
          val detailsOpt = Option(jar.getEntry(s"$internalClassName.class")).map { entry =>
            SingleClassEntry(readEntry(entry), relativeCompiledDir)
          }
          cache += internalClassName -> detailsOpt
          detailsOpt
        })
      }

      // caller should be sync'd
      private[this] def readEntry(entry: ZipEntry): Array[Byte] = {
        val is = jar.getInputStream(entry)
        try { ByteStreams.toByteArray(is) } finally { swallowException(is.close()) }
      }

      override def close() = synchronized(swallowException(jar.close()))
    }

    case class SingleClassEntry(bytes: Array[Byte], relativeCompiledDir: String) extends Entry with ClassDetails {
      private[this] val node = {
        val node = new ClassNode()
        new ClassReader(bytes).accept(node, 0)
        node
      }

      override def findClass(internalClassName: String): Option[ClassDetails] =
        if (node.name == internalClassName) Some(this) else None

      override def close() = ()

      override def access = node.access

      override def superInternalName: Option[String] = Option(node.superName)

      override def interfaceInternalNames: Seq[String] = node.interfaceNames
    }
  }
}
