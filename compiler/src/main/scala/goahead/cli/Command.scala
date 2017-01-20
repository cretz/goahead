package goahead.cli

import java.nio.file.{Path, Paths}

import scala.concurrent.ExecutionContext
import scala.util.Try

trait Command {

  type Conf

  def name: String

  def run(args: Seq[String]): Try[Unit] = {
    loadConf(args).map(run)
  }

  def loadConf(args: Seq[String]): Try[Conf] = {
    args.indexOf("-c") match {
      case -1 =>
        loadConfFromArgs(args)
      case index if index != 0 || args.length != 2 =>
        Try { sys.error("Missing configuration file as only param") }
      case index =>
        loadConfFromFile(args.last)
    }
  }

  def loadConfFromArgs(args: Seq[String]): Try[Conf] = Args.validated(args, argParser)

  def loadConfFromFile(file: String): Try[Conf] = confLoader(Paths.get(file))

  def run(conf: Conf): Unit

  implicit val execCtx: ExecutionContext = ExecutionContext.global

  def argParser: Args.Builder => Conf
  def confLoader: Path => Try[Conf]

  def usage: String = s"Usage:\n  $name " + Args.usageStartingWithArgs(argParser).replace("\n", "\n  ")
}
