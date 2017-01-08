package goahead.compile

import goahead.Logger
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.{Opcodes, Type}

sealed trait IType {
  def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType

  // "this" = "other" (but not necessarily the inverse)
  def isAssignableFrom(classPath: ClassPath, other: IType): Boolean

  def pretty: String

  // If already an array, just adds another dimension
  def asArray: IType

  // If multidimensional, just takes off a dimension
  def elementType: IType

  def isUnknown: Boolean

  // Matches what comes out of Class.getName
  def className: String
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

  val primitiveWrappers = Map(
    VoidType -> IType.getObjectType("java/lang/Void"),
    BooleanType -> IType.getObjectType("java/lang/Boolean"),
    ByteType -> IType.getObjectType("java/lang/Byte"),
    CharType -> IType.getObjectType("java/lang/Character"),
    IntType -> IType.getObjectType("java/lang/Integer"),
    FloatType -> IType.getObjectType("java/lang/Float"),
    DoubleType -> IType.getObjectType("java/lang/Double"),
    LongType -> IType.getObjectType("java/lang/Long"),
    ShortType -> IType.getObjectType("java/lang/Short")
  )

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
    case l: LabelNode => UndefinedLabelInitialized(l)
    case v => sys.error(s"Unrecognized frame var type $v")
  }

  case class Simple(typ: Type) extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = {
      if (isAssignableFrom(classPath, other)) {
        logger.trace(s"Making type $pretty to more specific version ${other.pretty}")
        other
      } else this
    }

    override def isAssignableFrom(classPath: ClassPath, other: IType) = {
      other match {
        // Arrays can be assigned to objects
        case other: Simple if other.isArray && isObject && typ.getInternalName == "java/lang/Object" =>
          true
        case other: Simple if isObject && other.isObject =>
          classPath.classImplementsOrExtends(other.typ.getInternalName, typ.getInternalName)
        // Arrays are covariant
        case other: Simple if isArray && other.isArray && typ.getDimensions == other.typ.getDimensions =>
          elementType.isAssignableFrom(classPath, other.elementType)
        // Nulls can be assigned to this type if this type is an object
        case NullType if isObject =>
          true
        // TODO: more of this w/ primitives and what not
        case _ =>
          false
      }
    }

    def isInterface(classPath: ClassPath): Boolean = isObject && classPath.isInterface(typ.getInternalName)

    def isRef: Boolean = isObject || isArray
    def isObject: Boolean = typ.getSort == Type.OBJECT
    def isArray: Boolean = typ.getSort == Type.ARRAY
    def isMethod: Boolean = typ.getSort == Type.METHOD
    def isPrimitive: Boolean = !isObject && !isArray && !isMethod

    override def pretty: String = typ.toString

    override def asArray: IType = {
      Simple(Type.getType('[' + typ.getDescriptor))
    }

    override def elementType: IType = {
      require(isArray, "Not array")
      Simple(Type.getType(("[" * (typ.getDimensions - 1)) + typ.getElementType.getDescriptor))
    }

    override def isUnknown = false

    override def className = {
      if (!isArray) typ.getClassName else ("[" * typ.getDimensions) + (typ.getElementType.getSort match {
        case Type.OBJECT => typ.getElementType.getClassName + ";"
        case _ => typ.getElementType.getDescriptor
      })
    }

    // Actual non-array element type regardless of dimension count
    def arrayElementType = IType(typ.getElementType)
  }

  case object NullType extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
    override def pretty: String = "null"
    override def asArray: IType = sys.error("Cannot make array of null type")
    override def elementType: IType = sys.error("No element type of null type")
    override def isUnknown = true
    override def className = sys.error("No class name for null type")
  }

  case object Undefined extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
    override def pretty: String = "<undefined>"
    override def asArray: IType = sys.error("Cannot make array of undefined type")
    override def elementType: IType = sys.error("No element type of undefined type")
    override def isUnknown = true
    override def className = sys.error("No class name for undefined type")
  }

  case class UndefinedLabelInitialized(label: LabelNode) extends IType {
    import Helpers._
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
    override def pretty: String = s"<undefined on ${label.getLabel.uniqueStr}>"
    override def asArray: IType = sys.error("Cannot make array of undefined label type")
    override def elementType: IType = sys.error("No element type of undefined label type")
    override def isUnknown = true
    override def className = sys.error("No class name for undefined label type")
  }
}