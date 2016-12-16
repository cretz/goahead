package goahead.cli

import goahead.Logger

import scala.util.{Failure, Success}

object Main extends Logger {
  val commands = Seq[Command](
    BuildJvmStubs,
    BuildStubs,
    BuildRt,
    FindOpcode
  )

  val usage = s"""
    |Usage:
    |  COMMAND -c file
    |
    |  COMMAND options...
    |Commands:
    |  ${commands.map(_.name).mkString("\n  ")}
  """.stripMargin.trim

  def run(origArgs: Seq[String]): Unit = {
    val args = configureLogging(origArgs)
    logger.trace(s"Args: $args")
    if (args.isEmpty) println(usage) else commands.find(_.name == args.head) match {
      case None =>
        println("Error: Unrecognized command: " + args.head)
        println(usage)
      case Some(command) => command.run(args.tail) match {
        case Failure(f: Args.Failure) =>
          if (f.errs.nonEmpty) {
            println("Errors:")
            f.errs.foreach(e => println("  " + e))
          }
          println("Usage:")
          println("  " + command.name + " " + f.usageStartingWithArgs.replace("\n", "\n  "))
          sys.exit(1)
        case Failure(e) =>
          def errMsg(e: Throwable, indent: Int = 0): String = {
            if (e.getCause == null) e.getMessage
            else e.getMessage + "\n" + (" " * indent) + errMsg(e.getCause, indent + 1)
          }
          println("Error: " + errMsg(e))
          if (logger.underlying.isDebugEnabled) logger.debug("Execution failed", e)
          else println("Run with -vv for more details")
          sys.exit(1)
        case Success(_) =>
          logger.trace("App complete successfully")
      }
    }
  }

  def configureLogging(args: Seq[String]): Seq[String] = {
    if (sys.props.contains("logback.configurationFile")) args
    else if (sys.env.contains("LOGBACK_CONF")) {
      Logger.setLogConfFile(Right(sys.env("LOGBACK_CONF")))
      args
    } else args.indexOf("-logback-config") match {
      case configArgIndex if configArgIndex != -1 && configArgIndex < args.length - 1 =>
        Logger.setLogConfFile(Right(args(configArgIndex + 1)))
        args.take(configArgIndex) ++ args.drop(configArgIndex + 2)
      case _ =>
        if (!sys.props.contains("logback.rootLevel")) sys.props.put("logback.rootLevel", "warn")
        val updatedArgs = args.filter {
          case "-v" => sys.props.put("logback.rootLevel", "info"); false
          case "-vv" => sys.props.put("logback.rootLevel", "debug"); false
          case "-vvv" => sys.props.put("logback.rootLevel", "trace"); false
          case _ => true
        }
        Logger.setLogConfFile(Left(getClass.getResource("/cli-logback.xml")))
        updatedArgs
    }
  }

  def main(args: Array[String]) = run(args)
}
