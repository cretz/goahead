package goahead.compile

import org.objectweb.asm.Type

trait Mangler {
  def fieldName(owner: String, name: String): String
  def instanceObjectName(internalName: String): String
  def methodName(name: String, desc: String): String
  def staticAccessorName(internalName: String): String
  def staticObjectName(internalName: String): String
  def staticVarName(internalName: String): String
}

object Mangler {

  object Simple extends Simple
  trait Simple extends Mangler {

    // TODO: I added "private[this]" to get rid of all unused later

    private[this] def classSuffix(simpleName: String): String = "__" + simpleName.replace("$", "__innerclass__")

    override def fieldName(owner: String, name: String) = name.capitalize

    override def instanceObjectName(internalName: String) = objectNamePrefix(internalName) + "__Instance"

    override def methodName(name: String, desc: String): String =
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

    private[this] def methodSuffix(desc: String): String =
      Type.getArgumentTypes(desc).map(typeToNameString).mkString("__")

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
      case Type.CHAR => "C"
      case Type.SHORT => "S"
      case Type.INT => "I"
      case Type.LONG => "J"
      case Type.FLOAT => "F"
      case Type.DOUBLE => "D"
      case Type.ARRAY => ("__arr__" * typ.getDimensions) + typeToNameString(typ.getElementType)
      case Type.OBJECT => "__obj__" + objectNamePrefix(typ.getInternalName)
      case _ => sys.error(s"Unrecognized type: $typ")
    }

    private[this] def varName(name: String): String = name
  }
}