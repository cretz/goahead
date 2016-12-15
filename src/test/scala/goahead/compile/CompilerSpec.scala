package goahead.compile

import java.io._
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import com.google.common.io.CharStreams
import goahead.ast.{Node, NodeWriter}
import goahead.{BaseSpec, ExpectedOutput, Logger}
import org.objectweb.asm.util.Printer
import org.scalatest.BeforeAndAfterAll

import scala.io.Source
import scala.util.Try

class CompilerSpec extends BaseSpec with BeforeAndAfterAll {
  import AstDsl._
  import CompilerSpec._

  // Create this entry for the "rt" classes and close at the end
  val javaRuntimeEntry = ClassPath.Entry.fromJarFile(
    ClassPath.Entry.javaRuntimeJarPath,
    //"rt"
    "github.com/cretz/goahead/javalib/src/rt"
  )

  override protected def afterAll() = {
    javaRuntimeEntry.close()
    showUsedOpcodes()
  }

  // Run each test case as its own setup
  testCases.foreach{t => t.subject should behave like expected(t)}

  def expected(t: CompilerSpec.TestCase) = it should t.provideExpectedOutput in withTemporaryFolder { tempFolder =>
    val testClasses = t.classes.map(ClassPath.Entry.fromLocalClass(_, ""))

    // Set classpath with helper classes
    val classPath = ClassPath(testClasses :+ javaRuntimeEntry)
    val testClassNames = testClasses.map(_.cls.name)

    addUsedOpcodes(testClassNames, classPath)

    // Compile to one big file
    val compiled = GoAheadCompiler.compile(
      testClassNames,
      classPath
    ).copy(packageName = "spectest".toIdent)

    // Write the regular code in the spectest folder
    val codeFile = Files.createDirectories(tempFolder.resolve("spectest")).resolve("code.go")
    writeGoCode(t, codeFile, compiled)

    // Write the main call
    val className = t.classes.find(c => Try(c.getMethod("main", classOf[Array[String]])).isSuccess).get.getName
    val mainCode = GoAheadCompiler.compileMainFile("./spectest", className.replace('.', '/'), classPath)
    writeGoCode(t, tempFolder.resolve("main.go"), mainCode)

    // Compile go code
    val compiledExe = compileDir(tempFolder)

    // Run it and check output
    assertExpectedOutput(compiledExe, t.expectedOutput.get)
  }

  def writeGoCode(testCase: TestCase, file: Path, code: Node.File): Unit = {
    val codeStr = NodeWriter.fromNode(code)
    logger.debug(s"Asserting and writing the following to $file:\n$codeStr")
    assertValidCode(testCase, codeStr)
    Files.write(file, codeStr.getBytes(StandardCharsets.UTF_8))
    // Discarding unneeded value above
    ()
  }

  def assertValidCode(testCase: TestCase, code: String): Unit = {
    val process = new ProcessBuilder("gofmt").start()
    val outReader = new BufferedReader(new InputStreamReader(process.getInputStream))
    val errReader = new BufferedReader(new InputStreamReader(process.getErrorStream))
    val writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream))
    try { writer.write(code); writer.flush() } finally { writer.close() }
    process.waitFor(4, TimeUnit.SECONDS)
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    val err = try { CharStreams.toString(errReader) } finally { errReader.close() }
    Try({
      assert(err == "")
      assert(out == code)
      assert(process.exitValue == 0)
      ()
    }).recover({
      case _ if testCase.warnOnFormatError =>
        val codeLines = code.split('\n')
        val outLines = out.split('\n')
        logger.debug(s"Formatting error ($err); full output:\n$out")
        codeLines.zipWithIndex.diff(outLines.zipWithIndex).foreach { case (_, lineIndex) =>
          logger.warn(s"Formatting diff on line ${lineIndex + 1} " +
            "(NOTE: binary expression spacing issues are a known problem currently)\n" +
            s"  Expected: ${outLines(lineIndex)}\n       Got: ${codeLines(lineIndex)}")
        }
        ()
      case e =>
        logger.error(s"Formatting error ($err); full output:\n$out")
        throw e
    }).get
  }

  def compileDir(dir: Path): Path = {
    val builder = new ProcessBuilder("go", "build", "-o", "test").directory(dir.toFile)
    // TODO: add the test workspace
    val goPaths = Seq(dir.toAbsolutePath.toString, sys.env.getOrElse("GOPATH", sys.error("Can't find GOPATH env")))
    val goPath = goPaths.mkString(File.pathSeparator)
    logger.debug(s"Setting GOPATH to $goPath")
    builder.environment().put("GOPATH", goPath)
    val process = builder.start()
    val outReader = new BufferedReader(
      new InputStreamReader(new SequenceInputStream(process.getInputStream, process.getErrorStream))
    )
    assert(process.waitFor(5, TimeUnit.SECONDS))
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
    val outReader = new BufferedReader(
      new InputStreamReader(new SequenceInputStream(process.getInputStream, process.getErrorStream))
    )
    assert(process.waitFor(5, TimeUnit.SECONDS))
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    Try(assert(out == expected)).recover({ case e =>
      logger.error(s"Did not match expected output. Got:\n$out\n\nExpected:\n$expected")
      throw e
    }).get
    // Discarding unneeded value above
    ()
  }
}

object CompilerSpec extends Logger {
  val testCases = {
    // TODO: maybe just read all classes out of the package dynamically?
    import goahead.testclasses._
    Seq(
      TestCase(classOf[Arrays]),
      TestCase(classOf[Casts]),
      TestCase(classOf[HelloWorld]),
      TestCase(classOf[Inheritance]),
      TestCase(classOf[InheritanceConstructors]),
      TestCase(classOf[Interfaces]),
      TestCase(classOf[Primitives]),
      TestCase(classOf[SimpleInstance]),
      TestCase(classOf[StaticFields]),
      TestCase(classOf[TryCatch])
    )
  }

  val knownOpcodes = Printer.OPCODES.zipWithIndex.
    collect({ case (str, opcode) if str != null && str.nonEmpty => opcode -> str }).toMap
  var usedOpcodes = Set.empty[Int]

  def addUsedOpcodes(internalClassNames: Seq[String], classPath: ClassPath) = synchronized {
    usedOpcodes ++= internalClassNames.flatMap { className =>
      classPath.getFirstClass(className).cls.methods.flatMap(_.instructions.map(_.getOpcode).toSet).toSet
    }
  }

  def showUsedOpcodes(): Unit = {
    logger.info(s"Tested ${usedOpcodes.size} of ${knownOpcodes.size} opcodes")
    logger.info(
      "Opcodes untested: " + knownOpcodes.keySet.diff(usedOpcodes).map(knownOpcodes.apply).toSeq.sorted.mkString(",")
    )
  }

  case class TestCase(
    classes: Seq[Class[_]],
    expectedOutput: Option[String],
    warnOnFormatError: Boolean
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
  }

  object TestCase {
    def apply(topClasses: Class[_]*): TestCase = {
      val allClasses = topClasses.flatMap(getSelfAndAllInnerClasses).distinct
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
        classes = allClasses,
        expectedOutput = Some(expectedOutput),
        warnOnFormatError = topClasses.exists(_.isAnnotationPresent(classOf[goahead.WarnOnFormatError]))
      )
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
