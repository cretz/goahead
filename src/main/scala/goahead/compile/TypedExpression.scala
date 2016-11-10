package goahead.compile

import goahead.ast.Node

sealed trait TypedExpression {
  def expr: Node.Expression
  def typ: IType
  def cheapRef: Boolean

  def name: String = expr match {
    case Node.Identifier(name) => name
    case _ => sys.error(s"Trying to get name from non ident: $expr")
  }

  def withMaybeMoreSpecificType(classPath: ClassPath, other: IType) =
    withUpdatedType(typ.maybeMakeMoreSpecific(classPath, other))

  def withUpdatedType(typ: IType): TypedExpression
}

object TypedExpression {
  def apply(expr: Node.Expression, typ: IType, cheapRef: Boolean): TypedExpression =
    Simple(expr, typ, cheapRef)

  def namedVar(name: String, typ: IType) = apply(Node.Identifier(name), typ, cheapRef = true)

  case class Simple(
    expr: Node.Expression,
    typ: IType,
    cheapRef: Boolean
  ) extends TypedExpression {
    override def withUpdatedType(typ: IType): TypedExpression = copy(typ = typ)
  }
}
