package goahead.compile.interop

sealed trait Decl {
  def name: String
}

object Decl {
  case class Constant(
    name: String,
    value: String,
    typ: Type
  ) extends Decl

  case class Function(
    name: String,
    rec: Option[Type],
    typ: FuncType
  ) extends Decl

  case class Interface(
    name: String,
    functions: Seq[(String, FuncType)]
  ) extends Decl

  case class Struct(
    name: String,
    fields: Seq[(String, Type)]
  ) extends Decl

  case class TypeAlias(
    name: String,
    typ: Type
  ) extends Decl

  sealed trait Type
  sealed trait BuiltInType extends Type
  object BuiltInType {
    case object String extends BuiltInType
  }

  case class PointerType(
    typ: Type
  ) extends Type

  case class QualifiedType(
    pkg: String,
    name: String
  ) extends Type

  case class ArrayType(
    typ: Type,
    dimensions: Option[Int] = None
  ) extends Type

  case class FuncType(
    params: Seq[(String, Type)],
    results: Seq[Type]
  ) extends Type
}
