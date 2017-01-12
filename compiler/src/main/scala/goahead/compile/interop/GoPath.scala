package goahead.compile.interop

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

import com.google.common.io.Resources
import com.squareup.javapoet.ClassName
import goahead.interop.{GoPackage, GoType}

class GoPath(
  paths: Seq[Path],
  root: Path = GoPath.goRoot,
  parser: GoParser = GoParser,
  javaPackages: Map[Path, GoPath.JavaPackage]
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

  def builtInPackage = getPackage("")

  def getPackage(pkgName: String): GoPath.JavaPackage = {
    // TODO: cleanup path code
    javaPackages.getOrElse(Paths.get(pkgName), sys.error(s"Unable to find package $pkgName"))
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
    parser: GoParser = GoParser,
    javaPackages: Map[Path, GoPath.JavaPackage] = javaPackagesFromClassPath()
  ): GoPath = new GoPath(
    paths = paths.flatMap(_.split(File.pathSeparatorChar)).map(Paths.get(_).toAbsolutePath),
    root = root,
    parser = parser,
    javaPackages = javaPackages
  )

  def javaPackagesFromClassPath(
    classLoader: ClassLoader = Thread.currentThread().getContextClassLoader
  ): Map[Path, GoPath.JavaPackage] = {
    import scala.collection.JavaConverters._
    val packages = classLoader.getResources("META-INF/gopackages").asScala.flatMap { url =>
      Resources.readLines(url, StandardCharsets.UTF_8).asScala
    }
    packages.map({ pkg =>
      val javaPkg = Option(java.lang.Package.getPackage(pkg)).getOrElse(sys.error(s"Unable to find package $pkg"))
      val goPkg = Option(javaPkg.getAnnotation(classOf[GoPackage])).
        getOrElse(sys.error(s"Unable to find GoPackage annotation on $pkg"))
      // TODO: cleanup path code
      Paths.get(goPkg.name()) -> JavaPackage(pkg, goPkg)
    }).toMap
  }

  case class JavaPackage(javaPkg: String, goPkg: GoPackage) {
    // Keyed by go name, value is FQCN
    private[this] lazy val typeNameMap = {
      goPkg.types().flatMap(c => Option(c.getAnnotation(classOf[GoType])).map(_.value() -> c.getSimpleName)).toMap
    }

    def javaClassName(goTypeName: String) =
      ClassName.get(javaPkg, typeNameMap.getOrElse(goTypeName, sys.error(s"Unable to find Go type $goTypeName")))
  }
}
