package goahead.compile

import java.io._
import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import com.google.common.io.CharStreams
import com.google.common.reflect.{ClassPath => CP}
import goahead._
import goahead.ast.{Node, NodeWriter}
import org.objectweb.asm.util.Printer
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.io.Source
import scala.util.Try

class CompilerSpec extends BaseSpec with BeforeAndAfterAll {
  import AstDsl._
  import CompilerSpec._

  // Create this entry for the "rt" classes and close at the end
  val javaRuntimeEntry =
    if (useTestRt) {
      ClassPath.Entry.fromZipFile(ClassPath.Entry.javaRuntimeLibPath(),
        "github.com/cretz/goahead/libs/java/testrt")
    } else {
      require(sys.env.contains("ZULU_JDK_HOME"), "ZULU_JDK_HOME env var required")
      ClassPath.Entry.fromZipFile(
        Paths.get(sys.env("ZULU_JDK_HOME"), "/jmods/java.base.jmod"),
        "github.com/cretz/goahead/libs/java/rt"
      )
    }

  val goFormatTimeout = 40.seconds
  val goBuildTimeout = if (useTestRt) 40.seconds else 4.hours
  val exeRunTimeout = if (useTestRt) 40.seconds else 1.hour
  val checkFormatting = false

  override protected def afterAll() = {
    javaRuntimeEntry.close()
    showUsedOpcodes()
  }

  // Run each test case as its own setup
  testCases.foreach{t => t.subject should behave like expected(t)}

  def expected(t: CompilerSpec.TestCase) = it should t.provideExpectedOutput in withTemporaryFolder { tempFolder =>
    if (t.nonWindowsOnly) assume(!sys.props.get("os.name").exists(_.startsWith("Windows")))
    val testClasses = t.classes.map(ClassPath.Entry.fromLocalClass(_, ""))

    // Set classpath with helper classes
    val classPath = ClassPath(testClasses :+ javaRuntimeEntry)
    val testClassNames = testClasses.map(_.cls.name)

    val opcodes = getOpcodes(testClassNames, classPath)
    val missingOpcodes = t.expectedOpcodes.diff(opcodes)
    assert(missingOpcodes.isEmpty, s"Missing opcodes: ${opcodesToString(missingOpcodes)}")
    addUsedOpcodes(opcodes)

    // Compile to one big file
    val compiled = GoAheadCompiler.compile(
      t.conf,
      testClassNames,
      classPath
    ).copy(packageName = "spectest".toIdent)

    // Write the regular code in the spectest folder
    val codeFile = Files.createDirectories(tempFolder.resolve("spectest")).resolve("code.go")
    writeGoCode(t, codeFile, compiled)

    // Write the main call
    val className = t.classWithMain.getName
    val mainCode = GoAheadCompiler.compileMainFile(t.conf, "./spectest", className.replace('.', '/'), classPath)
    writeGoCode(t, tempFolder.resolve("main.go"), mainCode)

    // Compile go code
    var startMs = System.currentTimeMillis()
    val compiledExe =
      try compileDir(tempFolder)
      finally logger.info(s"Compilation time: ${(System.currentTimeMillis() - startMs) / 1000} seconds")
    logger.info("Exe size: " + Files.size(compiledExe))

    // Run it and check output
    startMs = System.currentTimeMillis()
    try assertExpectedOutput(compiledExe, t.expectedOutput.get)
    finally logger.debug(s"Run (and check) time: ${(System.currentTimeMillis() - startMs) / 1000} seconds")
  }

  def writeGoCode(testCase: TestCase, file: Path, code: Node.File): Unit = {
    def withLineNumbers(str: String) =
      str.split('\n').zipWithIndex.map(si => (si._2 + 1).toString.padTo(8, ' ') + si._1).mkString("\n")
    val codeStr = NodeWriter.fromNode(code)
    logger.debug(s"Asserting and writing the following to $file:\n${withLineNumbers(codeStr)}")
    if (checkFormatting) assertValidCode(testCase, codeStr)
    Files.write(file, codeStr.getBytes(StandardCharsets.UTF_8))
    // Discarding unneeded value above
    ()
  }

