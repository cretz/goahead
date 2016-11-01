package goahead.compile

import org.objectweb.asm.tree.FrameNode

case class FrameSection(
  node: Option[FrameNode],
  children: Seq[LabelSection]
) {

}
