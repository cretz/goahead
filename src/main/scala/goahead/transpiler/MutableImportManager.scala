package goahead.transpiler

import goahead.ast.Node

import scala.annotation.tailrec

class MutableImportManager {
  @volatile
  private var imports = Map.empty[String, String]

  def loadImportAlias(dir: String): String = synchronized {
    // Get the alias and keep adding 1 to it until it's not in the map
    val alias = dir.substring(dir.lastIndexOf('/') + 1)
    @tailrec
    def aliasForDir(attempt: Int = 0): String = {
      val aliasTry = if (attempt == 0) alias else s"$alias$attempt"
      imports.get(aliasTry) match {
        case Some(v) if v == dir => aliasTry
        case None => imports += aliasTry -> dir; aliasTry
        case Some(_) => aliasForDir(attempt + 1)
      }
    }
    aliasForDir()
  }

  def buildDeclaration(): Node.Declaration = {
    import Helpers._
    Node.GenericDeclaration(
      Node.Token.Import,
      imports.toSeq.map { case (alias, mport) =>
        Node.ImportSpecification(
          name = if (alias == mport || mport.endsWith(s"/$alias")) None else Some(alias.toIdent),
          path = mport.toLit
        )
      }
    )
  }
}
