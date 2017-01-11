package goahead.compile

import goahead.ast.Node
import goahead.cli.FileTransformer

class RtFileTransformer extends FileTransformer {

  override def apply(conf: Config, classes: Seq[Cls], classPath: ClassPath, mangler: Mangler, f: Node.File) = {
    import goahead.compile.AstDsl._
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
      )
    )

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
