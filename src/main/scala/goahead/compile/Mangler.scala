package goahead.compile

import org.objectweb.asm.Type

trait Mangler {
  def fieldName(owner: String, name: String): String
  def fieldGetterName(owner: String, name: String): String
  def fieldSetterName(owner: String, name: String): String
  def implObjectName(internalName: String): String
  def implMethodName(method: Method): String =
    implMethodName(method.name, method.desc, method.privateTo)
  def implMethodName(name: String, desc: String, privateTo: Option[String]): String
  def dispatchInterfaceName(internalName: String): String
  def instanceInterfaceName(internalName: String): String
  def instanceRawPointerMethodName(internalName: String): String
  def forwardMethodName(method: Method): String =
    forwardMethodName(method.name, method.desc, method.privateTo)
  def forwardMethodName(name: String, desc: String, privateTo: Option[String]): String
  def interfaceDefaultMethodName(owner: String, name: String, desc: String): String
  def dispatchInitMethodName(classInternalName: String): String
  def staticAccessorName(internalName: String): String
  def staticObjectName(internalName: String): String
  def staticVarName(internalName: String): String
  def invokeDynamicSyncVarName(owner: String, methodName: String, methodDesc: String, insnIndex: Int): String
  def invokeDynamicCallSiteVarName(owner: String, methodName: String, methodDesc: String, insnIndex: Int): String
  def funcInterfaceProxySuffix(internalName: String): String
  def funcInterfaceProxyCreateMethodName(internalName: String): String
  def implSelfMethodName(): String
  def forwardSelfMethodName(): String
  // NOTE: without extension
  def fileName(packageOrClassNameWithSlashesOrDots: String): String
}

object Mangler {

  object Simple extends Simple
  trait Simple extends Mangler {

    private[this] def classSuffix(simpleName: String): String = "__" + simpleName.replace("$", "__innerclass__")

    override def dispatchInterfaceName(internalName: String): String =
      objectNamePrefix(internalName) + "__Dispatch"

    override def instanceInterfaceName(internalName: String): String =
      objectNamePrefix(internalName) + "__Instance"

    override def instanceRawPointerMethodName(internalName: String): String =
      "RawPtr__" + objectNamePrefix(internalName)

    override def forwardMethodName(name: String, desc: String, privateTo: Option[String]): String =
      methodName(name, desc, privateTo)

    override def interfaceDefaultMethodName(owner: String, name: String, desc: String): String =
      objectNamePrefix(owner) + "__defaultmethod__" + methodName(name, desc, None)

    override def dispatchInitMethodName(classInternalName: String): String =
      objectNamePrefix(classInternalName) + "__InitDispatch"

    override def fieldName(owner: String, name: String) = name.capitalize

    override def fieldGetterName(owner: String, name: String) =
      "FieldGet__" + objectNamePrefix(owner) + "__" + fieldName(owner, name)

    override def fieldSetterName(owner: String, name: String) =
      "FieldSet__" + objectNamePrefix(owner) + "__" + fieldName(owner, name)

    override def implObjectName(internalName: String) = objectNamePrefix(internalName) + "__Impl"

    override def implMethodName(name: String, desc: String, privateTo: Option[String]): String =
      "Impl__" + methodName(name, desc, privateTo)

    private[this] def methodName(name: String, desc: String, privateTo: Option[String]): String =
      methodPrefix(name, privateTo) + "__desc__" + methodSuffix(desc)

    private[this] def methodPrefix(name: String, privateTo: Option[String]): String = {
      def privPrefix = privateTo.map(v => "PrivTo__" + objectNamePrefix(v) + "__").getOrElse("")
      name match {
        case "<init>" => "Instance_Init"
        case "<clinit>" => "Static_Init"
        // We do this to disambiguate two methods of the same case-insensitive name
        case _ if name.head.isUpper => s"${privPrefix}Capitalized__${name.capitalize}"
        case _ => privPrefix + name.capitalize
      }
    }

    private[this] def methodSuffix(desc: String): String = {
      // Due to synthetic bridge methods, we have to include the return type here too...
      val argTypes = Type.getArgumentTypes(desc).map(typeToNameString).mkString("__")
      val retType = typeToNameString(Type.getReturnType(desc))
      argTypes + "__ret__" + retType
    }

    private[this] def objectNamePrefix(internalName: String): String = {
      require(!internalName.headOption.contains('['), "Not ready yet for arrays")
      val pieces = internalName.replace("$", "__innerclass__").replace("-", "__dash__").split('/')
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

    override def invokeDynamicSyncVarName(
      owner: String,
      methName: String,
      methDesc: String,
      insnIndex: Int
    ): String = "_" + objectNamePrefix(owner) + s"__invokedynsync${insnIndex}__" + methodName(methName, methDesc, None)

    override def invokeDynamicCallSiteVarName(
      owner: String,
      methName: String,
      methDesc: String,
      insnIndex: Int
    ): String = "_" + objectNamePrefix(owner) + s"__invokedynsite${insnIndex}__" + methodName(methName, methDesc, None)

    override def funcInterfaceProxySuffix(internalName: String): String =
      "__dynproxy__"

    override def funcInterfaceProxyCreateMethodName(internalName: String): String =
      "DynProxy_Create"

    override def implSelfMethodName(): String = "Impl_Self"

    override def forwardSelfMethodName(): String = "Self"

    override def fileName(packageOrClassNameWithSlashesOrDots: String): String =
      packageOrClassNameWithSlashesOrDots.replace('.', '_').replace('/', '_')
  }
}
