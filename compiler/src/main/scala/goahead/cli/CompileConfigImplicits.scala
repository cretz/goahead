package goahead.cli

import com.typesafe.config._
import goahead.cli.CompileConfig._
import pureconfig._
import pureconfig.error.{CannotConvertNullException, WrongTypeException}
import pureconfig.syntax._

import scala.util.Try

object CompileConfigImplicits {

  implicit def conv[T] = ConfigFieldMapping.apply[T](CamelCase, KebabCase)

  def fromObject[T](fn: ConfigObject => T): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Try[T] = Try(config match {
      case obj: ConfigObject => fn(obj)
      case null => throw CannotConvertNullException
      case _ => throw WrongTypeException(config.valueType().toString)
    })

    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  case class ConfException(str: String, origin: ConfigOrigin)
    extends Exception(s"${origin.filename}:${origin.lineNumber} - $str")

  implicit val confClsConv: ConfigConvert[Seq[ConfCls]] = new ConfigConvert[Seq[ConfCls]] {
    override def from(config: ConfigValue): Try[Seq[ConfCls]] = {
      import scala.collection.JavaConverters._
      Try(config match {
        case null =>
          throw CannotConvertNullException
        case list: ConfigList =>
          list.asScala.flatMap(v => from(v).get)
        case v if v.valueType() == ConfigValueType.STRING =>
          Seq(ConfCls(v.unwrapped().toString))
        case obj: ConfigObject =>
          obj.asScala.toSeq.collect { case (k, v: ConfigObject) =>
            ConfCls(
              pattern = k,
              anyModifiers =
                if (v.containsKey("any-modifiers")) v.get("any-modifiers").to[Set[String]].get
                else Set.empty
            )
          }
      })
    }

    override def to(t: Seq[ConfCls]) = ConfigValueFactory.fromAnyRef(t)
  }

  implicit val fileGroupingConv: ConfigConvert[FileGrouping] =
    ConfigConvert.fromString(s => Try(FileGrouping.apply(s)))

  implicit val fieldManipConv: ConfigConvert[FieldManip] = fromObject[FieldManip] { obj =>
    if (obj.containsKey("exclude")) {
      obj.to[FieldManip.Exclude].get
    } else if (obj.containsKey("go")) {
      obj.to[FieldManip.GoType].get
    } else throw ConfException(s"Unknown field manip value", obj.origin())
  }

  implicit val methodManipConv: ConfigConvert[MethodManip] = fromObject[MethodManip] { obj =>
    if (obj.containsKey("as-is")) {
      require(obj.get("as-is").unwrapped() == java.lang.Boolean.TRUE, "Must have 'true' for 'as-is'")
      MethodManip.AsIs
    } else if (obj.containsKey("empty")) {
      require(obj.get("empty").unwrapped() == java.lang.Boolean.TRUE, "Must have 'true' for 'empty'")
      MethodManip.Empty
    } else if (obj.containsKey("exclude")) {
      obj.to[MethodManip.Exclude].get
    } else if (obj.containsKey("exclude-impl")) {
      obj.to[MethodManip.ExcludeImpl].get
    } else if (obj.containsKey("panic")) {
      require(obj.get("panic").unwrapped() == java.lang.Boolean.TRUE, "Must have 'true' for 'panic'")
      MethodManip.Panic
    } else if (obj.containsKey("go")) {
      obj.to[MethodManip.GoForward].get
    } else if (obj.containsKey("forward-from-out-folder")) {
      require(
        obj.get("forward-from-out-folder").unwrapped() == java.lang.Boolean.TRUE,
        "Must have 'true' for 'forward-from-out-folder'"
      )
      MethodManip.GoForwardFromOutFolder
    } else throw ConfException(s"Unknown method manip value", obj.origin())
  }

  implicit val fieldManipsConv: ConfigConvert[FieldManips] = fromObject[FieldManips] { obj =>
    import scala.collection.JavaConverters._
    FieldManips {
      obj.asScala.toSeq.flatMap {
        case (pattern, manipVals: ConfigList) =>
          val p = MemberPatternMatch(pattern)
          manipVals.asScala.map(v => p -> v.to[FieldManip].get)
        case (pattern, manipVal) =>
          Seq(MemberPatternMatch(pattern) -> manipVal.to[FieldManip].get)
      }
    }
  }

  implicit val methodManipsConv: ConfigConvert[MethodManips] = fromObject[MethodManips] { obj =>
    import scala.collection.JavaConverters._
    MethodManips {
      obj.asScala.toSeq.flatMap {
        case (pattern, manipVals: ConfigList) =>
          val p = MemberPatternMatch(pattern)
          manipVals.asScala.map(v => p -> v.to[MethodManip].get)
        case (pattern, manipVal) =>
          Seq(MemberPatternMatch.apply(pattern) -> manipVal.to[MethodManip].get)
      }
    }
  }

  implicit val classManipsConv: ConfigConvert[ClassManips] = fromObject[ClassManips] { obj =>
    import scala.collection.JavaConverters._
    val manips = obj.asScala.flatMap { case (pattern, manipVal) =>
      def manipFromObj(obj: ConfigValue): ClassManip =
        obj.to[ClassManip].get.copy(pattern = ClassPatternMatch.apply(pattern))
      manipVal match {
        case manipObj: ConfigObject => Seq(manipFromObj(manipObj))
        case manipList: ConfigList => manipList.asScala.map({
          case manipObj: ConfigObject => manipFromObj(manipObj)
          case other => throw ConfException(s"Expected object type, got: ${other.valueType()}", other.origin())
        })
      }
    }
    ClassManips(manips.toSeq.sortWith(_.priority > _.priority))
  }

  implicit val reflectionConv: ConfigConvert[goahead.compile.Config.Reflection] =
    ConfigConvert.fromString(s => Try(goahead.compile.Config.Reflection.apply(s)))
}

