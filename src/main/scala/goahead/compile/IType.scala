package goahead.compile

import goahead.Logger
import org.objectweb.asm.{Label, Opcodes, Type}

sealed trait IType {
  def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType

  def isAssignableFrom(classPath: ClassPath, other: IType): Boolean

  def pretty: String

  def asArray(dimensions: Int = 1): IType

  // Fails is not array
  def elementType: IType
}
object IType extends Logger {
  def apply(typ: Type): IType = Simple(typ)

  def getArgumentAndReturnTypes(methodDesc: String): (Seq[IType], IType) = {
    val asmType = Type.getMethodType(methodDesc)
    asmType.getArgumentTypes.map(IType.apply).toSeq -> IType(asmType.getReturnType)
  }
  def getArgumentTypes(methodDesc: String) = Type.getArgumentTypes(methodDesc).map(IType.apply).toSeq
  def getObjectType(internalName: String) = IType(Type.getObjectType(internalName))
  def getReturnType(methodDesc: String) = IType(Type.getReturnType(methodDesc))
  def getType(desc: String) = IType(Type.getType(desc))
  def getType(cls: Class[_]) = IType(Type.getType(cls))

  val VoidType = IType(Type.VOID_TYPE)
  val BooleanType = IType(Type.BOOLEAN_TYPE)
  val ByteType = IType(Type.BYTE_TYPE)
  val CharType = IType(Type.CHAR_TYPE)
  val IntType = IType(Type.INT_TYPE)
  val FloatType = IType(Type.FLOAT_TYPE)
  val DoubleType = IType(Type.DOUBLE_TYPE)
  val LongType = IType(Type.LONG_TYPE)
  val ShortType = IType(Type.SHORT_TYPE)

  def fromFrameVarType(thisNode: Cls, typ: Any) = typ match {
    case Opcodes.TOP => Undefined
    case Opcodes.INTEGER => IntType
    case Opcodes.FLOAT => FloatType
    case Opcodes.DOUBLE => DoubleType
    case Opcodes.LONG => LongType
    case Opcodes.NULL => NullType
    case Opcodes.UNINITIALIZED_THIS => IType(Type.getObjectType(thisNode.name))
    case ref: String => IType(Type.getObjectType(ref))
    // TODO: investigate more... we need to come back and fix this type once we see the label
    case l: Label => UndefinedLabelInitialized(l)
    case v => sys.error(s"Unrecognized frame var type $v")
  }

  case class Simple(typ: Type) extends IType {
    // TODO: check this deeper to get subclasses and what not
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = {
      if (isAssignableFrom(classPath, other)) other
      else this
    }

    override def isAssignableFrom(classPath: ClassPath, other: IType) = {
      other match {
        case Simple(otherTyp) if otherTyp.getSort == Type.OBJECT =>
          classPath.classImplementsOrExtends(otherTyp.getInternalName, typ.getInternalName)
        // Nulls can be assigned to this type if this type is an object
        case NullType if isObject =>
          true
        // TODO: more of this w/ primitives and what not
        case _ =>
          false
      }
    }

    def isInterface(classPath: ClassPath): Boolean = isObject && classPath.isInterface(typ.getInternalName)

    def isObject: Boolean = typ.getSort == Type.OBJECT
    def isArray: Boolean = typ.getSort == Type.ARRAY

    override def pretty: String = typ.toString

    override def asArray(dimensions: Int = 1): IType = {
      val baseType = if (isArray) typ.getElementType else typ
      Simple(Type.getType(("[" * dimensions) + baseType.getDescriptor))
    }

    override def elementType: IType = {
      require(isArray, "Not array")
      Simple(typ.getElementType)
    }
  }

  case object NullType extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
    override def pretty: String = "null"
    override def asArray(dimensions: Int = 1): IType = sys.error("Cannot make array of null type")
    override def elementType: IType = sys.error("No element type of null type")
  }

  case object Undefined extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
    override def pretty: String = "<undefined>"
    override def asArray(dimensions: Int = 1): IType = sys.error("Cannot make array of undefined type")
    override def elementType: IType = sys.error("No element type of undefined type")
  }

  case class UndefinedLabelInitialized(label: Label) extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
    override def pretty: String = s"<undefined on $label>"
    override def asArray(dimensions: Int = 1): IType = sys.error("Cannot make array of undefined label type")
    override def elementType: IType = sys.error("No element type of undefined label type")
  }
}