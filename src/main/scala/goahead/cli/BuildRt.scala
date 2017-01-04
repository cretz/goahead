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
        "java.lang.Boolean",
        "java.lang.Byte",
        "java.lang.Character",
        "java.lang.Class",
        "java.lang.ClassLoader",
        "java.lang.ClassCastException",
        "java.lang.Double",
        "java.lang.Exception",
        "java.lang.Float",
        "java.lang.IllegalArgumentException",
        "java.lang.IllegalMonitorStateException",
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.NegativeArraySizeException",
        "java.lang.NullPointerException",
        "java.lang.Short",
        "java.lang.String",
        "java.lang.StringBuilder",
        "java.lang.System",
        "java.lang.VirtualMachineError",
        "java.lang.Void",
        "java.lang.invoke.CallSite",
        "java.lang.invoke.LambdaMetafactory",
        "java.lang.invoke.MethodHandle",
        "java.lang.invoke.MethodHandles",
        "java.lang.invoke.MethodHandles$Lookup",
        "java.lang.invoke.MethodType",
        "java.lang.invoke.SerializedLambda",
        "java.util.function.BiConsumer",
        "java.util.function.BinaryOperator",
        "java.util.function.IntFunction",
        "java.util.function.Function",
        "java.util.stream.Stream"
      ),
      // Things we are handling ourselves for now
      excludePatterns = Seq(
        "java/io/PrintStream.println(C)V",
        "java/io/PrintStream.println(D)V",
        "java/io/PrintStream.println(F)V",
        "java/io/PrintStream.println(I)V",
        "java/io/PrintStream.println(J)V",
        "java/io/PrintStream.println(Ljava/lang/String;)V",
        "java/io/PrintStream.println(Z)V",
        "java/lang/ClassCastException.<init>()V",
        "java/lang/Exception.<init>(Ljava/lang/String;)V",
        "java/lang/Exception.<init>(Ljava/lang/String;Ljava/lang/Throwable;)V",
        "java/lang/IllegalMonitorStateException.<init>()V",
        "java/lang/Long.valueOf(J)Ljava/lang/Long;",
        "java/lang/Long.longValue()J",
        "java/lang/Long.<init>(J)V",
        "java/lang/NegativeArraySizeException.<init>(Ljava/lang/String;)V",
        "java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
        "java/lang/Object.<init>()V",
        "java/lang/Object.getClass()Ljava/lang/Class;",
        "java/lang/Object.hashCode()I",
        "java/lang/StringBuilder.<init>()V",
        "java/lang/StringBuilder.append(I)Ljava/lang/StringBuilder;",
        "java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
        "java/lang/StringBuilder.toString()Ljava/lang/String;",
        "java/lang/System.<clinit>()V",
        "java/lang/System.identityHashCode(Ljava/lang/Object;)I",
        "java/lang/Throwable.<init>(Ljava/lang/String;)V",
        "java/lang/Throwable.getMessage()Ljava/lang/String;",
        "java/lang/VirtualMachineError.<init>(Ljava/lang/String;)V"
      )
    ))
  }

  protected def transformFile(conf: BuildStubs#Conf, d: Node.File): Node.File = {
    import goahead.compile.AstDsl._
    val primitiveWrappers = Map(
      "java/lang/Boolean" -> "bool",
      "java/lang/Byte" -> "byte",
      "java/lang/Character" -> "rune",
      "java/lang/Float" -> "float32",
      "java/lang/Integer" -> "int",
      "java/lang/Long" -> "int64",
      "java/lang/Short" -> "int16",
      "java/lang/Double" -> "float64"
    )
    val transformers: Seq[Node.File => Node.File] = Seq(
      // Let's add a string var inside the string struct
      addField(
        _: Node.File,
        conf.manglerInst.implObjectName("java/lang/String"),
        field("Underlying", "string".toIdent)
      ),
      // Also add it to the string builder struct
      addField(
        _: Node.File,
        conf.manglerInst.implObjectName("java/lang/StringBuilder"),
        field("Underlying", "string".toIdent)
      ),
      // Add message to throwable
      addField(
        _: Node.File,
        conf.manglerInst.implObjectName("java/lang/Throwable"),
        field("Message", conf.manglerInst.instanceInterfaceName("java/lang/String").toIdent)
      )
    ) ++ primitiveWrappers.toSeq.map { case (wrapperClass, goType) =>
      // Add primitives to their wrappers
      addField(
        _: Node.File,
        conf.manglerInst.implObjectName(wrapperClass),
        field("Value", goType.toIdent)
      )
    }

    // Run the transformers
    transformers.foldLeft(d) { case (file, transformer) => transformer(file) }
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