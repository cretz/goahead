package goahead.compile

import org.objectweb.asm.tree.FieldNode

sealed trait Field {
  def cls: Cls
  def name: String
  def desc: String
  def access: Int

  def privateTo = {
    import Helpers._
    if (access.isAccessPrivate) Some(cls.name)
    else if (access.isAccessPackagePrivate) Some(cls.packageName)
    else None
  }
}

object Field {

  def apply(cls: Cls, node: FieldNode): Field = Asm(cls, node)

  case class Asm(override val cls: Cls, node: FieldNode) extends Field {
    @inline
    override def name = node.name

    @inline
    override def desc = node.desc

    @inline
    override def access = node.access

    override def toString = s"${cls.name}::${node.name}"
  }
}
