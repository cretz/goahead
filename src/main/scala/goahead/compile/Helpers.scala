package goahead.compile

import goahead.ast.Node
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree._

object Helpers {

  import AstDsl._

  val ObjectType = Type.getType(classOf[Object])
  val StringType = Type.getType(classOf[String])
  val NilExpr = "nil".toIdent

  def importQualifiedName(
    imports: Imports,
    internalClassName: String,
    ident: String
  ): (Imports, Node.Expression) = {
    imports.classPath.findClassDir(internalClassName) match {
      case None =>
        sys.error(s"Class not found: $internalClassName")
      case Some("") =>
        imports -> ident.toIdent
      case Some(dir) =>
        val (newImports, alias) = imports.withImportAlias(dir)
        newImports -> alias.dot(ident)
    }
  }

  def newString(imports: Imports, v: String): (Imports, Node.CallExpression) = {
    val (newImports, alias) = imports.withImportAlias("rt")
    newImports -> alias.toIdent.call(v.toLit.singleSeq)
  }

  def typeToGoType(imports: Imports, typ: Type)(implicit m: Mangler): (Imports, Node.Expression) = {
    typ.getSort match {
      case Type.BOOLEAN => imports -> "bool".toIdent
      case Type.CHAR => imports -> "rune".toIdent
      case Type.SHORT => imports -> "int16".toIdent
      case Type.INT => imports -> "int".toIdent
      case Type.LONG => imports -> "int64".toIdent
      case Type.FLOAT => imports -> "float32".toIdent
      case Type.DOUBLE => imports -> "float64".toIdent
      case Type.ARRAY =>
        @inline
        def arrayTypeFromGoType(typ: (Imports, Node.Expression)) = typ._1 -> Node.ArrayType(typ._2)
        1.until(typ.getDimensions).foldLeft(arrayTypeFromGoType(typeToGoType(imports, typ.getElementType))) {
          case ((localImports, arrayType), _) => arrayTypeFromGoType(localImports, arrayType)
        }
      case Type.OBJECT if imports.classPath.isInterface(typ.getInternalName) =>
        importQualifiedName(imports, typ.getInternalName, m.instanceObjectName(typ.getInternalName))
      case Type.OBJECT =>
        val (newImports, expr) =
          importQualifiedName(imports, typ.getInternalName, m.instanceObjectName(typ.getInternalName))
        newImports -> expr.star
      case sort => sys.error(s"Unrecognized type $sort")
    }
  }

  def staticInstRefExpr(
    imports: Imports,
    internalName: String
  )(implicit m: Mangler): (Imports, Node.CallExpression) = {
    val (newImports, expr) = importQualifiedName(imports, internalName, m.staticAccessorName(internalName))
    newImports -> expr.call()
  }

  def staticNewExpr(
    imports: Imports,
    internalName: String
  )(implicit m: Mangler): (Imports, Node.CallExpression) = {
    val (newImports, staticRef) = staticInstRefExpr(imports, internalName)
    newImports -> staticRef.call("New".toIdent.singleSeq)
  }

  implicit class RichClassNode(val classNode: ClassNode) extends AnyVal {
    def fieldNodes = {
      import scala.collection.JavaConverters._
      classNode.fields.asScala.asInstanceOf[Seq[FieldNode]]
    }

    def methodNodes = {
      import scala.collection.JavaConverters._
      classNode.methods.asScala.asInstanceOf[Seq[MethodNode]]
    }
  }

  implicit class RichInt(val int: Int) extends AnyVal {
    @inline
    def isAccess(access: Int) = (int & access) == access
    def isAccessSuper = isAccess(Opcodes.ACC_SUPER)
    def isAccessInterface = isAccess(Opcodes.ACC_INTERFACE)
    def isAccessStatic = isAccess(Opcodes.ACC_STATIC)

    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, int.toString)
    def toTypedLit = TypedExpression(toLit, Type.INT_TYPE, cheapRef = true)
  }

  implicit class RichMethodContext(val ctx: MethodCompiler.Context) extends AnyVal {
    def stackPop() = {
      val (newStack, item) = ctx.stack.pop()
      ctx.copy(stack = newStack) -> item
    }

    def stackPop(amount: Int) = {
      val (newStack, items) = ctx.stack.pop(amount)
      ctx.copy(stack = newStack) -> items
    }

    def stackPushed(item: TypedExpression) = ctx.copy(stack = ctx.stack.push(item))

    def stackPopped[T](f: (MethodCompiler.Context, TypedExpression) => T) = {
      f.tupled(stackPop())
    }

    def stackPopped[T](amount: Int, f: (MethodCompiler.Context, Seq[TypedExpression]) => T) = {
      f.tupled(stackPop(amount))
    }

    def getLocalVar(index: Int, desc: String) = {
      // TODO: check if we need to check for 0 being "this"
      ctx.localVars.get(index) match {
        case Some(expr) => ctx -> v
        case None =>
          val expr = TypedExpression(s"var$index".toIdent, Type.getType(desc), cheapRef = true)
          ctx.copy(localVars = ctx.localVars + (index -> expr)) -> expr
      }
    }

    def withLocalVar[T](index: Int, desc: String, f: (MethodCompiler.Context, TypedExpression) => T) = {
      f.tupled(getLocalVar(index, desc))
    }

    def getTempVar(typ: Type) = {
      val (newStack, tempVar) = ctx.stack.tempVar(typ)
      ctx.copy(stack = newStack) -> tempVar
    }

    def withTempVar[T](typ: Type, f: (MethodCompiler.Context, Stack.TempVar) => T) = {
      f.tupled(getTempVar(typ))
    }
  }

  implicit class RichMethodNode(val methodNode: MethodNode) extends AnyVal {
    def instructionIter = {
      import scala.collection.JavaConverters._
      methodNode.instructions.iterator.asScala.asInstanceOf[Iterator[AbstractInsnNode]]
    }

    def debugLocalVariableNodes = {
      import scala.collection.JavaConverters._
      methodNode.localVariables.asScala.asInstanceOf[Seq[LocalVariableNode]]
    }

    def tryCatchBlockNodes = {
      import scala.collection.JavaConverters._
      methodNode.tryCatchBlocks.asScala.asInstanceOf[Seq[TryCatchBlockNode]]
    }
  }

  implicit class RichAsmNode(val node: AbstractInsnNode) extends AnyVal {
    def byOpcode[T](f: PartialFunction[Int, T]) =
      f.applyOrElse(node.getOpcode, sys.error(s"Unrecognized opcode: ${node.getOpcode}"))
  }

  implicit class RichType(val typ: Type) extends AnyVal {
    def zeroExpr = 0.toTypedLit.toExprNode(typ)
  }

  implicit class RichTypedExpression(val expr: TypedExpression) extends AnyVal {

    def asmType = expr.typ match {
      case TypedExpression.Type.Simple(typ) => typ
      case oldTyp => sys.error(s"Unable to handle type $oldTyp")
    }

    def toExprNode(typ: Type) = expr.expr.toType(asmType, typ)
  }
}
