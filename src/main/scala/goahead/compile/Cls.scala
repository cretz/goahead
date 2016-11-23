package goahead.compile

import org.objectweb.asm.tree.{ClassNode, FieldNode, MethodNode}

sealed trait Cls {
  def name: String
  // With slashes
  def packageName: String
  def access: Int
  def fields: Seq[Field]
  def methods: Seq[Method]
  def interfaces: Seq[String]
  def parent: Option[String]
}

object Cls {

  def apply(node: ClassNode): Cls = Asm(node)

  case class Asm(node: ClassNode) extends Cls {
    @inline
    override def name = node.name

    override def packageName = name.lastIndexOf('/') match {
      case -1 => ""
      case index => name.substring(0, index)
    }

    @inline
    override def access = node.access

    override lazy val fields = {
      import scala.collection.JavaConverters._
      node.fields.asScala.asInstanceOf[Seq[FieldNode]].map(Field(node.name, _))
    }

    override lazy val methods = {
      import scala.collection.JavaConverters._
      node.methods.asScala.asInstanceOf[Seq[MethodNode]].map(Method.apply)
    }

    override lazy val interfaces = {
      import scala.collection.JavaConverters._
      node.interfaces.asScala.asInstanceOf[Seq[String]]
    }

    override lazy val parent = Option(node.superName)
  }
}
