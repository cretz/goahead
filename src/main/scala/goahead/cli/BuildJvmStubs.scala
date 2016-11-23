package goahead.cli

import goahead.Logger
import goahead.compile.interop.GoPath

trait BuildJvmStubs extends Command with Logger {
  override val name = "build-jvm-stubs"

  case class Conf(
    importPath: String,
    packageName: String,
    outSrcDir: String
  )

  override def argParser = { implicit builder =>
    Conf(
      importPath = builder.trailingOpt(
        name = "import-path",
        required = true,
        desc = "The Go import path (relative to GOPATH) to generate stubs for"
      ).get,
      packageName = builder.trailingOpt(
        name = "package-name",
        required = true,
        desc = "The package name to use"
      ).get,
      outSrcDir = builder.trailingOpt(
        name = "out-src-dir",
        required = true,
        desc = "The src directory to output to (package name is then appended)"
      ).get
    )
  }

  override def confLoader = pureconfig.loadConfig[Conf]

  override def run(conf: Conf): Unit = {
    val pkg = GoPath().loadPackage(conf.importPath)
    println("YAY: " + pkg)
    ()
  }
}

object BuildJvmStubs extends BuildJvmStubs