package goahead.compile

import java.io.{PrintWriter, StringWriter}

import org.objectweb.asm.tree.{AbstractInsnNode, LocalVariableNode, MethodNode, TryCatchBlockNode}
import org.objectweb.asm.util.{Textifier, TraceMethodVisitor}

sealed trait Method {
  def access: Int
  def name: String
  def desc: String
  def instructions: Seq[AbstractInsnNode]
  def debugLocalVars: Seq[LocalVariableNode]

  def asmString: String

  def tryCatchBlocks: Seq[TryCatchBlockNode]
}

object Method {
  def apply(node: MethodNode): Method = new Simple(node)

  class Simple(val node: MethodNode) extends Method {
    override def access = node.access
    override def name = node.name
    override def desc = node.desc
    override def instructions = {
      import scala.collection.JavaConverters._
      node.instructions.iterator.asScala.asInstanceOf[Iterator[AbstractInsnNode]].toSeq
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
  }
}