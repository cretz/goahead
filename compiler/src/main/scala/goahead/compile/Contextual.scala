package goahead.compile

trait Contextual[T <: Contextual[T]] {
  def conf: Config
  def cls: Cls
  def imports: Imports
  def mangler: Mangler

  def updatedImports(imports: Imports): T

  @inline def classPath = imports.classPath
}
