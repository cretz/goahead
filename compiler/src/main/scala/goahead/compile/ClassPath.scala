package goahead.compile

import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.zip.{ZipEntry, ZipFile}

import com.google.common.io.ByteStreams
import goahead.compile.Helpers._
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

import scala.util.Try

// Guaranteed thread safe
case class ClassPath(entries: Seq[ClassPath.Entry]) {

  def `++`(other: Seq[ClassPath.Entry]) = copy(entries = entries ++ other)

  def findClassRelativeCompiledDir(classInternalName: String): Option[String] = {
    findFirstClass(classInternalName).map(_.relativeCompiledDir)
  }

  def isInterface(classInternalName: String): Boolean = {
    getFirstClass(classInternalName).cls.access.isAccessInterface
  }

  // Guaranteed to start with given type and recurse up and return in order
  def allSuperTypes(classInternalName: String): Seq[ClassPath.ClassDetails] = {
    // Not deep enough to rework for @tailrec
    getFirstClass(classInternalName).cls.parent.toSeq.flatMap({ superInternalName =>
      val details = getFirstClass(superInternalName)
      details +: allSuperTypes(superInternalName)
    }).distinct
  }

  // Guaranteed to start with given type and recurse up and return in order
  def allInterfaceTypes(classInternalName: String): Seq[ClassPath.ClassDetails] = {
    // Not deep enough to rework for @tailrec
    getFirstClass(classInternalName).cls.interfaces.flatMap({ interfaceInternalName =>
      val details = getFirstClass(interfaceInternalName)
      details +: allInterfaceTypes(interfaceInternalName)
    }).distinct
  }

  // Guaranteed to start with given type and recurse up and return in order
  def allSuperAndImplementingTypes(classInternalName: String): Seq[ClassPath.ClassDetails] = {
    val superTypes = allSuperTypes(classInternalName)
    (superTypes ++ allInterfaceTypes(classInternalName) ++
      superTypes.flatMap(c => allInterfaceTypes(c.cls.name))).distinct
  }

  def classImplementsOrExtends(childInternalName: String, parentInternalName: String): Boolean = {
    allSuperAndImplementingTypes(childInternalName).exists(_.cls.name == parentInternalName)
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

  final def findFieldOnDeclarer(
    startAtClassInternalName: String,
    fieldName: String,
    static: Boolean
  ): Field = {
    val allImpls = getFirstClass(startAtClassInternalName) +: allSuperAndImplementingTypes(startAtClassInternalName)
    // First try to find it in non-interfaces, then in interfaces if static
    allImpls.filter(c => !c.cls.access.isAccessInterface).
      collectFirst(Function.unlift(_.cls.fields.find(f => f.access.isAccessStatic == static && f.name == fieldName))).
      orElse(
        if (!static) None
        else allImpls.filter(_.cls.access.isAccessInterface).
          collectFirst(Function.unlift(_.cls.fields.find(f => f.access.isAccessStatic && f.name == fieldName)))
      ).getOrElse(sys.error(s"Cannot find field $fieldName"))
  }

  def allClassNames(): Iterable[String] = entries.flatMap(_.allClassNames())
  def classNamesWithoutCompiledDir(): Iterable[String] = entries.flatMap(_.classNamesWithoutCompiledDir())

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
    def cls: Cls
    def relativeCompiledDir: String
  }

  // Must be thread safe!
  sealed trait Entry {
    def findClass(internalClassName: String): Option[ClassDetails]
    def allClassNames(): Iterable[String]
    def classNamesWithoutCompiledDir(): Iterable[String]
    def close(): Unit
  }

  object Entry {

    // Note, lots of this follows http://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html

    lazy val javaRuntimeJarPath = {
      val jdk8Possible = Paths.get(System.getProperty("java.home"), "jre", "lib", "rt.jar")
      val jre8Possible = Paths.get(System.getProperty("java.home"), "lib", "rt.jar")
      val jdk9Possible = Paths.get(System.getProperty("java.home"), "jmods", "java.base.jmod")
      if (Files.exists(jdk8Possible)) jdk8Possible
      else if (Files.exists(jre8Possible)) jre8Possible
      else if (Files.exists(jdk9Possible)) jdk9Possible
      else sys.error("Unable to find rt.jar or java.base.jmod")
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
      val fileStream = Files.newDirectoryStream(dir, "*.{jar,JAR,jmod,JMOD}")
      new CompositeEntry(
        try { fileStream.iterator().asScala.toSeq.map(fromZipFile(_, relativeCompiledDir)) }
        finally { fileStream.close() }
      )
    }

    def fromFile(file: Path, relativeCompiledDir: String) = {
      require(Files.exists(file), s"$file does not exist")
      val fileStr = file.toString
      if (fileStr.endsWith(".jar") || fileStr.endsWith(".JAR")) fromZipFile(file, relativeCompiledDir)
      else if (fileStr.endsWith(".jmod") || fileStr.endsWith(".JMOD")) fromZipFile(file, relativeCompiledDir)
      else if (fileStr.endsWith(".class")) fromClassFile(file, relativeCompiledDir)
      else sys.error(s"$file does not have jar or jmod or class extension")
    }

    def fromZipFile(file: Path, relativeCompiledDir: String): Entry = {
      require(Files.exists(file), s"$file does not exist")
      val fileName = file.toString
      if (fileName.endsWith(".jar") || fileName.endsWith(".JAR"))
        new SingleDirJarEntry(new ZipFile(file.toFile), relativeCompiledDir)
      else if (fileName.endsWith(".jmod") || fileName.endsWith(".JMOD"))
        new SingleDirJmodEntry(new ZipFile(file.toFile), relativeCompiledDir)
      else sys.error(s"Expected $fileName to be a jar or jmod file")
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

      override def allClassNames(): Iterable[String] = entries.flatMap(_.allClassNames())

      override def classNamesWithoutCompiledDir() = entries.flatMap(_.classNamesWithoutCompiledDir())

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

      override def allClassNames(): Iterable[String] = {
        import scala.collection.JavaConverters._
        val onlyClassPred = new BiPredicate[Path, BasicFileAttributes] {
          override def test(t: Path, u: BasicFileAttributes) = t.toString.endsWith(".class")
        }

        Files.find(dir, Int.MaxValue, onlyClassPred).iterator().asScala.toIterable.map { path =>
          path.subpath(dir.getNameCount, path.getNameCount).toString.
            dropRight(6).replace(FileSystems.getDefault.getSeparator, "/")
        }
      }

      override def classNamesWithoutCompiledDir() = if (relativeCompiledDir == "") allClassNames() else Iterable.empty

      override def close() = ()
    }

    sealed trait BaseZipEntry extends Entry {
      private[this] var cache = Map.empty[String, Option[SingleClassEntry]]

      def file: ZipFile
      def relativeCompiledDir: String

      protected def internalClassNameToEntryPath(internalName: String): Option[String]

      private[this] def readEntry(entry: ZipEntry): Array[Byte] = {
        val is = file.getInputStream(entry)
        try { ByteStreams.toByteArray(is) } finally { swallowException(is.close()) }
      }

      override def findClass(internalClassName: String): Option[ClassDetails] = synchronized {
        cache.getOrElse(internalClassName, {
          val detailsOpt = internalClassNameToEntryPath(internalClassName).flatMap(v => Option(file.getEntry(v))).
            map(entry => SingleClassEntry(readEntry(entry), relativeCompiledDir))
          cache += internalClassName -> detailsOpt
          detailsOpt
        })
      }

      override def classNamesWithoutCompiledDir() = if (relativeCompiledDir == "") allClassNames() else Iterable.empty

      override def close() = synchronized(swallowException(file.close()))
    }

    class SingleDirJarEntry(val file: ZipFile, val relativeCompiledDir: String) extends BaseZipEntry {

      protected def internalClassNameToEntryPath(internalName: String) = Some(s"$internalName.class")

      override def allClassNames(): Iterable[String] = {
        import scala.collection.JavaConverters._
        file.stream().iterator().asScala.toIterable.collect {
          case e if e.getName.endsWith(".class") => e.getName.dropRight(6)
        }
      }
    }

    class SingleDirJmodEntry(val file: ZipFile, val relativeCompiledDir: String) extends BaseZipEntry {
      protected def internalClassNameToEntryPath(internalName: String) = Some(s"classes/$internalName.class")

      override def allClassNames(): Iterable[String] = {
        import scala.collection.JavaConverters._
        file.stream().iterator().asScala.toIterable.collect {
          case e if
              e.getName != "classes/module-info.class" &&
              e.getName.startsWith("classes/") &&
              e.getName.endsWith(".class") =>
            e.getName.drop(8).dropRight(6)
        }
      }
    }

    case class SingleClassEntry(cls: Cls, relativeCompiledDir: String) extends Entry with ClassDetails {
      override def findClass(internalClassName: String): Option[ClassDetails] =
        if (cls.name == internalClassName) Some(this) else None

      override def allClassNames(): Iterable[String] = Iterable(cls.name)

      override def classNamesWithoutCompiledDir() = if (relativeCompiledDir == "") allClassNames() else Iterable.empty

      override def close() = ()
    }

    object SingleClassEntry {
      def apply(bytes: Array[Byte], relativeCompiledDir: String): SingleClassEntry = {
        val node = new ClassNode()
        new ClassReader(bytes).accept(node, 0)
        SingleClassEntry(Cls(node), relativeCompiledDir)
      }
    }
  }
}