  def assertValidCode(testCase: TestCase, code: String): Unit = {
    val process = new ProcessBuilder("gofmt").start()
    val outReader = new BufferedReader(new InputStreamReader(process.getInputStream, StandardCharsets.UTF_8))
    val errReader = new BufferedReader(new InputStreamReader(process.getErrorStream, StandardCharsets.UTF_8))
    val writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream, StandardCharsets.UTF_8))
    try { writer.write(code); writer.flush() } finally { writer.close() }
    processWaitSuccess(process, goFormatTimeout)
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    val err = try { CharStreams.toString(errReader) } finally { errReader.close() }
    @inline
    def showFormattingError(): Unit = {
      val codeLines = code.split('\n')
      val outLines = out.split('\n')
      logger.debug(s"Formatting error ($err); full output:\n$out")
      codeLines.zipWithIndex.diff(outLines.zipWithIndex).foreach { case (_, lineIndex) =>
        logger.warn(s"Formatting diff on line ${lineIndex + 1} " +
          "(NOTE: binary expression spacing issues are a known problem currently)\n" +
          s"  Expected: ${outLines(lineIndex)}\n       Got: ${codeLines(lineIndex)}")
      }
      ()
    }
    Try({
      assert(err == "")
      assert(out == code)
      assert(process.exitValue == 0)
      ()
    }).recover({
      case _ if testCase.warnOnFormatError =>
        showFormattingError()
      case e =>
        showFormattingError()
        throw e
    }).get
  }

  def compileDir(dir: Path): Path = {
    val builder = new ProcessBuilder("go", "build", "-o", "test").directory(dir.toFile)
    // TODO: add the test workspace
    val goPaths = Seq(sys.env.getOrElse("GOPATH", sys.error("Can't find GOPATH env")), dir.toAbsolutePath.toString)
    val goPath = goPaths.mkString(File.pathSeparator)
    logger.debug(s"Setting GOPATH to $goPath")
    builder.environment().put("GOPATH", goPath)
    val process = builder.start()
    val outReader = new BufferedReader(new InputStreamReader(
      new SequenceInputStream(process.getInputStream, process.getErrorStream),
      StandardCharsets.UTF_8
    ))
    processWaitSuccess(process, goBuildTimeout)
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    Try({
      assert(out == "")
      assert(process.exitValue == 0)
    }).recover({ case e =>
      logger.error(s"Compilation error:\n$out")
      throw e
    }).get
    dir.resolve("test")
  }

  def assertExpectedOutput(exe: Path, expected: String): Unit = {
    val process = new ProcessBuilder(exe.toAbsolutePath.toString).start()
    val outReader = new BufferedReader(new InputStreamReader(
      new SequenceInputStream(process.getInputStream, process.getErrorStream),
      StandardCharsets.UTF_8
    ))
    processWaitSuccess(process, exeRunTimeout)
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    Try(assert(out == expected)).recover({ case e =>
      logger.error(s"Did not match expected output. Got:\n$out\n\nExpected:\n$expected")
      throw e
    }).get
    // Discarding unneeded value above
    ()
  }

  def processWaitSuccess(p: Process, timeout: FiniteDuration): Unit = {
    if (!p.waitFor(timeout.length, timeout.unit)) {
      p.destroyForcibly()
      fail("Timeout")
    }
  }
}

object CompilerSpec extends Logger {
  val useTestRt = true

  val testCases = {
    // TODO: maybe just read all classes out of the package dynamically?
    import goahead.testclasses._
    Seq(
      TestCase(classOf[Abstracts]),
      TestCase(classOf[AccessModifiers], classOf[goahead.testclasses.otherpkg.AccessModifiers]),
      TestCase(classOf[Arrays]),
      TestCase(classOf[Casts]),
      TestCase(classOf[Conditionals]),
      TestCase(classOf[CovariantReturn]),
      TestCase(classOf[FrameVars]),
      TestCase(classOf[HelloWorld]),
      TestCase(classOf[Inheritance]),
      TestCase(classOf[InheritanceConstructors]),
      TestCase(classOf[Initializers]),
      TestCase(classOf[InterfaceDefaults]),
      TestCase(classOf[Interfaces]),
      TestCase(classOf[Lambdas]), // TODO: test without optimization too
      TestCase(classOf[Literals]).asNonWindowsOnly(), // TODO: Windows too
      TestCase(classOf[LocalClasses]),
      TestCase(classOf[LocalVarReuse]),
      TestCase(classOf[NonStaticInnerClasses]),
      TestCase(classOf[Overflows]),
      TestCase(classOf[Primitives]),
      TestCase(classOf[PrimitiveWrappers]),
      TestCase(classOf[SigPolys]),
      TestCase(classOf[SimpleInstance]),
      TestCase(classOf[StackManips]),
      TestCase(classOf[StaticFields]),
      TestCase(classOf[Switches]),
      TestCase(classOf[TryCatch]),
      TestCase(classOf[UnusedLocalVar])
    )
  }

  val knownOpcodes = Printer.OPCODES.zipWithIndex.
    collect({ case (str, opcode) if str != null && str.nonEmpty => opcode -> str }).toMap
  var usedOpcodes = Set.empty[Int]

