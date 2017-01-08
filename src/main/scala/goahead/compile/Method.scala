package goahead.compile

import java.io.{PrintWriter, StringWriter}

import goahead.PolymorphicSignature
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{Textifier, TraceMethodVisitor}

sealed trait Method {
  def cls: Cls
  def access: Int
  def name: String
  def desc: String
  def argTypes: Seq[IType]
  def returnType: IType
  def instructions: IndexedSeq[AbstractInsnNode]
  def debugLocalVars: Seq[LocalVariableNode]
  def asmString: String
  def tryCatchBlocks: Seq[TryCatchBlockNode]
  def isSignaturePolymorphic: Boolean
  def visibleAnnotations: Seq[Annotation]
  def invisibleAnnotations: Seq[Annotation]
  def isCallerSensitive: Boolean
  def isDefault: Boolean

  def privateTo = {
    import Helpers._
    if (access.isAccessPrivate) Some(cls.name)
    else if (access.isAccessPackagePrivate) Some(cls.packageName)
    else None
  }
}

object Method {

  private[Method] val GoaheadSigPolyAnnotation = Type.getType(classOf[PolymorphicSignature]).getDescriptor
  private[Method] val JvmSigPolyAnnotation = s"Ljava/lang/invoke/MethodHandle$$PolymorphicSignature;"
  private[Method] val JvmCallerSensitiveAnnotation = "Lsun/reflect/CallerSensitive;"

  def apply(cls: Cls, node: MethodNode): Method = Asm(cls, node)

  case class Asm(override val cls: Cls, node: MethodNode) extends Method {
    @inline
    override def access = node.access

    @inline
    override def name = node.name

    @inline
    override def desc = node.desc

    override lazy val argTypes = IType.getArgumentTypes(node.desc)
    override lazy val returnType = IType.getReturnType(node.desc)

    override def instructions = {
      import scala.collection.JavaConverters._
      node.instructions.iterator.asScala.asInstanceOf[Iterator[AbstractInsnNode]].toIndexedSeq
    }

    override def debugLocalVars = {
      import scala.collection.JavaConverters._
      if (node.localVariables == null || node.localVariables.isEmpty) Seq.empty
      else node.localVariables.asScala.asInstanceOf[Seq[LocalVariableNode]]
    }

    override def asmString = {
      val printer = new Textifier()
      val writer = new StringWriter()
      node.accept(new TraceMethodVisitor(printer))
      printer.print(new PrintWriter(writer))
      writer.toString.trim
    }

    override def tryCatchBlocks: Seq[TryCatchBlockNode] = {
      import scala.collection.JavaConverters._
      node.tryCatchBlocks.iterator.asScala.asInstanceOf[Iterator[TryCatchBlockNode]].toSeq
    }

    override lazy val isSignaturePolymorphic: Boolean = {
      import Helpers._
      // We also have our own version of this annotation for testing
      if (allAnnotations.exists(_.desc == GoaheadSigPolyAnnotation)) {
        true
      } else if (cls.majorVersion <= Opcodes.V1_8) {
        cls.name == "java/lang/invoke/MethodHandle" && desc == "([Ljava/lang/Object;)Ljava/lang/Object;" &&
          access.isAccessNative && access.isAccessVarargs
      } else allAnnotations.exists(_.desc == JvmSigPolyAnnotation)
    }

    lazy val allAnnotations = visibleAnnotations ++ invisibleAnnotations

    override lazy val visibleAnnotations: Seq[Annotation] = {
      import scala.collection.JavaConverters._
      Option(node.visibleAnnotations).toSeq.flatMap(_.asScala.asInstanceOf[Seq[AnnotationNode]].map(Annotation.apply))
    }

    override lazy val invisibleAnnotations: Seq[Annotation] = {
      import scala.collection.JavaConverters._
      Option(node.invisibleAnnotations).toSeq.flatMap(_.asScala.asInstanceOf[Seq[AnnotationNode]].map(Annotation.apply))
    }

    override lazy val isCallerSensitive = allAnnotations.exists(_.desc == "Lsun/reflect/CallerSensitive;")

    override def isDefault = {
      import Helpers._
      cls.access.isAccessInterface && !access.isAccessStatic && !access.isAccessAbstract
    }
  }
}