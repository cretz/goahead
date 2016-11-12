package goahead.cli

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit

import goahead.ast.NodeWriter
import goahead.compile.ClassPath._
import goahead.compile.{ClassPath, GoAheadCompiler, Mangler}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait BuildStubs extends Command {
  override val name = "build-stubs"

  case class Conf(
    excludeRunningRuntimeJar: Boolean,
    // Individual strings inside can even be broken out when separated via path separators. Equals
    // signs (if present) separate the entries from the dirs
    classPath: Seq[String],
    classes: Seq[String],
    outDir: String,
    // If true, then super classes won't be generated, only the specific ones asked for
    excludeSuperClassesOfSameEntry: Boolean,
    manglerOverrideClassName: Option[String]
  )

  override def argParser = { implicit builder =>
    Conf(
      classPath = builder.opts(
        name = "classpath",
        aliases = Seq("cp"),
        desc = "The classpath same as javac except each entry is followed by an equals sign and the import dir. " +
          "Any without an equals sign is assumed to be in the local package (i.e. out-dir)."
      ).get,
      outDir = builder.opt(
        name = "out-dir",
        aliases = Seq("o"),
        default = ".",
        desc = "The output directory to compile all classes into. The trailing dir name will be the package name."
      ).get,
      excludeRunningRuntimeJar = builder.flag(
        name = "exclude-running-runtime-jar",
        aliases = Seq("nort"),
        desc = "If set, the running rt.jar will not be auto-included on the classpath in the 'rt' package"
      ).get,
      excludeSuperClassesOfSameEntry = builder.flag(
        name = "exclude-super-classes-of-same-classpath",
        aliases = Seq("nosuper"),
        desc = "By default, all stubs for the super classes/interfaces are built if they are in the same classpath " +
          "entry as the compiled classes. Setting this disables that feature and only builds explicitly named classes."
      ).get,
      manglerOverrideClassName = builder.opt(
        name = "mangler",
        desc = "The fully qualified class name for a different mangler"
      ).map(v => if (v == "") None else Some(v)),
      classes = builder.trailingOpts(
        name = "class",
        required = true,
        desc = "The fully qualified class names on the class path to build stubs for"
      ).get
    )
  }

  override def run(conf: Conf): Unit = {
    // Build class path
    val classPathStrings =
      if (conf.excludeRunningRuntimeJar) conf.classPath
      else conf.classPath :+ s"${Entry.javaRuntimeJarPath}=rt"
    val classPath = ClassPath.fromStrings(classPathStrings)

    // Build up the class list which might include super classes
    val classEntries = getClassEntriesByPackage(conf, classPath)

    // Build mangler
    val mangler = conf.manglerOverrideClassName.map(
      Class.forName(_).newInstance().asInstanceOf[Mangler]
    ).getOrElse(Mangler.Simple)

    // Compile (with panics), one package per file
    val outDir = Paths.get(conf.outDir).toAbsolutePath
    val goPackageName = outDir.getFileName.toString
    // TODO: Maybe something like monix would be better so I can do gatherUnordered?
    val futures = classEntries.map { case (packageName, classes) =>
      import goahead.compile.AstDsl._
      Future {
        val code = GoAheadCompiler.WithOnlyPanicMethodCompiler.compile(
          classes = classes.map(_.bytes),
          classPath = classPath,
          mangler = mangler
        ).copy(packageName = goPackageName.toIdent)

        Files.write(
          outDir.resolve(mangler.packageFileName(packageName) + ".go"),
          NodeWriter.fromNode(code).getBytes(StandardCharsets.UTF_8)
        )
      }
    }

    // Just wait for all futures, ignore the response...wait for a fixed amount for now
    Await.result(Future.sequence(futures), Duration(20, TimeUnit.SECONDS))
    ()
  }

  protected def getClassEntriesByPackage(conf: Conf, classPath: ClassPath): Map[String, Seq[ClassPath.ClassDetails]] = {
    val classEntries =
      if (conf.excludeSuperClassesOfSameEntry) conf.classes.map(classPath.getFirstClass)
      else {
        // Meh, @tailrec not needed, the stack won't get too deep (//TODO: right?)
        def withSupersOfSameEntry(expectedEntry: Entry, child: ClassDetails): Seq[ClassDetails] = {
          val supers = (child.superInternalName ++ child.interfaceInternalNames).flatMap({ internalName =>
            val (entry, cls) = classPath.getFirstClassWithEntry(internalName)
            if (entry == expectedEntry) Some(cls) else None
          }).toSeq

          supers ++ supers.flatMap(withSupersOfSameEntry(expectedEntry, _))
        }

        conf.classes.flatMap { className =>
          val (entry, cls) = classPath.getFirstClassWithEntry(className)
          cls +: withSupersOfSameEntry(entry, cls)
        }
      }

    classEntries.groupBy(_.packageName)
  }
}

object BuildStubs extends BuildStubs