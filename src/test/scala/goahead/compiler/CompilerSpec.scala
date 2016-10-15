package goahead.compiler

import java.io._
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import com.google.common.io.{ByteStreams, CharStreams}
import goahead.ast.{Node, NodeWriter}
import goahead.{BaseSpec, ExpectedOutput}
import goahead.testclasses.HelloWorld

import scala.util.Try
import org.scalatest.Assertions._

class CompilerSpec extends BaseSpec {

  // Run each test case as its own setup
  CompilerSpec.testCases.foreach{t => t.subject should behave like expected(t)}

  def expected(t: CompilerSpec.TestCase) = it should t.provideExpectedOutput in withTemporaryFolder { tempFolder =>
    // Instantiate the compiler
    val compiler = new Compiler(ClassPath(Nil))

    // Compile each one
    val compiled = t.classes.map { cls =>
      compiler.classFileToOutFile {
        val inStream = cls.getResourceAsStream(cls.getSimpleName + ".class")
        try { ByteStreams.toByteArray(inStream) } finally { inStream.close() }
      }
    }

    // Write them in the temp folder
    compiled.foreach { outFile =>

      // Let's first make sure the formatting is good
      outFile.bootstrapMain.foreach(assertValidCode)
      val code = assertValidCode(outFile.file)

      // Now write it
      // TODO: what does bootstrap main do?
      val dir = Files.createDirectories(tempFolder.resolve(outFile.dir))
      Files.write(dir.resolve(outFile.filename), code.getBytes(StandardCharsets.UTF_8))
    }

    // Compile go code
    val compiledExe = compileDir(tempFolder)

    // Run it and check output
    assertExpectedOutput(compiledExe, t.expectedOutput.get)
  }

  def assertValidCode(file: Node.File): String = {
    val code = NodeWriter.fromNode(file)
    val process = new ProcessBuilder("gofmt").start()
    val outReader = new BufferedReader(new InputStreamReader(process.getInputStream))
    val errReader = new BufferedReader(new InputStreamReader(process.getErrorStream))
    val writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream))
    try { writer.write(code) } finally { writer.close() }
    assert(process.waitFor(5, TimeUnit.SECONDS))
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    val err = try { CharStreams.toString(errReader) } finally { errReader.close() }
    assert(err == "")
    assert(out == code)
    assert(process.exitValue == 0)
    code
  }

  def compileDir(dir: Path): Path = {
    val builder = new ProcessBuilder("go", "build", "-o", "test").directory(dir.toFile)
    // TODO: add the test workspace
    //builder.environment().put("GOPATH", File(".", "etc/testworkspace").absolutePath +
    //  File.pathSeparatorChar + tempFolderFile.absolutePath
    val process = builder.start()
    val outReader = new BufferedReader(
      new InputStreamReader(new SequenceInputStream(process.getInputStream, process.getErrorStream))
    )
    assert(process.waitFor(5, TimeUnit.SECONDS))
    val out = try { CharStreams.toString(outReader) } finally { outReader.close() }
    assert(out == "")
    assert(process.exitValue == 0)
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

object CompilerSpec {
  val testCases = Seq(
    TestCase(classOf[HelloWorld])
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
