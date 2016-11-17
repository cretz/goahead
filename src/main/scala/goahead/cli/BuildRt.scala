package goahead.cli

import goahead.ast.Node
import goahead.ast.Node.File
import goahead.compile.ClassPath

trait BuildRt extends Command {
  override val name = "build-rt"

  case class Conf(outDir: String)

  override def argParser = implicit builder => Conf(
    outDir = builder.opt(
      name = "out-dir",
      aliases = Seq("o"),
      default = ".",
      desc = "The output directory to compile all classes into. The trailing dir name will be the package name."
    ).get
  )

  override def confLoader = _ => pureconfig.loadConfig[Conf]

  override def run(conf: Conf) = {
    val buildStubs = new BuildRt.BuildStubsFileTransform(transformFile)
    buildStubs.run(buildStubs.Conf(
      classPath = Seq(ClassPath.Entry.javaRuntimeJarPath.toAbsolutePath.toString),
      excludeRunningRuntimeJar = true,
      outDir = conf.outDir,
      onlyMethodsReferencingClasses = true,
      prependToFile = Some(
        """// Generated from Azul Zulu packaged OpenJDK JAR and carries the same
          |// GPL license with the classpath exception""".stripMargin
      ),
      classes = Seq(
        "java.io.PrintStream",
        "java.lang.Exception",
        "java.lang.NullPointerException",
        "java.lang.String",
        "java.lang.StringBuilder",
        "java.lang.System"
      ),
      // Things we are handling ourselves for now
      excludePatterns = Seq(
        "java/io/PrintStream.println(I)V",
        "java/io/PrintStream.println(Ljava/lang/String;)V",
        "java/io/PrintStream.println(Z)V",
        "java/lang/Object.<init>()V",
        "java/lang/StringBuilder.<init>()V",
        "java/lang/StringBuilder.append(I)Ljava/lang/StringBuilder;",
        "java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "java/lang/StringBuilder.toString()Ljava/lang/String;",
        "java/lang/System.<clinit>()V"
      )
    ))
  }

  protected def transformFile(conf: BuildStubs#Conf, d: Node.File): Node.File = {
    import goahead.compile.AstDsl._
    // Let's add a string var inside the string struct
    var f = addField(
      d,
      conf.manglerInst.instanceObjectName("java/lang/String"),
      field("Underlying", "string".toIdent)
    )
    // Also add it to the string builder struct
    f = addField(
      f,
      conf.manglerInst.instanceObjectName("java/lang/StringBuilder"),
      field("Underlying", "string".toIdent)
    )
    f
  }


  protected def addField(f: Node.File, structName: String, fld: Node.Field): Node.File = {
    import Node._, goahead.compile.AstDsl._
    f.copy(
      declarations = f.declarations.map {
        case d @ GenericDeclaration(Token.Type, Seq(TypeSpecification(Identifier(id), StructType(fields))))
          if id == structName => struct(structName, fields :+ fld)
        case other => other
      }
    )
  }
}

object BuildRt extends BuildRt {
  class BuildStubsFileTransform(transform: (BuildStubs#Conf, Node.File) => Node.File) extends BuildStubs {
    override protected def nodeToCode(conf: Conf, file: File): String =
      super.nodeToCode(conf, transform(conf, file))
  }
}