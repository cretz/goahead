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
    classes: Seq[String] = Seq("*"),
    outDir: String,
    parallel: Boolean = false,
    excludeSuperClassesOfSameEntry: Boolean = false,
    mangler: Option[String] = None,
    fileGrouping: Conf.FileGrouping = Conf.FileGrouping.Cls,
    prependToFile: Option[String] = None,
    excludeInnerClasses: Boolean = false,
    includeOldVersionClasses: Boolean = false,
    excludeAlreadyWrittenFiles: Boolean = false,
    excludeFields: Seq[String] = Nil,
    excludeMethods: Seq[String] = Nil,
    overrideMethods: Seq[String] = Nil,
    bodylessMethods: Seq[String] = Nil,
    fileTransformers: Seq[String] = Nil,
    onlyMethodsReferencingClasses: Boolean = false,
    onlyFieldsReferencingClasses: Boolean = false,
    excludePrivateMethods: Boolean = false,
    excludePrivateFields: Boolean = false,
    showExclusions: Boolean = false
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
        case "class-sans-inner" => ClsSansInner
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
    val outDir = Paths.get(conf.outDir).toAbsolutePath
    val goPackageName = outDir.getFileName.toString

    // Build up the class list which might include super classes
    var classEntries = getClassEntriesByFileName(conf, classPath, conf.manglerInst)
    if (conf.excludeAlreadyWrittenFiles)
      classEntries = classEntries.filter({ case (fileName, _) => !Files.exists(outDir.resolve(fileName)) })
    logger.trace(s"Class entries: " + classEntries.map(_._2.map(_.cls.name)))

    // Build the compiler
    var classRules = ClassRules(
      excludeFields = MethodOrFieldMatchers.fromStrings(conf.excludeFields),
      excludeMethods = MethodOrFieldMatchers.fromStrings(conf.excludeMethods),
      overrideMethods = MethodOrFieldMatchers.fromStrings(conf.overrideMethods),
      bodylessMethods = MethodOrFieldMatchers.fromStrings(conf.bodylessMethods) + MethodOrFieldMatch.NativeMatch
    )
    if (conf.onlyMethodsReferencingClasses || conf.onlyFieldsReferencingClasses) {
      val matcher = MethodOrFieldMatch.NotContainingTypes(
        internalClassNames = classEntries.flatMap(_._2.map(_.cls.name)).toSet,
        alsoCheckInstructionsOfMethodsNotMatching = classRules.bodylessMethods
      )
      if (conf.onlyMethodsReferencingClasses)
        classRules = classRules.copy(excludeMethods = classRules.excludeMethods + matcher)
      if (conf.onlyFieldsReferencingClasses)
        classRules = classRules.copy(excludeFields = classRules.excludeFields + matcher)
    }
    if (conf.excludePrivateMethods)
      classRules = classRules.copy(excludeMethods = classRules.excludeMethods + MethodOrFieldMatch.PrivateMatch)
    if (conf.excludePrivateFields)
      classRules = classRules.copy(excludeFields = classRules.excludeFields + MethodOrFieldMatch.PrivateMatch)
    val fileTransformers = conf.fileTransformers.map(c => Class.forName(c).newInstance().asInstanceOf[FileTransformer])
    val compiler = new Compile.FilteringCompiler(classRules, fileTransformers, conf.showExclusions)

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
    excludeFields: MethodOrFieldMatchers,
    excludeMethods: MethodOrFieldMatchers,
    overrideMethods: MethodOrFieldMatchers,
    bodylessMethods: MethodOrFieldMatchers
  )

  class FilteringCompiler(
    classRules: ClassRules,
    fileTransfomers: Seq[FileTransformer],
    showExclusions: Boolean
  ) extends GoAheadCompiler {

    override protected def compileClasses(conf: Config, classes: Seq[Cls], classPath: ClassPath, mangler: Mangler) =
      fileTransfomers.foldLeft(super.compileClasses(conf, classes, classPath, mangler)) {
        case (file, fileTransformer) => fileTransformer.apply(conf, classes, classPath, mangler, file)
      }

    override val classCompiler = new ClassCompiler {

      override protected val methodSetManager = new MethodSetManager.Filtered() {
        override def filter(method: Method, forImpl: Boolean) = {
          val excludeReasons = classRules.excludeMethods.matchReasons(method)
          if (excludeReasons.nonEmpty) {
            if (showExclusions)
              excludeReasons.foreach(r => logger.info(s"Excluding method $method because it $r"))
            false
          } else if (forImpl) {
            val overrideReasons = classRules.overrideMethods.matchReasons(method)
            if (showExclusions)
              overrideReasons.foreach(r => logger.info(s"Exclude method $method impl for override because it $r"))
            overrideReasons.isEmpty
          } else true
        }
      }

      override protected def clsFields(
        ctx: ClassCompiler.Context,
        forImpl: Boolean,
        includeParentFields: Boolean = false
      ): Seq[Field] =
        super.clsFields(ctx, forImpl, includeParentFields).filter { field =>
          val excludeReasons = classRules.excludeFields.matchReasons(field)
          if (showExclusions)
            excludeReasons.foreach(r => logger.info(s"Excluding field $field because it $r"))
          excludeReasons.isEmpty
        }

      override val methodCompiler = new MethodCompiler {
        override def compile(conf: Config, cls: Cls, method: Method, mports: Imports, mangler: Mangler) = {
          val excludeReasons = classRules.bodylessMethods.matchReasons(method)
          if (excludeReasons.isEmpty) {
            super.compile(conf, cls, method, mports, mangler)
          } else {
            if (showExclusions)
              excludeReasons.foreach(r => logger.info(s"Skipping body of method $method because it $r"))
            import AstDsl._
            val stmts = if (method.name == "<clinit>") Nil else {
              val methodStr = s"${method.cls.name}.${method.name}${method.desc}"
              Seq("panic".toIdent.call(Seq(s"Method not implemented - $methodStr".toLit)).toStmt)
            }
            buildFuncDecl(ctx = initContext(conf, cls, method, mports, mangler, Nil), stmts = stmts).map {
              case (ctx, funcDecl) => ctx.imports -> funcDecl
            }
          }
        }
      }
    }
  }

  sealed trait ClassPatternMatch {
    def matches(cls: Cls): Boolean = matches(cls.name.replace('/', '.'))
    def matches(cls: String): Boolean
  }

  object ClassPatternMatch {

    def apply(str: String): ClassPatternMatch = str.last match {
      case '*' => StartsWithDeep(str.init)
      case '?' => StartsWithShallow(str.init)
      case s => Exact(str)
    }

    case class StartsWithDeep(begin: String) extends ClassPatternMatch {
      override def matches(cls: String) = cls.startsWith(begin)
    }
    case class StartsWithShallow(begin: String) extends ClassPatternMatch {
      override def matches(cls: String) = cls.lastIndexOf('.') < begin.length && cls.startsWith(begin)
    }
    case class Exact(str: String) extends ClassPatternMatch {
      override def matches(cls: String) = cls == str
    }
  }

  sealed trait MethodOrFieldMatch {
    def matchReason(method: Method): Option[String]
    def matchReason(field: Field): Option[String]
  }

  object MethodOrFieldMatch {
    def apply(str: String): MethodOrFieldMatch = {
      str.indexOf("::") match {
        case -1 => SimpleName(ClassPatternMatch(str), None, None)
        case colonIndex =>
          val cls = ClassPatternMatch(str.substring(0, colonIndex))
          str.indexOf('(', colonIndex + 2) match {
            case -1 =>
              SimpleName(cls, Some(str.substring(colonIndex + 2)), None)
            case parenIndex =>
              SimpleName(cls, Some(str.substring(colonIndex + 2, parenIndex)), Some(str.substring(parenIndex)))
        }
      }
    }

    case class SimpleName(
      clsMatch: ClassPatternMatch,
      name: Option[String],
      desc: Option[String]
    ) extends MethodOrFieldMatch {

      override def matchReason(method: Method) = {
        if (clsMatch.matches(method.cls) && !name.exists(_ != method.name) && !desc.exists(_ != method.desc)) {
          Some("matches name filter of " + clsMatch + " - " + name + " - " + desc)
        } else None
      }

      override def matchReason(field: Field) = {
        if (clsMatch.matches(field.cls) && !name.exists(_ != field.name)) {
          Some("matches name filter")
        } else None
      }
    }

    case class NotContainingTypes(
      internalClassNames: Set[String],
      alsoCheckInstructionsOfMethodsNotMatching: MethodOrFieldMatchers
    ) extends MethodOrFieldMatch {
      override def matchReason(method: Method) = {
        var typesToCheck = method.argTypes.toSet + method.returnType
        if (alsoCheckInstructionsOfMethodsNotMatching.matchReasons(method).isEmpty) {
          typesToCheck ++= method.instructionRefTypes()
        }
        typeReason(typesToCheck)
      }

      override def matchReason(field: Field) = typeReason(Set(IType.getType(field.desc)))

      protected def typeReason(types: Set[IType]) = badClassReferences(types) match {
        case s if s.isEmpty => None
        case s => Some(s"references classes: ${s.mkString(", ")}")
      }

      protected def badClassReferences(types: Set[IType]) =
        types.flatMap(objectTypeName).diff(internalClassNames).toSeq.sorted

      protected def objectTypeName(typ: IType): Option[String] = typ match {
        case typ: IType.Simple if typ.isObject => Some(typ.internalName)
        case typ: IType.Simple if typ.isArray => objectTypeName(typ.arrayElementType)
        case _ => None
      }
    }

    case object PrivateMatch extends MethodOrFieldMatch {
      override def matchReason(method: Method) =
        if (method.access.isAccessPrivate) Some("is private") else None
      override def matchReason(field: Field) =
        if (field.access.isAccessPrivate) Some("is private") else None
    }

    case object NativeMatch extends MethodOrFieldMatch {
      override def matchReason(method: Method) =
        if (method.access.isAccessNative) Some("is native") else None
      override def matchReason(field: Field) =
        if (field.access.isAccessNative) Some("is native") else None
    }
  }

  case class MethodOrFieldMatchers(matchers: Seq[MethodOrFieldMatch]) {
    def matchReasons(method: Method): Seq[String] = matchers.flatMap(_.matchReason(method))
    def matchReasons(field: Field): Seq[String] = matchers.flatMap(_.matchReason(field))

    def `+`(rhs: MethodOrFieldMatch) = copy(matchers = matchers :+ rhs)
  }

  object MethodOrFieldMatchers {
    def fromStrings(strs: Seq[String]) = MethodOrFieldMatchers(strs.map(MethodOrFieldMatch.apply))
  }
}