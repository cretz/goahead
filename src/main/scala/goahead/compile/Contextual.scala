package goahead.compile

import org.objectweb.asm.tree.ClassNode

trait Contextual[T <: Contextual[T]] {
  def cls: ClassNode
  def imports: Imports
  def mangler: Mangler

  def updatedImports(imports: Imports): T
}
