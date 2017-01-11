package goahead.compile

import org.objectweb.asm.tree.AnnotationNode

sealed trait Annotation {
  def desc: String
}

object Annotation {
  def apply(node: AnnotationNode): Asm = Asm(node)

  case class Asm(node: AnnotationNode) extends Annotation {
    @inline override def desc = node.desc
  }
}