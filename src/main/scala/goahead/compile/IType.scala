package goahead.compile

import goahead.Logger
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{Label, Opcodes, Type}

sealed trait IType {
  def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType

  def isAssignableFrom(classPath: ClassPath, other: IType): Boolean
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
  val IntType = IType(Type.INT_TYPE)
  val FloatType = IType(Type.FLOAT_TYPE)
  val DoubleType = IType(Type.DOUBLE_TYPE)
  val LongType = IType(Type.LONG_TYPE)

  def fromFrameVarType(thisNode: ClassNode, typ: Any) = typ match {
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
      logger.info(s"CHECKING IF $this is assignable from $other")
      other match {
        case Simple(otherTyp) if otherTyp.getSort == Type.OBJECT =>
          classPath.classHasSuper(otherTyp.getInternalName, typ.getInternalName)
        // TODO: more of this
        case _ =>
          false
      }
    }

    def isPointer: Boolean = typ.getSort == Type.OBJECT
  }

  case object NullType extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
  }

  case object Undefined extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
  }

  case class UndefinedLabelInitialized(label: Label) extends IType {
    override def maybeMakeMoreSpecific(classPath: ClassPath, other: IType): IType = other
    override def isAssignableFrom(classPath: ClassPath, other: IType) = other == this
  }
}