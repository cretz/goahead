package goahead.compile

import java.io.{PrintWriter, StringWriter}

import goahead.ast.Node
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{Textifier, TraceMethodVisitor}
import org.objectweb.asm.{Opcodes, Type}

object Helpers {

  import AstDsl._

  val ObjectType = Type.getType(classOf[Object])
  val StringType = Type.getType(classOf[String])
  val NilExpr = "nil".toIdent

  implicit class RichContextual[T <: Contextual[T]](val ctx: T) extends AnyVal {

    def withImportAlias(dir: String): (T, String) = {
      val (newImports, alias) = ctx.imports.withImportAlias(dir)
      ctx.updatedImports(newImports) -> alias
    }

    def importQualifiedName(
      internalClassName: String,
      ident: String
    ): (T, Node.Expression) = {
      ctx.imports.classPath.findClassDir(internalClassName) match {
        case None =>
          sys.error(s"Class not found: $internalClassName")
        case Some("") =>
          ctx -> ident.toIdent
        case Some(dir) =>
          val (ctx, alias) = withImportAlias(dir)
          ctx -> alias.dot(ident)
      }
    }

    def newString(v: String): (T, Node.CallExpression) = {
      withImportAlias("rt").leftMap { case (ctx, alias) =>
        ctx -> alias.toIdent.sel("NewString").call(v.toLit.singleSeq)
      }
    }

    def typeToGoType(typ: Type): (T, Node.Expression) = {
      typ.getSort match {
        case Type.BOOLEAN => ctx -> "bool".toIdent
        case Type.CHAR => ctx -> "rune".toIdent
        case Type.SHORT => ctx -> "int16".toIdent
        case Type.INT => ctx -> "int".toIdent
        case Type.LONG => ctx -> "int64".toIdent
        case Type.FLOAT => ctx -> "float32".toIdent
        case Type.DOUBLE => ctx -> "float64".toIdent
        case Type.ARRAY =>
          @inline
          def arrayTypeFromGoType(typ: (T, Node.Expression)) = typ._1 -> Node.ArrayType(typ._2)
          1.until(typ.getDimensions).foldLeft(arrayTypeFromGoType(typeToGoType(typ.getElementType))) {
            case (v, _) => arrayTypeFromGoType(v)
          }
        case Type.OBJECT if ctx.imports.classPath.isInterface(typ.getInternalName) =>
          importQualifiedName(typ.getInternalName, ctx.mangler.instanceObjectName(typ.getInternalName))
        case Type.OBJECT =>
          importQualifiedName(typ.getInternalName, ctx.mangler.instanceObjectName(typ.getInternalName)).leftMap {
            case (ctx, expr) => ctx -> expr.star
          }
        case sort => sys.error(s"Unrecognized type $sort")
      }
    }

    def staticInstRefExpr(internalName: String): (T, Node.CallExpression) = {
      importQualifiedName(internalName, ctx.mangler.staticAccessorName(internalName)).leftMap { case (ctx, expr) =>
        ctx -> expr.call()
      }
    }

    def staticNewExpr(internalName: String): (T, Node.CallExpression) = {
      staticInstRefExpr(internalName).leftMap { case (ctx, staticRef) =>
        ctx -> staticRef.sel("New").call()
      }
    }

    def staticInstTypeExpr(internalName: String): (T, Node.Expression) = {
      importQualifiedName(internalName, ctx.mangler.staticObjectName(internalName))
    }

    def instTypeExpr(internalName: String): (T, Node.Expression) = {
      importQualifiedName(internalName, ctx.mangler.instanceObjectName(internalName))
    }
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

    def hasStaticInit = methodNodes.exists(_.name == "<clinit>")
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
        case Some(expr) => ctx -> expr
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

  implicit class RichAsmNode(val node: AbstractInsnNode) extends AnyVal {
    def byOpcode[T](f: PartialFunction[Int, T]) =
      f.applyOrElse(node.getOpcode, (o: Int) => sys.error(s"Unrecognized opcode: $o"))

    def pretty: String = {
      val printer = new Textifier()
      val writer = new StringWriter()
      node.accept(new TraceMethodVisitor(printer))
      printer.print(new PrintWriter(writer))
      writer.toString.trim
    }
  }

  implicit class RichTuple[A, B](val tuple: (A, B)) extends AnyVal {
    @inline
    def leftMap[C](f: (A, B) => C): C = f.tupled(tuple)
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
