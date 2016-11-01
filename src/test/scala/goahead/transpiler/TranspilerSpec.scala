package goahead.transpiler

import java.io._
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

import com.google.common.io.{ByteStreams, CharStreams}
import goahead.ast.{Node, NodeWriter}
import goahead.{BaseSpec, ExpectedOutput}
import goahead.testclasses._

import scala.util.Try
import org.scalatest.Assertions._

class TranspilerSpec extends BaseSpec {

  val defaultClassDirs = Map(
    "java/lang/NullPointerException" -> "rt",
    "java/lang/Object" -> "rt",
    "java/lang/String" -> "rt",
    "java/lang/StringBuilder" -> "rt",
    "java/lang/System" -> "rt"
  )

  // Run each test case as its own setup
  TranspilerSpec.testCases.foreach{t => t.subject should behave like expected(t)}

  def expected(t: TranspilerSpec.TestCase) = it should t.provideExpectedOutput in withTemporaryFolder { tempFolder =>
    // Instantiate the compiler w/ classpath having the local classes
    val transpiler = new Transpiler(
      ClassPath(
        defaultClassDirs ++ t.classes.map(c => c.getName.replace('.', '/') -> ""),
        Seq.empty
      )
    )

    // Compile to one big file
    val transpiled = transpiler.classesToFile(t.classes.map { cls =>
      val inStream = cls.getResourceAsStream(cls.getSimpleName + ".class")
      try { ByteStreams.toByteArray(inStream) } finally { inStream.close() }
    }).copy(packageName = Node.Identifier("spectest"))

    // Write the regular code in the spectest folder
    val codeFile = Files.createDirectories(tempFolder.resolve("spectest")).resolve("code.go")
    writeGoCode(codeFile, transpiled)

    // Write the main call
    val className = t.classes.find(c => Try(c.getMethod("main", classOf[Array[String]])).isSuccess).get.getName
    val mainCode = transpiler.buildMainFile("./spectest", className.replace('.', '/'))
    writeGoCode(tempFolder.resolve("main.go"), mainCode)

    // Compile go code
    val compiledExe = compileDir(tempFolder)

    // Run it and check output
    assertExpectedOutput(compiledExe, t.expectedOutput.get)
  }

  def writeGoCode(file: Path, code: Node.File): Unit = {
    val codeStr = NodeWriter.fromNode(code)
    logger.debug(s"Asserting and writing the following to $file:\n$codeStr")
    assertValidCode(codeStr)
    Files.write(file, codeStr.getBytes(StandardCharsets.UTF_8))
  }

  def assertValidCode(code: String): Unit = {
    val process = new ProcessBuilder("gofmt").start()
    val outReader = new BufferedReader(new InputStreamReader(process.getInputStream))
    val errReader = new BufferedReader(new InputStreamReader(process.getErrorStream))
    val writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream))
    try { writer.write(code) } finally { writer.close() }
    assert(process.waitFor(5, TimeUnit.SECONDS))
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    val err = try { CharStreams.toString(errReader) } finally { errReader.close() }
    Try({
      assert(err == "")
      assert(out == code)
      assert(process.exitValue == 0)
    }).recover({ case e =>
      logger.error(s"Formatting error ($err):\n$out")
      throw e
    }).get
  }

  def compileDir(dir: Path): Path = {
    val builder = new ProcessBuilder("go", "build", "-o", "test").directory(dir.toFile)
    // TODO: add the test workspace
    val goPaths = Seq(Paths.get("etc/testworkspace"), dir)
    val goPath = goPaths.map(_.toAbsolutePath.toString).mkString(File.pathSeparator)
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
    assert(out == expected)
  }
}

object TranspilerSpec {
  val testCases = Seq(
//    TestCase(classOf[HelloWorld]),
//    TestCase(classOf[StaticFields]),
//    TestCase(classOf[SimpleInstance]),
    TestCase(classOf[TryCatch])
  )

  case class TestCase(
    classes: Seq[Class[_]],
    expectedOutput: Option[String]
  ) {
    def subject =
      "Compiled classes " + classes.map(_.getSimpleName).mkString(",")

    def provideExpectedOutput =
      // TODO: more than just stdout string
      "provide the following output: " + expectedOutput.get
  }

  object TestCase {
    def apply(classes: Class[_]*): TestCase = {
      // Get the expected out from an annotation or from a run
      val expectedOutput = classes.flatMap(c => Option(c.getAnnotation(classOf[ExpectedOutput]))).headOption match {
        case Some(expected) =>
          // Just use the annotation
          expected.value
        case None =>
          // Actually run the program and get the output assuming we can find one with the main class
          val mainMeth = classes.flatMap(cls => Try(cls.getMethod("main", classOf[Array[String]])).toOption).headOption
          runAndGetOutput(mainMeth.getOrElse(sys.error("No main method")))
      }

      TestCase(
        classes = classes,
        expectedOutput = Some(expectedOutput)
      )
    }
  }

  def runAndGetOutput(method: Method): String = {
    val existingOut = System.out
    val byteStream = new ByteArrayOutputStream()
    val printStream = new PrintStream(byteStream)
    System.setOut(printStream)
    try {
      method.invoke(null, Array.empty[String])
      System.out.flush()
    } finally {
      System.setOut(existingOut)
    }
    byteStream.toString
  }
}
