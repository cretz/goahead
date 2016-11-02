package goahead.compile

import scala.annotation.tailrec

case class Imports(
  classPath: ClassPath,
  aliases: Map[String, String] = Map.empty
) {

  def withImportAlias(dir: String): (Imports, String) = {
    // Get the alias and keep adding 1 to it until it's not in the map
    val alias = dir.substring(dir.lastIndexOf('/') + 1)
    @tailrec
    def aliasForDir(attempt: Int = 0): (Imports, String) = {
      val aliasTry = if (attempt == 0) alias else s"$alias$attempt"
      aliases.get(aliasTry) match {
        case Some(v) if v == dir => this -> aliasTry
        case None => copy(aliases = aliases + (aliasTry -> dir)) -> aliasTry
        case Some(_) => aliasForDir(attempt + 1)
      }
    }
    aliasForDir()
  }
}
