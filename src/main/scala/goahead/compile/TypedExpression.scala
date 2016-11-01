package goahead.compile

import goahead.ast.Node
import org.objectweb.asm
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{Label, Opcodes}

sealed trait TypedExpression {
  def expr: Node.Expression
  def typ: TypedExpression.Type
  def cheapRef: Boolean
}

object TypedExpression {
  def apply(expr: Node.Expression, typ: asm.Type, cheapRef: Boolean): TypedExpression =
    apply(expr, Type(typ), cheapRef)

  def apply(expr: Node.Expression, typ: Type, cheapRef: Boolean): TypedExpression =
    Simple(expr, typ, cheapRef)

  case class Simple(
    expr: Node.Expression,
    typ: Type,
    cheapRef: Boolean
  ) extends TypedExpression

  sealed trait Type

  object Type {
    def apply(typ: asm.Type): Type = Simple(typ)

    def fromFrameVarType(thisNode: ClassNode, typ: Any) = typ match {
      case Opcodes.TOP => Undefined
      case Opcodes.INTEGER => Type(asm.Type.INT_TYPE)
      case Opcodes.FLOAT => Type(asm.Type.FLOAT_TYPE)
      case Opcodes.DOUBLE => Type(asm.Type.DOUBLE_TYPE)
      case Opcodes.LONG => Type(asm.Type.LONG_TYPE)
      case Opcodes.NULL => NullType
      case Opcodes.UNINITIALIZED_THIS => Type(asm.Type.getObjectType(thisNode.name))
      case ref: String => Type(asm.Type.getObjectType(ref))
      // TODO: investigate more... we need to come back and fix this type once we see the label
      case l: Label => UndefinedLabelInitialized(l)
      case v => sys.error(s"Unrecognized frame var type $v")
    }

    case object NullType extends Type

    case class Simple(typ: asm.Type) extends Type

    case object Undefined extends Type

    case class UndefinedLabelInitialized(label: Label) extends Type
  }
}
