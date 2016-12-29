package goahead.cli

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

import goahead.Logger
import goahead.ast.{Node, NodeWriter}
import goahead.compile.ClassPath._
import goahead.compile._
import org.objectweb.asm.Type

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait Compile extends Command with Logger {
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
    excludePatterns: Seq[String] = Nil,
    onlyMethodsReferencingClasses: Boolean = false,
    prependToFile: Option[String] = None,
    excludeInnerClasses: Boolean = false
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
      excludePatterns = builder.opts(
        name = "exclude-patterns",
        aliases = Seq("e"),
        desc = "Exclude certain patterns. Sans quotes, patterns can be things like 'java/lang/String' for classes, " +
          "'java/lang/String.<init>(Ljava/lang/String;)V' for methods, etc. No regex support or anything yet. Note, " +
          "this only excludes the impl methods, not the dispatch ones."
      ).get,
      onlyMethodsReferencingClasses = builder.flag(
        name = "only-methods-referencing-classes",
        aliases = Seq("limitmethods"),
        desc = "By default, all methods are built. When this is set, if the method param or return type is not one of " +
          "the classes being generated, it won't be generated."
      ).get,
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
    implicit val fileGroupingConvert = ConfigConvert.fromString(Conf.FileGrouping.apply)
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

    // Build up the class list which might include super classes
    val classEntries = getClassEntriesByFileName(conf, classPath, conf.manglerInst)
    logger.trace(s"Class entries: " + classEntries.mapValues(_.map(_.cls.name)))

    // Build the compiler
    val compiler = new Compile.FilteringCompiler(
      excludePatterns = conf.excludePatterns.toSet,
      onlyIncludeClassRefs =
        if (!conf.onlyMethodsReferencingClasses) Set.empty
        else classEntries.flatMap(_._2.map(_.cls.name)).toSet
    )

    // Compile (with panics), one package per file
    val outDir = Paths.get(conf.outDir).toAbsolutePath
    val goPackageName = outDir.getFileName.toString

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
        var codeStr = nodeToCode(conf, code)
        conf.prependToFile.foreach(s => codeStr = s"$s\n$codeStr")
        Files.write(outDir.resolve(fileName), codeStr.getBytes(StandardCharsets.UTF_8))
      }
    }

    // Just wait for all futures, ignore the response...wait for a fixed amount for now though only applies to parallel
    Await.result(Future.sequence(futures), Duration(20, TimeUnit.SECONDS))
    ()
  }

  protected def nodeToCode(conf: Conf, file: Node.File): String = NodeWriter.fromNode(file)

  protected def getClassEntriesByFileName(
    conf: Conf,
    classPath: ClassPath,
    mangler: Mangler
  ): Map[String, Seq[ClassDetails]] = {
    val classInternalNames = conf.classes.map(_.replace('.', '/')).flatMap({
      case str if str.endsWith("?") =>
        val prefix = str.dropRight(1)
        classPath.allClassNames().filter { str => str.lastIndexOf('.') < prefix.length && str.startsWith(prefix) }
      case str if str.endsWith("*") =>
        val prefix = str.dropRight(1)
        classPath.allClassNames().filter(_.startsWith(prefix))
      case str => Some(str)
    }).filterNot(conf.excludePatterns.contains)
    var classEntries = classInternalNames.map(classPath.getFirstClass)
    if (!conf.excludeInnerClasses)
      classEntries ++= classEntries.flatMap(_.cls.innerClasses.map(classPath.getFirstClass))
    if (!conf.excludeSuperClassesOfSameEntry)
      classEntries ++= classEntries.flatMap(e => classPath.allSuperAndImplementingTypes(e.cls.name))

    // De-dupe, filter again, then map into file names
    classEntries.
      distinct.
      filterNot(e => conf.excludePatterns.contains(e.cls.name)).
      groupBy(conf.fileGrouping.groupClassBy).map {
        case (k, v) => (mangler.fileName(k) + ".go") -> v.sortBy(_.cls.name)
      }
  }
}

object Compile extends Compile {
  import Helpers._

  class FilteringCompiler(excludePatterns: Set[String], onlyIncludeClassRefs: Set[String]) extends GoAheadCompiler {
    override val classCompiler = new ClassCompiler {

      override protected val methodSetManager = new MethodSetManager.Filtered() {
        override def filter(method: Method, forImpl: Boolean) = {
          val exclusions = if (forImpl) excludePatterns else Set.empty[String]
          if (exclusions.contains(method.cls.name + "." + method.name + method.desc)) false
          else if (onlyIncludeClassRefs.isEmpty) true
          else {
            // We need to make sure neither the params nor the
            val objectTypes = Type.getArgumentTypes(method.desc).flatMap(getObjectType) ++
              getObjectType(Type.getReturnType(method.desc))
            // Needs to be true if no object types exists that isn't in the only include
            val hasNonIncludedClassRef = objectTypes.exists(t => !onlyIncludeClassRefs.contains(t))
            !hasNonIncludedClassRef
          }
        }
      }

      override protected def clsFields(
        ctx: ClassCompiler.Context,
        includeParentFields: Boolean = false
      ): Seq[Field] = {
        super.clsFields(ctx, includeParentFields).filter { field =>
          if (excludePatterns.contains(ctx.cls.name + "." + field.name)) false
          else if (onlyIncludeClassRefs.isEmpty) true
          else {
            // Has to be in only-include-class-refs
            getObjectType(Type.getType(field.desc)) match {
              case None => true
              case Some(typ) => onlyIncludeClassRefs.contains(typ)
            }
          }
        }
      }

      def getObjectType(typ: Type): Option[String] = typ.getSort match {
        case Type.ARRAY => getObjectType(typ.getElementType)
        case Type.OBJECT => Some(typ.getInternalName)
        case _ => None
      }

      override val methodCompiler = new MethodCompiler {
        override def compile(conf: Config, cls: Cls, method: Method, mports: Imports, mangler: Mangler) = {
          import AstDsl._
          // Special handling for native methods
          if (!method.access.isAccessNative) super.compile(conf, cls, method, mports, mangler) else {
            val methodStr = s"${cls.name}.${method.name}${method.desc}"
            logger.debug(s"Skipping native method $methodStr")
            buildFuncDecl(
              ctx = initContext(conf, cls, method, mports, mangler, Nil),
              stmts = Seq("panic".toIdent.call(s"Native method not implemented - $methodStr".toLit.singleSeq).toStmt)
            ).map { case (ctx, funcDecl) => ctx.imports -> funcDecl }
          }
        }
      }
    }
  }
}