package goahead.compile.interop

import java.io.File
import java.nio.file.{Path, Paths}

class GoPath(
  paths: Seq[Path],
  root: Path = GoPath.goRoot,
  parser: GoParser = GoParser
) {

  private[this] var packageCache = Map.empty[String, Package]

  private val allPaths = root +: paths

  def loadPackage(pkgName: String): Package = this.synchronized {
    packageCache.getOrElse(pkgName, {
      val pkg = parser.loadPackage(pkgName, this)
      packageCache += pkgName -> pkg
      pkg
    })
  }

  def envString(additional: Path*) = (paths ++ additional).mkString(File.pathSeparator)
}

object GoPath {

  lazy val goRoot = Paths.get {
    sys.env.getOrElse("GOROOT", {
      if (sys.props.get("os.name").exists(_.startsWith("Windows"))) "C:\\Go" else "/usr/local/go"
    })
  }

  def apply(
    paths: Seq[String] = sys.env.get("GOPATH").toSeq,
    root: Path = GoPath.goRoot,
    parser: GoParser = GoParser
  ): GoPath = new GoPath(paths.flatMap(_.split(File.pathSeparatorChar)).map(Paths.get(_).toAbsolutePath), root, parser)
}
