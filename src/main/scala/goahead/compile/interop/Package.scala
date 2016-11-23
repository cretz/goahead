package goahead.compile.interop

case class Package(name: String, decls: Seq[Package.Decl])

object Package {

  def fromJson(json: String): Package = {
    import io.circe._, io.circe.parser._
    val decoded = decode[Json](json).right.flatMap { json =>
      JsonHelper.packageDecoder.decodeJson(JsonHelper.uncapitalizeKeys(json))
    }
    decoded match {
      case Left(err) => throw new Exception("Failed reading package JSON", err)
      case Right(pkg) => pkg
    }
  }

  object JsonHelper {
    import io.circe._
    import io.circe.generic.auto._

    def uncapitalizeKeys(json: Json): Json = {
      json.mapObject({ obj =>
        JsonObject.fromMap(obj.toMap.map { case (key, value) =>
          (key.head.toLower + key.tail) -> uncapitalizeKeys(value)
        })
      }).mapArray(_.map(uncapitalizeKeys))
    }

    implicit class RichJson(val json: Json) extends AnyVal {
      def stringField(name: String) = json.asObject.flatMap(_.apply(name).flatMap(_.asString)).
        getOrElse(sys.error(s"Unable to find field $name"))
    }

    implicit val kindBasedDeclDecoder: Decoder[Decl] = Decoder.decodeJson.flatMap { json =>
      val dec = json.stringField("kind") match {
        case "const" => implicitly[Decoder[ConstDecl]]
        case "var" => implicitly[Decoder[VarDecl]]
        case "func" => implicitly[Decoder[FuncDecl]]
        case "struct" => implicitly[Decoder[StructDecl]]
        case "iface" => implicitly[Decoder[IfaceDecl]]
        case "alias" => implicitly[Decoder[AliasDecl]]
      }
      dec.asInstanceOf[Decoder[Decl]]
    }

    implicit val kindBasedTypeDecoder: Decoder[Type] = Decoder.decodeJson.flatMap { json =>
      val dec = json.stringField("kind") match {
        case "qualified" => implicitly[Decoder[QualifiedType]]
        case "func" => implicitly[Decoder[FuncType]]
        case "basic" => implicitly[Decoder[BasicType]]
        case "array" => implicitly[Decoder[ArrayType]]
        case "slice" => implicitly[Decoder[SliceType]]
        case "struct" => implicitly[Decoder[StructType]]
        case "pointer" => implicitly[Decoder[PointerType]]
        case "iface" => implicitly[Decoder[IfaceType]]
        case "map" => implicitly[Decoder[MapType]]
        case "chan" => implicitly[Decoder[ChanType]]
      }
      dec.asInstanceOf[Decoder[Type]]
    }

    implicit val packageDecoder = implicitly[Decoder[Package]]
  }

  sealed trait Decl {
    def name: String
  }

  case class ConstDecl(
    name: String,
    typ: Type,
    value: String
  ) extends Decl

  case class VarDecl(
    name: String,
    typ: Type
  ) extends Decl

  case class FuncDecl(
    name: String,
    params: Seq[NamedType],
    results: Seq[NamedType],
    variadic: Boolean
  ) extends Decl

  case class StructDecl(
    name: String,
    fields: Seq[NamedType],
    methods: Seq[Method]
  ) extends Decl

  case class IfaceDecl(
    name: String,
    embedded: Seq[Type],
    methods: Seq[FuncType]
  ) extends Decl

  case class AliasDecl(
    name: String,
    typ: Type,
    methods: Seq[Method]
  ) extends Decl

  case class NamedType(name: Option[String], typ: Type)

  case class Method(
    name: String,
    pointer: Boolean,
    params: Seq[NamedType],
    results: Seq[NamedType],
    variadic: Boolean
  )

  sealed trait Type

  case class QualifiedType(
    pkg: Option[String],
    name: String
  ) extends Type

  case class FuncType(
    params: Seq[NamedType],
    results: Seq[NamedType],
    variadic: Boolean
  ) extends Type

  case class BasicType(
    name: String,
    untyped: Boolean
  ) extends Type

  case class ArrayType(
    size: Long,
    typ: Type
  ) extends Type

  case class SliceType(
    typ: Type
  ) extends Type

  case class StructType(
    fields: Seq[NamedType],
    methods: Seq[Method]
  ) extends Type

  case class PointerType(
    typ: Type
  ) extends Type

  case class IfaceType(
    embedded: Seq[Type],
    methods: Seq[FuncType]
  ) extends Type

  case class MapType(
    key: Type,
    value: Type
  ) extends Type

  case class ChanType(
    typ: Type,
    send: Boolean,
    receive: Boolean
  ) extends Type
}
