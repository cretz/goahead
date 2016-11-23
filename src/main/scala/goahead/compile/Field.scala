package goahead.compile

import org.objectweb.asm.tree.FieldNode

sealed trait Field {
  def owner: String
  def name: String
  def desc: String
  def access: Int
}

object Field {

  def apply(owner: String, node: FieldNode): Field = Asm(owner, node)

  case class Asm(owner: String, node: FieldNode) extends Field {
    @inline
    override def name = node.name

    @inline
    override def desc = node.desc

    @inline
    override def access = node.access
  }
}
