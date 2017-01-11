package goahead.compile.interop

import java.nio.file.Paths

import goahead.Logger

trait GoParser extends Logger {

  def loadPackage(importPath: String, goPath: GoPath): Package = {
    import sys.process._
    val cmd = Seq("go", "run", "javalib/src/tools/ast_json.go", importPath)
    val extraEnv = "GOPATH" -> goPath.envString(Paths.get("javalib").toAbsolutePath)
    val proc = Process(cmd, None, extraEnv)
    logger.debug(s"Running '$cmd' with env: $extraEnv")
    val goOut = proc.!!
    logger.trace(s"Result:\n$goOut")
    Package.fromJson(goOut)
  }
}

object GoParser extends GoParser