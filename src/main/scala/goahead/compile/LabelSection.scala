package goahead.compile

import goahead.ast.Node
import org.objectweb.asm.tree.FrameNode

case class LabelSection(
  lineNumber: Int,
  children: Seq[Either[LabelSection, CodeSection]]
) extends MethodSection {

  def withAppendedStatement(stmt: Node.Statement): LabelSection = ???
}
