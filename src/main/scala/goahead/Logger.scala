package goahead

import java.net.URL

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory

trait Logger extends LazyLogging
object Logger {
  def setLogConfFile(path: Either[URL, String]) = {
    val ctx = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val conf = new JoranConfigurator
    conf.setContext(ctx)
    ctx.reset()
    path match {
      case Left(url) => conf.doConfigure(url)
      case Right(fileName) => conf.doConfigure(fileName)
    }
  }
}
