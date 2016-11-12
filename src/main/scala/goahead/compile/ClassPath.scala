package goahead.compile

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.jar.JarFile
import java.util.zip.ZipEntry

import com.google.common.io.ByteStreams
import goahead.compile.Helpers._
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

import scala.util.Try

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

  def findFirstClassWithEntry(classInternalName: String): Option[(ClassPath.Entry, ClassPath.ClassDetails)] =
    entries.collectFirst(Function.unlift(e => e.findClass(classInternalName).map(e -> _)))

  def findFirstClass(classInternalName: String): Option[ClassPath.ClassDetails] =
    entries.collectFirst(Function.unlift(_.findClass(classInternalName)))

  def getFirstClass(classInternalName: String): ClassPath.ClassDetails =
    findFirstClass(classInternalName) match {
      case Some(entry) => entry
      case None => sys.error(s"Unable to find class $classInternalName")
    }

  def getFirstClassWithEntry(classInternalName: String): (ClassPath.Entry, ClassPath.ClassDetails) =
    findFirstClassWithEntry(classInternalName) match {
      case Some(entry) => entry
      case None => sys.error(s"Unable to find class $classInternalName")
    }

  def close(): Unit = entries.foreach(e => swallowException(e.close()))
}

object ClassPath {

  // Each string can have multiple entries, separated with "path separator". Inside each
  // entry, dir is appended with equals sign if present or otherwise considered local import dir (i.e. "")
  def fromStrings(entryToRelativeCompiledDir: Seq[String]): ClassPath = fromMap(
    entryToRelativeCompiledDir.flatMap(_.split(File.pathSeparatorChar)).map({ entry =>
      entry.indexOf('=') match {
        case -1 => entry -> ""
        case index => entry.take(index) -> entry.drop(index + 1)
      }
    }).toMap
  )

  def fromMap(entryToRelativeCompiledDir: Map[String, String]): ClassPath =
    ClassPath(Entry.fromMap(entryToRelativeCompiledDir).toSeq)

  sealed trait ClassDetails {
    def name: String
    def packageName: String
    def superInternalName: Option[String]
    def interfaceInternalNames: Seq[String]
    def relativeCompiledDir: String
    def access: Int
    def bytes: Array[Byte]
  }

  // Must be thread safe!
  sealed trait Entry {
    def findClass(internalClassName: String): Option[ClassDetails]
    def close(): Unit
  }

  object Entry {

    // Note, lots of this follows http://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html

    lazy val javaRuntimeJarPath = {
      val jdkPossible = Paths.get(System.getProperty("java.home"), "jre", "lib", "rt.jar")
      val jrePossible = Paths.get(System.getProperty("java.home"), "lib", "rt.jar")
      if (Files.exists(jdkPossible)) jdkPossible
      else if (Files.exists(jrePossible)) jrePossible
      else sys.error("Unable to find rt.jar")
    }

    def fromMap(entryToRelativeCompiledDir: Map[String, String]) = entryToRelativeCompiledDir.map((fromString _).tupled)

    def fromString(str: String, relativeCompiledDir: String) = {
      if (str.endsWith("*")) fromJarDir(Paths.get(str.dropRight(1)), relativeCompiledDir) else {
        val path = Try(Paths.get(str)).recover({ case e => throw new Exception(s"Invalid path: $str", e)}).get
        if (Files.isDirectory(path)) fromClassDir(path, relativeCompiledDir)
        else if (Files.exists(path)) fromFile(path, relativeCompiledDir)
        else sys.error(s"Unable to find class path entry for $path")
      }
    }

    def fromClassDir(dir: Path, relativeCompiledDir: String) = new SingleDirClassDir(dir, relativeCompiledDir)

    def fromJarDir(dir: Path, relativeCompiledDir: String) = {
      import scala.collection.JavaConverters._
      require(Files.isDirectory(dir), s"$dir is not a directory")
      // Only .jar and .JAR files
      val fileStream = Files.newDirectoryStream(dir, "*.{jar,JAR}")
      new CompositeEntry(
        try { fileStream.iterator().asScala.toSeq.map(fromJarFile(_, relativeCompiledDir)) }
        finally { fileStream.close() }
      )
    }

    def fromFile(file: Path, relativeCompiledDir: String) = {
      require(Files.exists(file), s"$file does not exist")
      val fileStr = file.toString
      if (fileStr.endsWith(".jar") || fileStr.endsWith(".JAR")) fromJarFile(file, relativeCompiledDir)
      else if (fileStr.endsWith(".class")) fromClassFile(file, relativeCompiledDir)
      else sys.error(s"$file does not have jar or class extension")
    }

    def fromJarFile(file: Path, relativeCompiledDir: String) = {
      require(Files.exists(file), s"$file does not exist")
      new SingleDirJarEntry(new JarFile(file.toFile), relativeCompiledDir)
    }

    def fromClassFile(file: Path, relativeCompiledDir: String) =
      fromClass(Files.readAllBytes(file), relativeCompiledDir)

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

    class CompositeEntry[T <: Entry](entries: Seq[T]) extends Entry {
      override def findClass(internalClassName: String): Option[ClassDetails] =
        entries.collectFirst(Function.unlift(_.findClass(internalClassName)))

      override def close(): Unit = entries.foreach(_.close())
    }

    class SingleDirClassDir(dir: Path, relativeCompiledDir: String) extends Entry {
      require(Files.isDirectory(dir), s"$dir is not directory")
      private[this] var cache = Map.empty[String, Option[SingleClassEntry]]

      override def findClass(internalClassName: String): Option[ClassDetails] = synchronized {
        cache.getOrElse(internalClassName, {
          val pathPieces = s"$internalClassName.class".split('/')
          val filePath = dir.resolve(Paths.get(pathPieces.head, pathPieces.tail:_*))
          val detailsOpt =
            if (!Files.exists(filePath)) None
            else Some(SingleClassEntry(Files.readAllBytes(filePath), relativeCompiledDir))
          cache += internalClassName -> detailsOpt
          detailsOpt
        })
      }

      override def close() = ()
    }

    class SingleDirJarEntry(jar: JarFile, relativeCompiledDir: String) extends Entry {
      private[this] var cache = Map.empty[String, Option[SingleClassEntry]]

      override def findClass(internalClassName: String): Option[ClassDetails] = synchronized {
        cache.getOrElse(internalClassName, {
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

      override def name = node.name

      override def packageName = {
        name.lastIndexOf('/') match {
          case -1 => ""
          case index => name.substring(0, index)
        }
      }

      override def access = node.access

      override def superInternalName: Option[String] = Option(node.superName)

      override def interfaceInternalNames: Seq[String] = node.interfaceNames
    }
  }
}
