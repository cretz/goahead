package goahead.cli

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

import goahead.Logger
import goahead.ast.{Node, NodeWriter}
import goahead.compile.ClassCompiler.Context
import goahead.compile.ClassPath._
import goahead.compile._
import org.objectweb.asm.Type
import org.objectweb.asm.tree.{FieldNode, MethodNode}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait BuildStubs extends Command with Logger {
  override val name = "build-stubs"

  case class Conf(
    excludeRunningRuntimeJar: Boolean = false,
    // Individual strings inside can even be broken out when separated via path separators. Equals
    // signs (if present) separate the entries from the dirs
    classPath: Seq[String] = Nil,
    classes: Seq[String] = Nil,
    outDir: String,
    parallel: Boolean = false,
    // If true, then super classes won't be generated, only the specific ones asked for
    excludeSuperClassesOfSameEntry: Boolean = false,
    mangler: Option[String] = None,
    fileGrouping: Conf.FileGrouping = Conf.FileGrouping.Pkg,
    excludePatterns: Seq[String] = Nil,
    onlyMethodsReferencingClasses: Boolean = false,
    prependToFile: Option[String] = None
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
        case "package" => Pkg
        case _ => sys.error(s"Unrecognized file grouping: $name")
      }

      case object Cls extends FileGrouping { override def groupClassBy(det: ClassDetails) = det.name }
      case object Pkg extends FileGrouping { override def groupClassBy(det: ClassDetails) = det.packageName }
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
        desc = "If set, run each file (i.e. package-level) in parallel"
      ).get,
      excludeRunningRuntimeJar = builder.flag(
        name = "exclude-running-runtime-jar",
        aliases = Seq("nort"),
        desc = "If set, the running rt.jar will not be auto-included on the classpath in the 'rt' package"
      ).get,
      excludeSuperClassesOfSameEntry = builder.flag(
        name = "exclude-super-classes-of-same-entry",
        aliases = Seq("nosuper"),
        desc = "By default, all stubs for the super classes/interfaces are built if they are in the same classpath " +
          "entry as the compiled classes. Setting this disables that feature and only builds explicitly named classes."
      ).get,
      mangler = builder.opt(
        name = "mangler",
        desc = "The fully qualified class name for a different mangler"
      ).map(v => if (v == "") None else Some(v)),
      fileGrouping = builder.opt(
        name = "file-grouping",
        default = "package",
        desc = "Either 'cls' or 'pkg' file grouping"
      ).map(Conf.FileGrouping.apply),
      excludePatterns = builder.opts(
        name = "exclude-patterns",
        aliases = Seq("e"),
        desc = "Exclude certain patterns. Sans quotes, patterns can be things like 'java/lang/String' for classes, " +
          "'java/lang/String.<init>(Ljava/lang/String;)V for methods, etc. No regex support or anything yet."
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
        required = true,
        desc = "The fully qualified class names on the class path to build stubs for"
      ).get
    )
  }

  override def confLoader = {
    import pureconfig._
    implicit val fileGroupingConvert = ConfigConvert.fromString(Conf.FileGrouping.apply)
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
    logger.trace(s"Class entries: " + classEntries.mapValues(_.map(_.name)))

    // Build the compiler
    // (excludePatterns: Set[String], allClassNamesRefsToCheck: Set[String])
    val compiler = new BuildStubs.StubCompiler(
      excludePatterns = conf.excludePatterns.toSet,
      onlyIncludeClassRefs =
        if (!conf.onlyMethodsReferencingClasses) Set.empty
        else classEntries.flatMap(_._2.map(_.name)).toSet
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
          classes = classes.map(_.bytes),
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
    val classInternalNames = conf.classes.map(_.replace('.', '/'))
    val classEntries =
      if (conf.excludeSuperClassesOfSameEntry) classInternalNames.map(classPath.getFirstClass)
      else {
        // Meh, @tailrec not needed, the stack won't get too deep (//TODO: right?)
        def withSupersOfSameEntry(expectedEntry: Entry, child: ClassDetails): Seq[ClassDetails] = {
          val supers = (child.superInternalName ++ child.interfaceInternalNames).flatMap({ internalName =>
            val (entry, cls) = classPath.getFirstClassWithEntry(internalName)
            if (entry == expectedEntry) Some(cls) else None
          }).toSeq

          supers ++ supers.flatMap(withSupersOfSameEntry(expectedEntry, _))
        }

        classInternalNames.flatMap { className =>
          val (entry, cls) = classPath.getFirstClassWithEntry(className)
          cls +: withSupersOfSameEntry(entry, cls)
        }
      }

    // Have to remove class entries that are excluded by user
    classEntries.filterNot(c => conf.excludePatterns.contains(c.name)).distinct.
      groupBy(conf.fileGrouping.groupClassBy).map { case (k, v) => (mangler.fileName(k) + ".go") -> v.sortBy(_.name) }
  }
}

object BuildStubs extends BuildStubs {
  import AstDsl._
  import Helpers._
  class StubCompiler(excludePatterns: Set[String], onlyIncludeClassRefs: Set[String]) extends GoAheadCompiler {
    override val classCompiler = new ClassCompiler {

      override protected def methodNodes(ctx: Context, forDispatch: Boolean): Seq[MethodNode] = {
        val exclusions = if (forDispatch) Set.empty[String] else excludePatterns
        super.methodNodes(ctx, forDispatch).filter { method =>
          if (method.access.isAccessPrivate ||
            exclusions.contains(ctx.cls.name + "." + method.name + method.desc)) false
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

      override protected def fieldNodes(ctx: Context): Seq[FieldNode] = super.fieldNodes(ctx).filter { field =>
        if (field.access.isAccessPrivate || excludePatterns.contains(ctx.cls.name + "." + field.name)) false
        else if (onlyIncludeClassRefs.isEmpty) true
        else {
          // Has to be in only-include-class-refs
          getObjectType(Type.getType(field.desc)) match {
            case None => true
            case Some(typ) => onlyIncludeClassRefs.contains(typ)
          }
        }
      }

      def getObjectType(typ: Type): Option[String] = typ.getSort match {
        case Type.ARRAY => getObjectType(typ.getElementType)
        case Type.OBJECT => Some(typ.getInternalName)
        case _ => None
      }

      override val methodCompiler = new MethodCompiler {
        override def getLabelSets(node: Method) = Nil
        override def compileLabelSets(ctx: MethodCompiler.Context): (MethodCompiler.Context, Seq[Node.Statement]) = {
          // Only panic calls except for static init
          if (ctx.method.name == "<clinit>") ctx -> Seq(Node.EmptyStatement)
          else ctx -> "panic".toIdent.call("Not Implemented".toLit.singleSeq).toStmt.singleSeq
        }
      }
    }
  }
}