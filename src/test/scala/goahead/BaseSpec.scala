package goahead

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

import org.scalatest._

import scala.util.Try

trait BaseSpec extends FlatSpec with Logger {

  def withTemporaryFolder(f: Path => Any): Unit = {
    val dir = Files.createTempDirectory("goahead")
    logger.debug(s"Created temporary directory $dir")
    try {
      f(dir)
    } finally {
      Try(Files.walkFileTree(dir, BaseSpec.DeleteAllVisitor))
      logger.debug(s"Deleted temporary directory $dir")
    }
  }
}

object BaseSpec {
  object DeleteAllVisitor extends SimpleFileVisitor[Path] {
    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      Files.delete(file)
      FileVisitResult.CONTINUE
    }

    override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
      Files.delete(dir)
      FileVisitResult.CONTINUE
    }
  }
}