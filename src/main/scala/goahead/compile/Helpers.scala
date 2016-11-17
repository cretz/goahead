package goahead.compile

import java.io.{PrintWriter, StringWriter}

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{Textifier, TraceMethodVisitor}

import scala.annotation.tailrec

object Helpers extends Logger {

  import AstDsl._

  val ObjectType = IType.getType(classOf[Object])
  val StringType = IType.getType(classOf[String])
  val NilExpr = "nil".toIdent

  @inline
  def swallowException[T](f: => T): Unit = { f; () }

  implicit class RichContextual[T <: Contextual[T]](val ctx: T) extends AnyVal {

    @inline
    def map[U](f: T => U): U = f(ctx)

    def withImportAlias(dir: String): (T, String) = {
      val (newImports, alias) = ctx.imports.withImportAlias(dir)
      ctx.updatedImports(newImports) -> alias
    }

    def importQualifiedName(
      internalClassName: String,
      ident: String
    ): (T, Node.Expression) = {
      ctx.imports.classPath.findClassRelativeCompiledDir(internalClassName) match {
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

    def typeToGoType(typ: IType): (T, Node.Expression) = {
      typ match {
        case IType.Simple(typ) => asmTypeToGoType(typ)
        case IType.NullType | IType.Undefined | _: IType.UndefinedLabelInitialized => ctx -> emptyInterface.star
      }
    }

    private[this] def asmTypeToGoType(typ: org.objectweb.asm.Type): (T, Node.Expression) = {
      import org.objectweb.asm.Type
      typ.getSort match {
        case Type.VOID => ctx -> emptyStruct.star
        case Type.BOOLEAN => ctx -> "bool".toIdent
        case Type.CHAR => ctx -> "rune".toIdent
        case Type.BYTE => ctx -> "byte".toIdent
        case Type.SHORT => ctx -> "int16".toIdent
        case Type.INT => ctx -> "int".toIdent
        case Type.LONG => ctx -> "int64".toIdent
        case Type.FLOAT => ctx -> "float32".toIdent
        case Type.DOUBLE => ctx -> "float64".toIdent
        case Type.ARRAY =>
          @inline
          def arrayTypeFromGoType(typ: (T, Node.Expression)) = typ._1 -> Node.ArrayType(typ._2)
          1.until(typ.getDimensions).foldLeft(arrayTypeFromGoType(asmTypeToGoType(typ.getElementType))) {
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

    def staticInstRefExpr(internalName: String): (T, Node.Expression) = {
      // If this is inside the static init of the requested class, we have to use "this" instead
      // to prevent stack overflow by re-calling init
      ctx match {
        case mCtx: MethodCompiler.Context if mCtx.cls.name == internalName && mCtx.method.name == "<clinit>" =>
          ctx -> "this".toIdent
        case _ =>
          importQualifiedName(internalName, ctx.mangler.staticAccessorName(internalName)).leftMap { case (ctx, expr) =>
            ctx -> expr.call()
          }
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

    def frameStack(frame: FrameNode): Seq[IType] = Option(frame.stack) match {
      case None => Nil
      case Some(stack) =>
        import scala.collection.JavaConverters._
        stack.asScala.map(IType.fromFrameVarType(ctx.cls, _))
    }

    def frameLocals(frame: FrameNode): Seq[IType] = Option(frame.local) match {
      case None => Nil
      case Some(locals) =>
        import scala.collection.JavaConverters._
        locals.asScala.map(IType.fromFrameVarType(ctx.cls, _))
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

    def interfaceNames = {
      import scala.collection.JavaConverters._
      classNode.interfaces.asScala.asInstanceOf[Seq[String]]
    }

    def hasStaticInit = methodNodes.exists(_.name == "<clinit>")
  }

  implicit class RichInt(val int: Int) extends AnyVal {
    @inline
    def isAccess(access: Int) = (int & access) == access
    def isAccessInterface = isAccess(Opcodes.ACC_INTERFACE)
    def isAccessNative = isAccess(Opcodes.ACC_NATIVE)
    def isAccessPrivate = isAccess(Opcodes.ACC_PRIVATE)
    def isAccessStatic = isAccess(Opcodes.ACC_STATIC)
    def isAccessSuper = isAccess(Opcodes.ACC_SUPER)

    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, int.toString)
    def toTypedLit = TypedExpression(toLit, IType.IntType, cheapRef = true)
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

    def getLocalVar(index: Int, desc: String): (MethodCompiler.Context, TypedExpression) = {
      getLocalVar(index, IType.getType(desc))
    }

    def getLocalVar(index: Int, typ: IType): (MethodCompiler.Context, TypedExpression) = {
      val static = ctx.method.access.isAccessStatic
      if (index == 0 && !static) {
        ctx -> TypedExpression.namedVar("this", IType.getObjectType(ctx.cls.name))
      } else {
        // Index 1 for non-static is actually 0 in the seq
        val seqIndex = if (static) index else index - 1
        if (ctx.localVars.size > seqIndex) {
          // TODO: try to make it more specific if we can
          ctx -> ctx.localVars(seqIndex)
        } else {
          // Fill in with uninitialized...
          Range(ctx.localVars.size, seqIndex).foldLeft(ctx)({ case (ctx, index) =>
            getLocalVar(index, IType.Undefined)._1
          }).map { ctx =>
            // Append a new one, first find the max existing index
            val varIndices = ctx.functionVars.flatMap(_.maybeName).collect {
              case name if name.startsWith("var") => name.substring(3).toInt
            }
            val maxIndex = if (varIndices.isEmpty) -1 else varIndices.max
            val localVar = TypedExpression.namedVar("var" + (maxIndex + 1), typ)
            ctx.copy(localVars = ctx.localVars :+ localVar, functionVars = ctx.functionVars :+ localVar) -> localVar
          }
        }
      }
    }

    def appendLocalVar(typ: IType): (MethodCompiler.Context, TypedExpression) = {
      if (ctx.method.access.isAccessStatic) getLocalVar(ctx.localVars.size, typ)
      else getLocalVar(ctx.localVars.size + 1, typ)
    }

    def getTempVar(typ: IType) = {
      // Try to find one not in use, otherwise create
      val possibleTempVars = ctx.functionVars ++ ctx.localTempVars
      possibleTempVars.find(t => t.typ == typ && !ctx.stack.items.contains(t)) match {
        case Some(tempVar) => ctx -> tempVar
        case None =>
          // Since temp vars can be removed after frames, just keep trying names
          val name = Iterator.from(0).map("temp" + _).find({ name =>
            !possibleTempVars.exists(_.name == name)
          }).get
          val tempVar = TypedExpression.namedVar(name, typ)
          ctx.copy(localTempVars = ctx.localTempVars :+ tempVar) -> tempVar
      }
    }

    def withTempVar[T](typ: IType, f: (MethodCompiler.Context, TypedExpression) => T) = {
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

    // Careful, this is expensive
    def index = {
      // Ref: http://stackoverflow.com/questions/25582515/make-method-actually-inline
      @inline
      @tailrec
      def nodeIndex(n: AbstractInsnNode, indexCount: Int = -1): Int =
        if (n == null) indexCount else nodeIndex(n.getPrevious, indexCount + 1)
      nodeIndex(node)
    }

    def isUnconditionalJump = node.getOpcode match {
      case Opcodes.GOTO | Opcodes.RET | Opcodes.TABLESWITCH | Opcodes.LOOKUPSWITCH |
        Opcodes.IRETURN | Opcodes.LRETURN | Opcodes.FRETURN | Opcodes.DRETURN |
        Opcodes.ARETURN | Opcodes.RETURN | Opcodes.ATHROW => true
      case _ => false
    }
  }

  implicit class RichTuple[A, B](val tuple: (A, B)) extends AnyVal {
    @inline
    def leftMap[C](f: (A, B) => C): C = f.tupled(tuple)
  }

  implicit class RichIType(val typ: IType) extends AnyVal {
    // We can safely ignore the ctx change here
    def zeroExpr = typ match {
      case IType.IntType | IType.FloatType | IType.DoubleType | IType.LongType => 0.toLit
      case IType.BooleanType => "false".toIdent
      case _ => sys.error(s"Unrecognized type to get zero val for: $typ")
    }
  }

  implicit class RichTypedExpression(val expr: TypedExpression) extends AnyVal {

    def isThis = expr.maybeName.contains("this")

    def unsafeCast[T <: Contextual[T]](ctx: T, oldTyp: IType, newTyp: IType): (T, Node.Expression) = {
      ctx.withImportAlias("unsafe").leftMap { case (ctx, unsafeAlias) =>
        val pointerArgExpr = oldTyp match {
          case IType.NullType => expr.expr
          case s: IType.Simple if s.isObject => expr.expr
          case IType.Simple(asmTyp) => expr.expr.addressOf
          case other => sys.error(s"Unrecognized existing type to convert from: $other")
        }
        ctx.typeToGoType(newTyp).leftMap { case (ctx, goType) =>
          val convertToPointer = unsafeAlias.toIdent.sel("Pointer").call(pointerArgExpr.singleSeq)
          newTyp match {
            case s: IType.Simple if s.isObject =>
              // Just parentheses
              ctx -> goType.inParens.call(convertToPointer.singleSeq)
            case _: IType.Simple =>
              // Parens with star on both sides
              ctx -> goType.star.inParens.call(convertToPointer.singleSeq).star
            case other =>
              sys.error(s"Unrecognized existing type to convert to: $other")
          }
        }
      }
    }

    def toExprNode[T <: Contextual[T]](ctx: T, newTyp: IType, noCasting: Boolean = false): (T, Node.Expression) = {
      logger.trace(s"Converting from '${expr.typ.pretty}' to '${newTyp.pretty}'")
      expr.typ -> newTyp match {
        case (IType.IntType, IType.BooleanType) => expr.expr match {
          case Node.BasicLiteral(Node.Token.Int, "1") => ctx -> "true".toIdent
          case Node.BasicLiteral(Node.Token.Int, "0") => ctx -> "false".toIdent
          case _ => sys.error(s"Unable to change int $expr to boolean")
        }
        case (oldTyp, newTyp) if oldTyp == newTyp =>
          ctx -> expr.expr
        case (oldTyp, newTyp: IType.Simple)
          if newTyp.isObject && newTyp.isAssignableFrom(ctx.imports.classPath, oldTyp) =>
            // Casting to an object unless asked not to or it's an interface
            if (noCasting) ctx -> expr.expr
            else if (newTyp.isInterface(ctx.imports.classPath)) ctx -> expr.expr
            else oldTyp match {
              case IType.NullType =>
                unsafeCast(ctx, oldTyp, newTyp)
              case old: IType.Simple if old.isObject =>
                unsafeCast(ctx, oldTyp, newTyp)
              case other =>
                sys.error(s"Unable to assign types: $oldTyp -> $newTyp")
            }
        // TODO: support primitives
        case (oldTyp, newTyp) =>
          sys.error(s"Unable to assign types: $oldTyp -> $newTyp")
      }
    }
  }
}
