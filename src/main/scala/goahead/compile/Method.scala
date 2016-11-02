package goahead.compile

import org.objectweb.asm.tree.{AbstractInsnNode, MethodNode}

case class Method(
  access: Int,
  name: String,
  desc: String,
  instructions: Seq[AbstractInsnNode]
)

object Method {
  def apply(node: MethodNode): Method = Method(
    access = node.access,
    name = node.name,
    desc = node.desc,
    instructions = {
      import scala.collection.JavaConverters._
      node.instructions.iterator.asScala.asInstanceOf[Iterator[AbstractInsnNode]].toSeq
    }
  )
}