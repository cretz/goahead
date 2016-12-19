package goahead.compile

import org.objectweb.asm.Type

trait Mangler {
  def fieldName(owner: String, name: String): String
  def fieldGetterName(owner: String, name: String): String
  def fieldSetterName(owner: String, name: String): String
  def implObjectName(internalName: String): String
  def implMethodName(name: String, desc: String): String
  def dispatchInterfaceName(internalName: String): String
  def instanceInterfaceName(internalName: String): String
  def forwardMethodName(name: String, desc: String): String
  def interfaceDefaultMethodName(owner: String, name: String, desc: String): String
  def dispatchInitMethodName(classInternalName: String): String
  def staticAccessorName(internalName: String): String
  def staticObjectName(internalName: String): String
  def staticVarName(internalName: String): String
  // NOTE: without extension
  def fileName(packageOrClassNameWithSlashesOrDots: String): String
}

object Mangler {

  object Simple extends Simple
  trait Simple extends Mangler {

    // TODO: I added "private[this]" to get rid of all unused later

    private[this] def classSuffix(simpleName: String): String = "__" + simpleName.replace("$", "__innerclass__")

    override def dispatchInterfaceName(internalName: String): String =
      objectNamePrefix(internalName) + "__Dispatch"

    override def instanceInterfaceName(internalName: String): String =
      objectNamePrefix(internalName) + "__Instance"

    override def forwardMethodName(name: String, desc: String): String =
      methodName(name, desc)

    override def interfaceDefaultMethodName(owner: String, name: String, desc: String): String =
      objectNamePrefix(owner) + "__defaultmethod__" + methodName(name, desc)

    override def dispatchInitMethodName(classInternalName: String): String =
      objectNamePrefix(classInternalName) + "__InitDispatch"

    override def fieldName(owner: String, name: String) = name.capitalize

    override def fieldGetterName(owner: String, name: String) =
      "FieldGet__" + objectNamePrefix(owner) + "__" + fieldName(owner, name)

    override def fieldSetterName(owner: String, name: String) =
      "FieldSet__" + objectNamePrefix(owner) + "__" + fieldName(owner, name)

    override def implObjectName(internalName: String) = objectNamePrefix(internalName) + "__Impl"

    override def implMethodName(name: String, desc: String): String =
      "Impl__" + methodName(name, desc)

    private[this] def methodName(name: String, desc: String): String =
      methodPrefix(name) + "__desc__" + methodSuffix(desc)

    private[this] def methodName(name: String, desc: Type): String =
      methodName(name, desc.getDescriptor)

    private[this] def methodNameFromClasses(name: String, returnType: Class[_], argumentTypes: Class[_]*): String =
      methodName(name, Type.getMethodDescriptor(Type.getType(returnType), argumentTypes.map(Type.getType): _*))

    private[this] def methodNameFromTypes(name: String, returnType: Type, argumentTypes: Type*): String =
      methodName(name, Type.getMethodDescriptor(returnType, argumentTypes: _*))

    private[this] def methodPrefix(name: String): String = name match {
      case "<init>" => "Instance_Init"
      case "<clinit>" => "Static_Init"
      case _ => name.capitalize
    }

    private[this] def methodSuffix(desc: String): String = {
      // Due to synthetic bridge methods, we have to include the return type here too...
      val argTypes = Type.getArgumentTypes(desc).map(typeToNameString).mkString("__")
      val retType = typeToNameString(Type.getReturnType(desc))
      argTypes + "__ret__" + retType
    }

    private[this] def objectNamePrefix(internalName: String): String = {
      require(!internalName.headOption.contains('['), "Not ready yet for arrays")
      val pieces = internalName.replace("$", "__innerclass__").split('/')
      packagePrefix(pieces.dropRight(1)) + classSuffix(pieces.last)
    }

    private[this] def packagePrefix(pieces: Array[String]): String =
      if (pieces.isEmpty) "__ROOT" else pieces.mkString("__").capitalize

    override def staticAccessorName(internalName: String): String = objectNamePrefix(internalName)

    override def staticObjectName(internalName: String): String = objectNamePrefix(internalName) + "__Static"

    override def staticVarName(internalName: String): String = objectNamePrefix(internalName) + "__Var"

    private[this] def typeToNameString(typ: Type): String = typ.getSort match {
      case Type.BOOLEAN => "Z"
      case Type.BYTE => "B"
      case Type.CHAR => "C"
      case Type.SHORT => "S"
      case Type.INT => "I"
      case Type.LONG => "J"
      case Type.FLOAT => "F"
      case Type.DOUBLE => "D"
      case Type.VOID => "V"
      case Type.ARRAY => ("__arr__" * typ.getDimensions) + typeToNameString(typ.getElementType)
      case Type.OBJECT => "__obj__" + objectNamePrefix(typ.getInternalName)
      case _ => sys.error(s"Unrecognized type: $typ")
    }

    private[this] def varName(name: String): String = name

    override def fileName(packageOrClassNameWithSlashesOrDots: String): String =
      packageOrClassNameWithSlashesOrDots.replace('.', '_').replace('/', '_')
  }
}
