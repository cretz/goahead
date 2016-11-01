package goahead.transpiler

import goahead.ast.Node
import Helpers._
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode

trait ClassTranspiler {
  import ClassTranspiler._

  def transpile(transpileCtx: Transpiler.Context, classNode: ClassNode): Seq[Node.Declaration] = {
    transpile(Context(transpileCtx, classNode))
  }

  def transpile(ctx: Context): Seq[Node.Declaration] = {
    // There's a static side and a non-static side to each class
    static(ctx) ++ instance(ctx)
  }

  def instance(ctx: Context): Seq[Node.Declaration] = {

    val instanceStruct = Node.GenericDeclaration(
      token = Node.Token.Type,
      specifications = Seq(
        Node.TypeSpecification(
          name = ctx.instanceObjectName.toIdent,
          typ = Node.StructType(instanceFields(ctx))
        )
      )
    )

    Seq(instanceStruct) ++ instanceMethods(ctx)
  }

  def instanceFields(ctx: Context): Seq[Node.Field] = {
    val fields = ctx.classNode.fieldNodes.collect {
      case node if !node.access.isAccessStatic =>
        Node.Field(
          names = Seq(goFieldName(ctx.classNode.name, node.name).toIdent),
          typ = ctx.typeToGoType(Type.getType(node.desc))
        )
    }
    // If there is a super class, it is embedded
    if (!ctx.classNode.access.isAccessSuper) fields
    else {
      Node.Field(
        names = Nil,
        typ = ctx.typeToGoType(Type.getObjectType(ctx.classNode.superName))
      ) +: fields
    }
  }

  def instanceMethods(ctx: Context): Seq[Node.FunctionDeclaration] = {
    ctx.classNode.methodNodes.collect {
      case node if !node.access.isAccessStatic =>
        ctx.transpileCtx.methodTranspiler.transpile(ctx, node)
    }
  }

  def static(ctx: Context): Seq[Node.Declaration] = {

    val staticConstructStmts = staticConstructStatements(ctx)

    val staticStruct = Node.GenericDeclaration(
      token = Node.Token.Type,
      specifications = Seq(Node.TypeSpecification(
        name = ctx.staticObjectName.toIdent,
        typ = Node.StructType(staticFields(ctx, includeSyncOnce = staticConstructStmts.nonEmpty))
      ))
    )

    val staticVar = Node.GenericDeclaration(
      token = Node.Token.Var,
      specifications = Seq(Node.ValueSpecification(
        names = Seq(ctx.staticVarName.toIdent),
        typ = Some(ctx.staticObjectName.toIdent),
        values = Nil
      ))
    )

    val staticAccessor = Node.FunctionDeclaration(
      receivers = Nil,
      name = goStaticAccessorName(ctx.classNode.name).toIdent,
      typ = Node.FunctionType(
        parameters = Nil,
        results = Seq(
          Node.Field(names = Nil, typ = Node.StarExpression(ctx.staticObjectName.toIdent))
        )
      ),
      body = Some(Node.BlockStatement(staticConstructStatements(ctx) :+ Node.ReturnStatement(
        Seq(Node.UnaryExpression(Node.Token.And, ctx.staticVarName.toIdent))
      )))
    )

    val staticNew = Node.FunctionDeclaration(
      receivers = Seq(ctx.staticThisField),
      name = "New".toIdent,
      typ = Node.FunctionType(Nil, Seq(Node.Field(Nil, ctx.instancePointerExpr))),
      body = Some(Node.BlockStatement(Seq(
        Node.ReturnStatement(Seq(
          Node.UnaryExpression(
            operator = Node.Token.And,
            operand = Node.CompositeLiteral(
              typ = Some(ctx.classRefExpr(ctx.classNode.name, false)),
              elements = if (!ctx.classNode.access.isAccessSuper) Nil else Seq(
                Node.KeyValueExpression(
                  key = goInstanceObjectName(ctx.classNode.superName).toIdent,
                  value = ctx.staticNewExpr(ctx.classNode.superName)
                )
              )
            )
          )
        ))
      )))
    )

    Seq(staticStruct, staticVar, staticAccessor, staticNew) ++ staticMethods(ctx)
  }

  def staticFields(ctx: Context, includeSyncOnce: Boolean): Seq[Node.Field] = {
    val fields = ctx.classNode.fieldNodes.collect {
      case node if node.access.isAccessStatic =>
        Node.Field(
          names = Seq(goFieldName(ctx.classNode.name, node.name).toIdent),
          typ = ctx.typeToGoType(Type.getType(node.desc))
        )
    }
    if (!includeSyncOnce) fields
    else {
      fields :+ Node.Field(
        names = Seq("init".toIdent),
        typ = Node.SelectorExpression(
          ctx.transpileCtx.imports.loadImportAlias("sync").toIdent,
          "Once".toIdent
        )
      )
    }
  }

