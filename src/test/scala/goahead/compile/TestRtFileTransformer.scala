package goahead.compile

import goahead.ast.Node
import goahead.cli.FileTransformer

class TestRtFileTransformer extends FileTransformer {

  override def apply(conf: Config, classes: Seq[Cls], classPath: ClassPath, mangler: Mangler, f: Node.File) = {
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
        mangler.implObjectName("java/lang/String"),
        field("Underlying", "string".toIdent)
      ),
      // Also add it to the string builder struct
      addField(
        _: Node.File,
        mangler.implObjectName("java/lang/StringBuilder"),
        field("Underlying", "string".toIdent)
      ),
      // Add message to throwable
      addField(
        _: Node.File,
        mangler.implObjectName("java/lang/Throwable"),
        field("Message", mangler.instanceInterfaceName("java/lang/String").toIdent)
      )
    ) ++ primitiveWrappers.toSeq.map { case (wrapperClass, goType) =>
      // Add primitives to their wrappers
      addField(
        _: Node.File,
        mangler.implObjectName(wrapperClass),
        field("Value", goType.toIdent)
      )
    }

    // Run the transformers
    transformers.foldLeft(f) { case (file, transformer) => transformer(file) }
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
