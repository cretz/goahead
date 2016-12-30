package goahead.cli

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import goahead.Logger
import goahead.ast.{Node, NodeWriter}
import goahead.compile.ClassPath._
import goahead.compile._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

trait Compile extends Command with Logger {
  import Compile._

  override val name = "compile"

  case class Conf(
    excludeRunningRuntimeJar: Boolean = false,
    classPath: Seq[String] = Nil,
    classes: Seq[String] = Nil,
    outDir: String,
    parallel: Boolean = false,
    excludeSuperClassesOfSameEntry: Boolean = false,
    mangler: Option[String] = None,
    fileGrouping: Conf.FileGrouping = Conf.FileGrouping.Cls,
    prependToFile: Option[String] = None,
    excludeInnerClasses: Boolean = false,
    includeOldVersionClasses: Boolean = false,
    excludeAlreadyWrittenFiles: Boolean = false,
    classRules: Map[String, ClassRules] = Map.empty
  ) {
    lazy val manglerInst = mangler.map(Class.forName(_).newInstance().asInstanceOf[Mangler]).getOrElse(Mangler.Simple)
  }

  object Conf {
    sealed trait FileGrouping {
      def groupClassBy(det: ClassDetails): String
    }
    object FileGrouping {
      def apply(name: String): FileGrouping = name match {
        case "class" => Cls
        case "class-sans-inner" => Cls
        case "package" => Pkg
        case str if str.startsWith("first-packages-") => FirstXPkgs(str.substring("first-packages-".length).toInt)
        case _ => sys.error(s"Unrecognized file grouping: $name")
      }

      case object Cls extends FileGrouping {
        // Put inner classes together
        override def groupClassBy(det: ClassDetails) = det.cls.name.indexOf('$') match {
          case -1 => det.cls.name
          case index => det.cls.name.substring(0, index)
        }
      }

      case object ClsSansInner extends FileGrouping {
        override def groupClassBy(det: ClassDetails) = det.cls.name
      }

      case object Pkg extends FileGrouping {
        override def groupClassBy(det: ClassDetails) = det.cls.packageName
      }

      case class FirstXPkgs(count: Int) extends FileGrouping {
        override def groupClassBy(det: ClassDetails) = {
          if (det.cls.packageName.isEmpty) "__root__" else {
            val pieces = det.cls.packageName.split('/')
            pieces.take(count).mkString("/")
          }
        }
      }
    }
  }

  override def argParser = { implicit builder =>
    Conf(
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
      ).map(Conf.FileGrouping.apply),
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

  override def confLoader = path => {
    import pureconfig._
    implicit val fileGroupingConvert = ConfigConvert.fromString(Conf.FileGrouping.apply)
    implicit def conv[T] = ConfigFieldMapping.apply[T](CamelCase, KebabCase)
    pureconfig.loadConfig[Conf](path).map { conf =>
      if (conf.classes.isEmpty) {
        if (conf.classRules.nonEmpty) conf.copy(classes = conf.classRules.keys.toSeq)
        else conf.copy(classes = Seq("*"))
      } else conf
    }
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
    if (conf.excludeAlreadyWrittenFiles)
      classEntries = classEntries.filter({ case (fileName, _) => !Files.exists(outDir.resolve(fileName)) })
    logger.trace(s"Class entries: " + classEntries.map(_._2.map(_.cls.name)))

    // Build the compiler
    val compiler = new Compile.FilteringCompiler(conf.classRules.map({ case (k, v) => k.replace('.', '/') -> v }))

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
        classPath.allClassNames().filter { str => str.lastIndexOf('.') < prefix.length && str.startsWith(prefix) }
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

    // De-dupe, filter again, then map into file names
    classEntries.
      distinct.
      groupBy(conf.fileGrouping.groupClassBy).map({
        case (k, v) => (mangler.fileName(k) + ".go") -> v.sortBy(_.cls.name)
      }).toSeq.sortBy(_._1)
  }
}

object Compile extends Compile {
  import Helpers._

  case class ClassRules(
    excludeFields: Set[String] = Set.empty,
    excludeMethods: Set[String] = Set.empty,
    overrideMethods: Set[String] = Set.empty,
    panicMethods: Set[String] = Set.empty
  )

  class FilteringCompiler(classRules: Map[String, ClassRules]) extends GoAheadCompiler {
    override val classCompiler = new ClassCompiler {

      override protected val methodSetManager = new MethodSetManager.Filtered() {
        override def filter(method: Method, forImpl: Boolean) = {
          !classRules.get(method.cls.name).exists { rules =>
            val toCheck = method.cls.name + "." + method.name + method.desc
            rules.excludeMethods.contains(toCheck) || (forImpl && rules.overrideMethods.contains(toCheck))
          }
        }
      }

      override protected def clsFields(
        ctx: ClassCompiler.Context,
        forImpl: Boolean,
        includeParentFields: Boolean = false
      ): Seq[Field] = {
        super.clsFields(ctx, forImpl, includeParentFields).filter { field =>
          !classRules.get(field.cls.name).exists { rules =>
            rules.excludeFields.contains(field.cls.name + "." + field.name)
          }
        }
      }

      override val methodCompiler = new MethodCompiler {
        override def compile(conf: Config, cls: Cls, method: Method, mports: Imports, mangler: Mangler) = {
          import AstDsl._
          val methodStr = s"${cls.name}.${method.name}${method.desc}"
          val panicImpl = method.access.isAccessNative ||
            classRules.get(cls.name).exists(_.panicMethods.contains(methodStr))
          if (!panicImpl) super.compile(conf, cls, method, mports, mangler) else {
            buildFuncDecl(
              ctx = initContext(conf, cls, method, mports, mangler, Nil),
              stmts = Seq("panic".toIdent.call(s"Method not implemented - $methodStr".toLit.singleSeq).toStmt)
            ).map { case (ctx, funcDecl) => ctx.imports -> funcDecl }
          }
        }
      }
    }
  }
}