  def opcodesToString(opcodes: Set[Int]) =
    opcodes.map(knownOpcodes.apply).toSeq.sorted.mkString(",")

  def getOpcodes(internalClassNames: Seq[String], classPath: ClassPath) = {
    internalClassNames.flatMap({ className =>
      classPath.getFirstClass(className).cls.methods.flatMap(_.instructions.map(_.getOpcode).toSet).toSet
    }).toSet
  }

  def addUsedOpcodes(opcodes: Set[Int]) = synchronized {
    usedOpcodes ++= opcodes
  }

  def showUsedOpcodes(): Unit = {
    logger.warn(s"Tested ${usedOpcodes.size} of ${knownOpcodes.size} opcodes")
    logger.warn("Opcodes untested: " + opcodesToString(knownOpcodes.keySet.diff(usedOpcodes)))
  }

  case class TestCase(
    conf: Config,
    classes: Seq[Class[_]],
    expectedOutput: Option[String],
    warnOnFormatError: Boolean,
    nonWindowsOnly: Boolean = false
  ) {
    def subject =
      "Compiled classes " + classes.map(_.getName).mkString(", ")

    def provideExpectedOutput =
      // TODO: more than just stdout string
      s"provide the following output: $trimmedQuotedOutput"

    def trimmedQuotedOutput = expectedOutput match {
      case None => ""
      case Some(str) =>
        val flattened = Source.fromString(expectedOutput.get).getLines.mkString("\\n")
        val trimmed = if (flattened.length > 40) flattened.substring(0, 39) + "..." else flattened
        '"' + trimmed + '"'
    }

    lazy val expectedOpcodes = {
      classes.flatMap(c => Option(c.getAnnotation(classOf[ExpectOpcodes]))).flatMap(_.value()).toSet
    }

    def classWithMain = {
      val mains = classes.flatMap(c =>
        Try(c.getDeclaredMethod("main", classOf[Array[String]])).toOption
      ).filter(m => Modifier.isStatic(m.getModifiers))
      require(mains.size == 1, "Must only have a single main method")
      mains.head.getDeclaringClass
    }

    def asNonWindowsOnly() = copy(nonWindowsOnly = true)
  }

  object TestCase {
    lazy val allClassesInClassLoader = CP.from(classOf[goahead.testclasses.HelloWorld].getClassLoader).getAllClasses

    def apply(topClasses: Class[_]*): TestCase = {
      val allClasses = getAllClasses(topClasses)
      // Get the expected out from an annotation or from a run
      val expectedOutput = allClasses.flatMap(c => Option(c.getAnnotation(classOf[ExpectedOutput]))).headOption match {
        case Some(expected) =>
          // Just use the annotation
          expected.value
        case None =>
          // Actually run the program and get the output assuming we can find one with the main class
          val mainMeth = allClasses.flatMap(cls => Try(cls.getMethod("main", classOf[Array[String]])).toOption).headOption
          runAndGetOutput(mainMeth.getOrElse(sys.error("No main method")))
      }

      TestCase(
        conf = Config(reflection = if (useTestRt) Config.Reflection.None else Config.Reflection.All),
        classes = allClasses,
        expectedOutput = Some(expectedOutput),
        warnOnFormatError = topClasses.exists(_.isAnnotationPresent(classOf[WarnOnFormatError]))
      )
    }

    def getAllClasses(topClasses: Seq[Class[_]]): Seq[Class[_]] = {
      import scala.collection.JavaConverters._
      val classes = topClasses.flatMap(getSelfAndAllInnerClasses).toSet

      def isYetUnseenSubclass(name: String): Boolean = {
        val dollarIndex = name.indexOf('$')
        if (dollarIndex == -1) false else {
          val prefix = name.substring(0, dollarIndex)
          !classes.exists(_.getName == name) && classes.exists(_.getName == prefix)
        }
      }

      // We have to get all classes who's enclosing class is one of the classes
      val additionalClasses = allClassesInClassLoader.asScala.collect {
        case c if isYetUnseenSubclass(c.getName) => c.load()
      }

      (classes ++ additionalClasses).toSeq
    }

    def getSelfAndAllInnerClasses(cls: Class[_]): Seq[Class[_]] = {
      cls +: cls.getDeclaredClasses.flatMap(getSelfAndAllInnerClasses)
    }
  }

  def runAndGetOutput(method: java.lang.reflect.Method): String = synchronized {
    val existingOut = System.out
    val byteStream = new ByteArrayOutputStream()
    System.setOut(new PrintStream(byteStream))
    try {
      method.invoke(null, Array.empty[String])
      System.out.flush()
    } finally {
      System.setOut(existingOut)
    }
    byteStream.toString
  }
}
