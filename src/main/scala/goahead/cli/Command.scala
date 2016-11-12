package goahead.cli

import scala.concurrent.ExecutionContext

trait Command {

  type Conf

  def name: String

  def run(args: Seq[String]): Option[Args.Failure] = {
    loadConf(args) match {
      case Left(failure) =>
        Some(failure)
      case Right(conf) =>
        run(conf)
        None
    }
  }

  def loadConf(args: Seq[String]): Either[Args.Failure, Conf] = Args.validated(args, argParser)

  def run(conf: Conf): Unit

  implicit val execCtx: ExecutionContext = ExecutionContext.global

  def argParser: Args.Builder => Conf
}
