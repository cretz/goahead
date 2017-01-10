package goahead.compile.interop

trait Mangler {
  def javaClassName(goName: String): String
  def javaMemberName(goName: String): String
}

object Mangler {
  trait Default extends Mangler {
    override def javaClassName(goName: String) = ???
    override def javaMemberName(goName: String) = ???
  }
  object Default extends Default
}
