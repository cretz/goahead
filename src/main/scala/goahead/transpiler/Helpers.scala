package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree.{AbstractInsnNode, ClassNode, FieldNode, MethodNode}

object Helpers {

  val StringType = Type.getType(classOf[String])
  val NilExpr = Node.Identifier("nil")

  // Rule for string names is that you get closest to the letter you need
  // *after* extended latin A

  def goStaticObjectName(internalName: String): String = goObjectNamePrefix(internalName) + "__Static"

  def goInstanceObjectName(internalName: String) = goObjectNamePrefix(internalName) + "__Instance"

  def goObjectNamePrefix(internalName: String): String = {
    require(!internalName.headOption.contains('['), "Not ready yet for arrays")
    val pieces = internalName.split('/')
    goPackagePrefix(pieces.dropRight(1)) + goClassSuffix(pieces.last)
  }

  def goStaticVarName(internalName: String): String = goObjectNamePrefix(internalName) + "__Var"

  def goStaticAccessorName(internalName: String): String = goObjectNamePrefix(internalName)

  def goPackagePrefix(pieces: Array[String]): String =
    if (pieces.isEmpty) "__ROOT" else pieces.mkString("__").capitalize

  def goClassSuffix(simpleName: String): String = s"__$simpleName"

  def goFieldName(owner: String, name: String): String = name.capitalize

  def goMethodName(name: String, desc: String): String =
    goMethodPrefix(name) + "__desc__" + goMethodSuffix(desc)

  def goMethodName(name: String, desc: Type): String =
    goMethodName(name, desc.getDescriptor)

  def goMethodNameFromTypes(name: String, returnType: Type, argumentTypes: Type*): String =
    goMethodName(name, Type.getMethodDescriptor(returnType, argumentTypes:_*))

  def goMethodNameFromClasses(name: String, returnType: Class[_], argumentTypes: Class[_]*): String =
    goMethodName(name, Type.getMethodDescriptor(Type.getType(returnType), argumentTypes.map(Type.getType):_*))

  def goMethodPrefix(name: String): String = name match {
    case "<init>" => "Instance_Init"
    case "<clinit>" => "Static_Init"
    case _ => name.capitalize
  }

  def goMethodSuffix(desc: String): String =
    Type.getArgumentTypes(desc).map(typeToNameString).mkString("__")

  def typeToNameString(typ: Type): String = typ.getSort match {
    case Type.BOOLEAN => "Z"
    case Type.CHAR => "C"
    case Type.SHORT => "S"
    case Type.INT => "I"
    case Type.LONG => "J"
    case Type.FLOAT => "F"
    case Type.DOUBLE => "D"
    case Type.ARRAY => ("__arr__" * typ.getDimensions) + typeToNameString(typ.getElementType)
    case Type.OBJECT => "__obj__" + goObjectNamePrefix(typ.getInternalName)
    case _ => sys.error(s"Unrecognized type: $typ")
  }

  implicit class RichString(val str: String) extends AnyVal {
    def toIdent: Node.Identifier = Node.Identifier(str)

    def goUnescaped: String =
     // TODO
     str

    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.String, '"' + str.goUnescaped + '"')
  }

  implicit class RichInt(val int: Int) extends AnyVal {
    @inline
    def isAccess(access: Int) = (int & access) == access
    def isAccessInterface = isAccess(Opcodes.ACC_INTERFACE)
    def isAccessStatic = isAccess(Opcodes.ACC_STATIC)

    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, int.toString)
  }

  implicit class RichClassNode(val classNode: ClassNode) extends AnyVal {
    def fieldNodes = {
      import scala.collection.JavaConverters._
      classNode.fields.asScala.asInstanceOf[Seq[FieldNode]]
    }

    def methodNodes = {
      import scala.collection.JavaConverters._
      classNode.methods.asScala.asInstanceOf[Seq[MethodNode]]
    }
  }

  implicit class RichMethodNode(val methodNode: MethodNode) extends AnyVal {
    def instructionIter = {
      import scala.collection.JavaConverters._
      methodNode.instructions.iterator.asScala.asInstanceOf[Iterator[AbstractInsnNode]]
    }
  }
}