  def staticConstructStatements(ctx: Context): Seq[Node.Statement] = {
    // We collect each set of statements as a block statement, then we put them all together
    // into a single sync once block
    val blocks = ctx.classNode.methodNodes.collect({
      case node if node.access.isAccessStatic && node.name == "<clinit>" =>
        ctx.transpileCtx.methodTranspiler.transpile(ctx, node).body
    }).flatten
    if (blocks.isEmpty) Nil
    else Seq(
      Node.ExpressionStatement(
        Node.CallExpression(
          function = Node.SelectorExpression(
            Node.SelectorExpression(ctx.staticVarName.toIdent, "init".toIdent),
            "Do".toIdent
          ),
          args = Seq(
            Node.FunctionLiteral(
              typ = Node.FunctionType(Seq.empty, Seq.empty),
              body = Node.BlockStatement(blocks)
            )
          )
        )
      )
    )
  }

  def staticMethods(ctx: Context): Seq[Node.FunctionDeclaration] = {
    ctx.classNode.methodNodes.collect {
      case node if node.access.isAccessStatic && node.name != "<clinit>" =>
        ctx.transpileCtx.methodTranspiler.transpile(ctx, node)
    }
  }
}

object ClassTranspiler extends ClassTranspiler {
  case class Context(
    transpileCtx: Transpiler.Context,
    classNode: ClassNode
  ) {
    lazy val instanceObjectName = goInstanceObjectName(classNode.name)
    lazy val instancePointerExpr = typeToGoType(Type.getObjectType(classNode.name))
    lazy val instanceThisField = Node.Field(Seq("this".toIdent), instancePointerExpr)
    lazy val staticObjectName = goStaticObjectName(classNode.name)
    lazy val staticVarName = goStaticVarName(classNode.name)
    lazy val staticPointerExpr = Node.StarExpression(classRefExpr(classNode.name, true))
    lazy val staticThisField = Node.Field(Seq("this".toIdent), staticPointerExpr)

    def importQualifiedIdent(internalClassName: String, ident: Node.Identifier): Node.Expression = {
      transpileCtx.classPath.findClassDir(internalClassName) match {
        case None =>
          sys.error(s"Class not found: $internalClassName")
        case Some("") =>
          ident
        case Some(dir) =>
          Node.SelectorExpression(
            transpileCtx.imports.loadImportAlias(dir).toIdent,
            ident
          )
      }
    }

    def classRefExpr(internalName: String, static: Boolean): Node.Expression = {
      importQualifiedIdent(
        internalClassName = internalName,
        ident = (if (static) goStaticObjectName(internalName) else goInstanceObjectName(internalName)).toIdent
      )
    }

    def typeToGoType(desc: String): Node.Expression = typeToGoType(Type.getType(desc))

    def typeToGoType(typ: Type): Node.Expression = {
      typ.getSort match {
        case Type.BOOLEAN => "bool".toIdent
        case Type.CHAR => "rune".toIdent
        case Type.SHORT => "int16".toIdent
        case Type.INT => "int".toIdent
        case Type.LONG => "int64".toIdent
        case Type.FLOAT => "float32".toIdent
        case Type.DOUBLE => "float64".toIdent
        case Type.ARRAY =>
          1.until(typ.getDimensions).foldLeft(Node.ArrayType(typeToGoType(typ.getElementType))) {
            case (arrayType, _) => Node.ArrayType(arrayType)
          }
        case Type.OBJECT if transpileCtx.classPath.isInterface(typ.getInternalName) =>
          classRefExpr(typ.getInternalName, false)
        case Type.OBJECT =>
          Node.StarExpression(classRefExpr(typ.getInternalName, false))
        case sort => sys.error(s"Unrecognized type $sort")
      }
    }

    def methodToFunctionType(returnType: Type, params: Array[Type]): Node.FunctionType = {
      Node.FunctionType(
        parameters = params.zipWithIndex.map { case (typ, index) =>
          Node.Field(names = Seq(s"arg$index".toIdent), typ = typeToGoType(typ))
        },
        results =
          if (returnType == Type.VOID_TYPE) Seq.empty
          else Seq(Node.Field(names = Seq.empty, typ = typeToGoType(returnType)))
      )
    }

    def staticClassRefExpr(internalName: String): Node.CallExpression = {
      Node.CallExpression(importQualifiedIdent(internalName, goStaticAccessorName(internalName).toIdent))
    }

    def staticNewExpr(internalName: String): Node.CallExpression = {
      Node.CallExpression(Node.SelectorExpression(staticClassRefExpr(internalName), "New".toIdent))
    }
  }
}
