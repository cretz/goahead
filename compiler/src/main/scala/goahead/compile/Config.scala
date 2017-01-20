package goahead.compile

case class Config(
  optimizeRecognizedInvokeDynamic: Boolean = true,
  localizeTempVarUsage: Boolean = false,
  reflection: Config.Reflection = Config.Reflection.ClassName
)

object Config {
  sealed trait Reflection
  object Reflection {
    def apply(str: String): Reflection = str match {
      case "all" => All
      case "none" => None
      case "class-name" => ClassName
      case _ => sys.error(s"Unrecognized reflection type: $str")
    }

    case object All extends Reflection
    case object None extends Reflection
    case object ClassName extends Reflection
  }
}