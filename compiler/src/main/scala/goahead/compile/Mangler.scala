package goahead.compile

import java.nio.ByteBuffer
import java.util.Base64

import org.objectweb.asm.Type

trait Mangler {
  def fieldName(field: Field): String = {
    import Helpers._
    fieldName(
      field.cls.name,
      field.name,
      field.privateTo,
      isPriv = field.access.isAccessPrivate,
      static = field.access.isAccessStatic
    )
  }
  def fieldName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean, static: Boolean): String
  def fieldGetterName(field: Field): String = {
    import Helpers._
    fieldGetterName(field.cls.name, field.name, field.privateTo, isPriv = field.access.isAccessPrivate)
  }
  def fieldGetterName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean): String
  def fieldSetterName(field: Field): String = {
    import Helpers._
    fieldSetterName(field.cls.name, field.name, field.privateTo, isPriv = field.access.isAccessPrivate)
  }
  def fieldSetterName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean): String
  def implObjectName(internalName: String): String
  def implMethodName(method: Method): String = {
    import Helpers._
    implMethodName(method.name, method.desc, method.privateTo, isPriv = method.access.isAccessPrivate)
  }
  def implMethodName(name: String, desc: String, privateTo: Option[String], isPriv: Boolean): String
  def dispatchInterfaceName(internalName: String): String
  def instanceInterfaceName(internalName: String): String
  def instanceRawPointerMethodName(internalName: String): String
  def forwardMethodName(method: Method): String = {
    import Helpers._
    forwardMethodName(method.name, method.desc, method.privateTo, isPriv = method.access.isAccessPrivate)
  }
  def forwardMethodName(name: String, desc: String, privateTo: Option[String], isPriv: Boolean): String
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

  /**
    * @deprecated See https://github.com/golang/go/issues/18602
    */
  @Deprecated
  object Simple extends Simple
  trait Simple extends Mangler {

    private[this] def classSuffix(simpleName: String): String = "__" + simpleName.replace("$", "__innerclass__")

    override def dispatchInterfaceName(internalName: String): String =
      objectNamePrefix(internalName) + "__Dispatch"

    override def instanceInterfaceName(internalName: String): String =
      objectNamePrefix(internalName) + "__Instance"

    override def instanceRawPointerMethodName(internalName: String): String =
      "RawPtr__" + objectNamePrefix(internalName)

    override def forwardMethodName(name: String, desc: String, privateTo: Option[String], isPriv: Boolean): String =
      methodName(name, desc, privateTo)

    override def interfaceDefaultMethodName(owner: String, name: String, desc: String): String =
      objectNamePrefix(owner) + "__defaultmethod__" + methodName(name, desc, None)

    override def dispatchInitMethodName(classInternalName: String): String =
      objectNamePrefix(classInternalName) + "__InitDispatch"

    override def fieldName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean, static: Boolean) =
      name.capitalize

    override def fieldGetterName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean) =
      "FieldGet__" + objectNamePrefix(owner) + "__" + fieldName(owner, name, privateTo, isPriv, static = false)

    override def fieldSetterName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean) =
      "FieldSet__" + objectNamePrefix(owner) + "__" + fieldName(owner, name, privateTo, isPriv, static = false)

    override def implObjectName(internalName: String) = objectNamePrefix(internalName) + "__Impl"

    override def implMethodName(name: String, desc: String, privateTo: Option[String], isPriv: Boolean): String =
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

  class Compact(val packagePrivateUnexported: Boolean) extends Mangler {
    import AstDsl._

    // Essentially what we do here is use the short version of names followed
    // by base 64'd string hashes and pray there are no collisions, heh
    val encoder = Base64.getEncoder.withoutPadding()
    val implSuffix = "_Í"
    val dispatchSuffix = "_Ð"
    val instanceSuffix = "_Ñ"
    val defaultMethodSuffix = "_Ď"
    val staticObjectSuffix = "_Ś"
    val staticVarSuffix = "_Ʋ"
    val invokeDynamicSyncVarSuffix = "_Ú"
    val invokeDynamicCallSiteVarSuffix = "_Ş"
    val dynamicProxySuffix = "_Ƥ"

    private[this] def stringHash(str: String): String = {
      // Base 64 has '+' and '/' as non-ident chars, so we have to replace them
      encoder.encodeToString(ByteBuffer.allocate(4).putInt(str.hashCode).array()).
        replace('+', 'Þ').replace('/', 'Ø')
    }

    private[this] def fixNameExport(name: String, privateTo: Option[String], isPriv: Boolean): String = {
      // If it's private or package private and we unexport those, the uncapitalize, otherwise capitalize
      if (isPriv || (privateTo.isDefined && packagePrivateUnexported)) name.head.toLower + name.tail
      else name.capitalize
    }

    private[this] def fieldName(owner: String, name: String, privateTo: Option[String]) = {
      // Name has to be prefixed with c_ if it was capitalized to prevent clash
      val uniqueName = if (name.head.isUpper) s"c_$name" else name
      // If it's not private to anything, we need no hash
      val hash = stringHash(s"${owner}_${name}_$privateTo")
      s"${uniqueName}_$hash"
    }

    override def fieldName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean, static: Boolean) = {
      // We consider non-static private here too since there are accessors for it
      fixNameExport(fieldName(owner, name, privateTo), privateTo, isPriv || !static).goSafeIdent
    }

    override def fieldGetterName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean) = {
      fixNameExport("Get" + fieldName(owner, name, privateTo).capitalize, privateTo, isPriv).goSafeIdent
    }

    override def fieldSetterName(owner: String, name: String, privateTo: Option[String], isPriv: Boolean) = {
      fixNameExport("Set" + fieldName(owner, name, privateTo).capitalize, privateTo, isPriv).goSafeIdent
    }

    private[this] def objectName(internalName: String): String = {
      require(!internalName.headOption.contains('['), "Arrays not supported")
      val simpleName = internalName.substring(internalName.lastIndexOf('/') + 1)
      simpleName + "_" + stringHash(internalName)
    }

    override def implObjectName(internalName: String) = {
      // TODO: fix this to not export if private
      (objectName(internalName).capitalize + implSuffix).goSafeIdent
    }

    override def dispatchInterfaceName(internalName: String) = {
      // TODO: fix this to not export if private
      (objectName(internalName).capitalize + dispatchSuffix).goSafeIdent
    }

    override def instanceInterfaceName(internalName: String) = {
      // TODO: fix this to not export if private
      (objectName(internalName).capitalize + instanceSuffix).goSafeIdent
    }

    private[this] def methodName(
      name: String,
      desc: String,
      privateTo: Option[String],
      isPriv: Boolean,
      ownerSpecific: Option[String] = None
    ) = {
      // Name has to be prefixed with c_ if it was capitalized to prevent clash
      val uniqueName = name match {
        case "<init>" => "init"
        case "<clinit>" => "clinit"
        case n if n.head.isUpper => s"c_$name"
        case _ => name
      }
      val strToHash = s"$name${desc}_${privateTo.getOrElse("pub")}_${ownerSpecific.getOrElse("noowner")}"
      fixNameExport(uniqueName + "_" + stringHash(strToHash), privateTo, isPriv)
    }

    override def implMethodName(name: String, desc: String, privateTo: Option[String], isPriv: Boolean) = {
      (methodName(name, desc, privateTo, isPriv) + implSuffix).goSafeIdent
    }

    override def instanceRawPointerMethodName(internalName: String) = {
      ("Raw_" + stringHash(internalName)).goSafeIdent
    }

    override def forwardMethodName(name: String, desc: String, privateTo: Option[String], isPriv: Boolean) = {
      methodName(name, desc, privateTo, isPriv).goSafeIdent
    }

    override def interfaceDefaultMethodName(owner: String, name: String, desc: String) = {
      (methodName(name, desc, None, isPriv = false, Some(owner)) + defaultMethodSuffix).goSafeIdent
    }

    override def dispatchInitMethodName(internalName: String) = {
      (dispatchInterfaceName(internalName) + "_Init").goSafeIdent
    }

    override def staticAccessorName(internalName: String) = {
      objectName(internalName).goSafeIdent
    }

    override def staticObjectName(internalName: String) = {
      (objectName(internalName) + staticObjectSuffix).goSafeIdent
    }

    override def staticVarName(internalName: String) = {
      (objectName(internalName) + staticVarSuffix).goSafeIdent
    }

    override def invokeDynamicSyncVarName(owner: String, name: String, desc: String, insnIndex: Int) = {
      s"_${insnIndex}_${methodName(name, desc, None, isPriv = false, Some(owner))}$invokeDynamicSyncVarSuffix".
        goSafeIdent
    }

    override def invokeDynamicCallSiteVarName(owner: String, name: String, desc: String, insnIndex: Int) = {
      s"_${insnIndex}_${methodName(name, desc, None, isPriv = false, Some(owner))}$invokeDynamicCallSiteVarSuffix".
        goSafeIdent
    }

    override def funcInterfaceProxySuffix(internalName: String) = dynamicProxySuffix

    override def funcInterfaceProxyCreateMethodName(internalName: String) = ("Create" + dynamicProxySuffix).goSafeIdent

    override def implSelfMethodName() = ("Self" + implSuffix).goSafeIdent

    override def forwardSelfMethodName() = "Self".goSafeIdent

    override def fileName(packageOrClassNameWithSlashesOrDots: String) = {
      packageOrClassNameWithSlashesOrDots.replace('.', '_').replace('/', '_')
    }
  }
}
