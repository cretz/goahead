package goahead.cli

import goahead.Logger

class BuildStubs extends Command with Logger {
  override val name = "build-stubs"

  case class Conf(
  )

  override def argParser = { implicit builder =>
    Conf(

    )
  }

  override def confLoader = pureconfig.loadConfig[Conf]

  override def run(conf: Conf): Unit = {

  }
}
