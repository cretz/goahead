package goahead.compile

trait Contextual[T <: Contextual[T]] {
  def cls: Cls
  def imports: Imports
  def mangler: Mangler

  def updatedImports(imports: Imports): T
}
