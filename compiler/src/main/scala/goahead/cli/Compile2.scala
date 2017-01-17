package goahead.cli

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.{Config => _}
import goahead.Logger
import goahead.ast.{Node, NodeWriter}
import goahead.compile.ClassPath._
import goahead.compile._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

trait Compile2 extends Command with Logger {

  override val name = "compile2"

  type Conf = CompileConfig

  override def argParser = { implicit builder =>
    CompileConfig(
      classPath = builder.opts(
        name = "class-path",
        aliases = Seq("cp"),
        desc = "The classpath same as javac except each entry is followed by an equals sign and the import dir. " +
          "Any without an equals sign is assumed to be in the local package (i.e. -o)."
      ).get,
      outDir = builder.opt(
        name = "out-dir",
        aliases = Seq("o"),
        default = ".",
        desc = "The output directory to compile all classes into. The trailing dir name will be the package name."
      ).get,
      parallel = builder.flag(
        name = "parallel",
        desc = "If set, run each file in parallel"
      ).get,
      excludeRunningRuntimeJar = builder.flag(
        name = "exclude-running-runtime-jar",
        aliases = Seq("nort"),
        desc = "If set, the running rt.jar will not be auto-included on the classpath in the 'rt' package"
      ).get,
      excludeSuperClassesOfSameEntry = builder.flag(
        name = "exclude-super-classes-of-same-entry",
        aliases = Seq("nosuper"),
        desc = "By default, all super classes/interfaces are built if they are in the same classpath " +
          "entry as the compiled classes. Setting this disables that feature and only builds explicitly named classes."
      ).get,
      excludeInnerClasses = builder.flag(
        name = "exclude-inner-classes",
        aliases = Seq("noinner"),
        desc = "By default, all inner classes/interfaces are built if they are in the same classpath " +
          "entry as the compiled classes. Setting this disables that feature and only builds explicitly named classes."
      ).get,
      mangler = builder.opt(
        name = "mangler",
        desc = "The fully qualified class name for a different mangler"
      ).map(v => if (v == "") None else Some(v)),
      fileGrouping = builder.opt(
        name = "file-grouping",
        default = "class",
        desc = "Either 'class' or 'package' or 'class-sans-inner' file grouping"
      ).map(CompileConfig.FileGrouping.apply),
      prependToFile = builder.opt(
        name = "prependToFile",
        desc = "String to prepend to each resulting file"
      ).map(v => if (v == "") None else Some(v)),
      classes = builder.trailingOpts(
        name = "classes",
        default = Seq("*"),
        desc = "The fully qualified class names on the class path to build stubs for. If it ends in an asterisk, it " +
          "checks for all classes that start with the value sans asterisk. If it ends in a question mark, it is the " +
          "same as an asterisk but won't include anything in a deeper package (i.e. having another dot). If not " +
          "present, it is the same as providing a single asterisk (i.e. all classes)"
      ).get
    )
  }

  override def confLoader = {
    import pureconfig._
    import CompileConfigImplicits._
    implicit def conv[T] = ConfigFieldMapping.apply[T](CamelCase, KebabCase)
    pureconfig.loadConfig[Conf]
  }

  override def run(conf: Conf): Unit = {
    // Build class path
    val classPathStrings =
      if (conf.excludeRunningRuntimeJar) conf.classPath
      else conf.classPath :+ s"${Entry.javaRuntimeJarPath}=rt"
    logger.debug(s"Setting class path to $classPathStrings")
    val classPath = ClassPath.fromStrings(classPathStrings)
    val outDir = Paths.get(conf.outDir).toAbsolutePath
    val goPackageName = outDir.getFileName.toString

    // Build up the class list which might include super classes
    var classEntries = getClassEntriesByFileName(conf, classPath, conf.manglerInst)
    val includedClasses = classEntries.flatMap(_._2)
    if (conf.excludeAlreadyWrittenFiles)
      classEntries = classEntries.filter({ case (fileName, _) => !Files.exists(outDir.resolve(fileName)) })
    logger.trace(s"Class entries: " + classEntries.map(_._2.map(_.cls.name)))

    // Build the compiler
    val compiler = new FilteringCompiler(
      conf,
      classPath,
      includedClasses,
      Forwarders.loadForwarders(conf.manglerInst, classPath, conf.outDir)
    )

    // Compile
    // TODO: Maybe something like monix would be better so I can do gatherUnordered?
    def futFn: (=> Path) => Future[Path] = if (conf.parallel) Future.apply[Path] else p => Future.successful(p)
    val futures = classEntries.map { case (fileName, classes) =>
      import goahead.compile.AstDsl._
      futFn {
        val code = compiler.compile(
          conf = Config(),
          internalClassNames = classes.map(_.cls.name),
          classPath = classPath,
          mangler = conf.manglerInst
        ).copy(packageName = goPackageName.toIdent)
        logger.info(s"Writing to $fileName")
        val resolvedFile = outDir.resolve(fileName)
        val writer = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(resolvedFile.toFile), StandardCharsets.UTF_8
        ))
        try {
          conf.prependToFile.foreach(s => writer.write(s + "\n"))
          NodeWriter.fromNode(code, writer)
        } finally { Try(writer.close()); () }
        resolvedFile
      }
    }

    // Just wait for all futures, ignore the response...
    Await.result(Future.sequence(futures), Duration.Inf)
    ()
  }

  protected def nodeToCode(conf: Conf, file: Node.File): String = {
    conf.prependToFile match {
      case None => NodeWriter.fromNode(file)
      case Some(prepend) => prepend + "\n" + NodeWriter.fromNode(file)
    }
  }

  protected def getClassEntriesByFileName(
    conf: Conf,
    classPath: ClassPath,
    mangler: Mangler
  ): Seq[(String, Seq[ClassDetails])] = {
    val classInternalNames = conf.classes.map(_.replace('.', '/')).flatMap({
      case str if str.endsWith("?") =>
        val prefix = str.dropRight(1)
        classPath.allClassNames().filter { str => str.lastIndexOf('/') < prefix.length && str.startsWith(prefix) }
      case str if str.endsWith("*") =>
        val prefix = str.dropRight(1)
        classPath.allClassNames().filter(_.startsWith(prefix))
      case str => Some(str)
    })
    var classEntries = classInternalNames.map(classPath.getFirstClass)
    if (!conf.excludeInnerClasses)
      classEntries ++= classEntries.flatMap(_.cls.innerClasses.map(classPath.getFirstClass))
    if (!conf.excludeSuperClassesOfSameEntry)
      classEntries ++= classEntries.flatMap(e => classPath.allSuperAndImplementingTypes(e.cls.name))
    if (!conf.includeOldVersionClasses)
      classEntries = classEntries.filter(_.cls.majorVersion >= ClassCompiler.MinSupportedMajorVersion)
    if (conf.anyClassModifiers.nonEmpty)
      classEntries = classEntries.filter { c =>
        import Helpers._
        val modStr = Modifier.toString(c.cls.access)
        conf.anyClassModifiers.exists(modStr.contains) ||
          (c.cls.access.isAccessPackagePrivate && conf.anyClassModifiers.contains("package-private"))
      }

    // De-dupe, filter again, then map into file names
    classEntries.
      distinct.
      groupBy(conf.fileGrouping.groupClassBy).map({
        case (k, v) => (mangler.fileName(k) + ".go") -> v.sortBy(_.cls.name)
      }).toSeq.sortBy(_._1)
  }
}

object Compile2 extends Compile2