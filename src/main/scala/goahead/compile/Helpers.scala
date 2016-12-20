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

  implicit class RichBoolean(val boolean: Boolean) extends AnyVal {
    def toLit = if (boolean) "true".toIdent else "false".toIdent
  }

  implicit class RichContextual[T <: Contextual[T]](val ctx: T) extends AnyVal {

    @inline
    def map[U](f: T => U): U = f(ctx)

    def withRuntimeImportAlias: (T, String) = {
      val (newImports, alias) = ctx.imports.withRuntimeImportAlias
      ctx.updatedImports(newImports) -> alias
    }

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
      withRuntimeImportAlias.map { case (ctx, alias) =>
        ctx -> alias.toIdent.sel("NewString").call(v.toLit.singleSeq)
      }
    }

    def typeToGoType(typ: IType): (T, Node.Expression) = {
      typ match {
        case IType.Simple(typ) => asmTypeToGoType(typ)
        case IType.NullType | IType.Undefined | _: IType.UndefinedLabelInitialized => ctx -> emptyInterface
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
        case Type.ARRAY => withRuntimeImportAlias.map { case (ctx, rtAlias) =>
          // If it's multidimensional, it's an object regardless
          @inline def rtQualified(v: String): Node.Expression =
            if (rtAlias.isEmpty) v.toIdent else rtAlias.toIdent.sel(v)
          if (typ.getDimensions > 1) ctx -> rtQualified("ObjectArray__Instance") else {
            typ.getElementType.getSort match {
              case Type.BOOLEAN => ctx -> rtQualified("BoolArray__Instance")
              case Type.CHAR => ctx -> rtQualified("CharArray__Instance")
              case Type.BYTE => ctx -> rtQualified("ByteArray__Instance")
              case Type.SHORT => ctx -> rtQualified("ShortArray__Instance")
              case Type.INT => ctx -> rtQualified("IntArray__Instance")
              case Type.LONG => ctx -> rtQualified("LongArray__Instance")
              case Type.FLOAT => ctx -> rtQualified("FloatArray__Instance")
              case Type.DOUBLE => ctx -> rtQualified("DoubleArray__Instance")
              case Type.ARRAY | Type.OBJECT => ctx -> rtQualified("ObjectArray__Instance")
              case sort => sys.error(s"Unrecognized array type $sort")
            }
          }
        }
        case Type.OBJECT =>
          importQualifiedName(typ.getInternalName, ctx.mangler.instanceInterfaceName(typ.getInternalName))
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
          importQualifiedName(internalName, ctx.mangler.staticAccessorName(internalName)).map { case (ctx, expr) =>
            ctx -> expr.call()
          }
      }
    }

    def staticNewExpr(internalName: String): (T, Node.CallExpression) = {
      staticInstRefExpr(internalName).map { case (ctx, staticRef) =>
        ctx -> staticRef.sel("New").call()
      }
    }

    def staticInstTypeExpr(internalName: String): (T, Node.Expression) = {
      importQualifiedName(internalName, ctx.mangler.staticObjectName(internalName)).map { case (ctx, typ) =>
        ctx -> typ.star
      }
    }

    def implTypeExpr(internalName: String): (T, Node.Expression) = {
      importQualifiedName(internalName, ctx.mangler.implObjectName(internalName)).map { case (ctx, typ) =>
        ctx -> typ.star
      }
    }

    def instToImpl(inst: TypedExpression, implOf: String): (T, Node.Expression) = {
      inst.toExprNode(ctx, IType.getObjectType(implOf)).map { case (ctx, expr) =>
        ctx.instToImpl(expr, implOf)
      }
    }

    def instToImpl(inst: Node.Expression, implOf: String): (T, Node.Expression) = {
      // Just call the raw pointer func
      ctx -> inst.sel(ctx.mangler.instanceRawPointerMethodName(implOf)).call()
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

    def createVarDecl(vars: Seq[TypedExpression]): (T, Node.Statement) = {
      // Collect them all and then send at once to a single var decl
      val ctxAndNamedTypes = vars.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
        case ((ctx, prevNamedTypes), localVar) =>
          // We ignore undefined types here...
          localVar.typ match {
            case IType.Undefined =>
              ctx -> prevNamedTypes
            case _ =>
              ctx.typeToGoType(localVar.typ).map { case (ctx, typ) =>
                ctx -> (prevNamedTypes :+ (localVar.name -> typ))
              }
          }
      }
      ctxAndNamedTypes.map { case (ctx, namedTypes) =>
        ctx -> varDecls(namedTypes: _*).toStmt
      }
    }
  }

  implicit class RichCls(val cls: Cls) extends AnyVal {
    def hasStaticInit = cls.methods.exists(_.name == "<clinit>")
  }

  implicit class RichDouble(val double: Double) extends AnyVal {
    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Float, double.toString)
  }

  implicit class RichFloat(val float: Float) extends AnyVal {
    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Float, float.toString)
  }

  implicit class RichInt(val int: Int) extends AnyVal {
    @inline
    def isAccess(access: Int) = (int & access) == access
    def isAccessAbstract = isAccess(Opcodes.ACC_ABSTRACT)
    def isAccessInterface = isAccess(Opcodes.ACC_INTERFACE)
    def isAccessNative = isAccess(Opcodes.ACC_NATIVE)
    def isAccessPrivate = isAccess(Opcodes.ACC_PRIVATE)
    def isAccessStatic = isAccess(Opcodes.ACC_STATIC)
    def isAccessSuper = isAccess(Opcodes.ACC_SUPER)

    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, int.toString)
    def toTypedLit = TypedExpression(toLit, IType.IntType, cheapRef = true)
  }

  implicit class RichLong(val long: Long) extends AnyVal {
    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, long.toString)
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

    def getLocalVar(index: Int, typ: IType, forWriting: Boolean): (MethodCompiler.Context, TypedExpression) = {
      ctx.localVars.getLocalVar(ctx, index, typ, forWriting).map { case (localVars, localVar) =>
        ctx.copy(localVars = localVars) -> localVar
      }
    }

    def appendLocalVar(typ: IType): (MethodCompiler.Context, TypedExpression) = {
      ctx.localVars.appendLocalVar(ctx, typ).map { case (localVars, localVar) =>
        ctx.copy(localVars = localVars) -> localVar
      }
    }

    def dropLocalVars(amount: Int): MethodCompiler.Context = {
      ctx.copy(localVars = ctx.localVars.dropRight(amount))
    }

    def takeLocalVars(amount: Int): MethodCompiler.Context = {
      ctx.copy(localVars = ctx.localVars.take(amount))
    }

    def getTempVar(typ: IType) = {
      // Previously we tried to find one not in use, but unfortunately it is very difficult
      // to tell whether one is in use. Can't just check the top level of the stack, but would
      // have to walk all stack expressions. Instead, just create a new temp var and live with
      // the consequences which should be minimal because frames don't usually last long.
      // We just check for existing names to know the next we are able to create.
      val existingVars = ctx.functionVars ++ ctx.localTempVars
      val name = Iterator.from(0).map("temp" + _).find(name => !existingVars.exists(_.name == name)).get
      val tempVar = TypedExpression.namedVar(name, typ)
      ctx.copy(localTempVars = ctx.localTempVars :+ tempVar) -> tempVar
    }

    def withTempVar[T](typ: IType, f: (MethodCompiler.Context, TypedExpression) => T) = {
      f.tupled(getTempVar(typ))
    }

    def prepareToGotoLabel(label: LabelNode): (MethodCompiler.Context, Seq[Node.Statement]) = {
      // Set a stack vars in prep for the jump
      ctx.sets.find(_.label.getLabel == label.getLabel).get.newFrame match {
        case None => ctx -> Nil
        case Some(otherFrame) =>
          ctx -> ctx.frameStack(otherFrame).zipWithIndex.flatMap {
            case (frameType, index) =>
              // TODO: do we need to convert to anything here?
              ctx.stack.items.lift(index).map { existingStackItem =>
                s"${label.getLabel}_stack$index".toIdent.assignExisting(existingStackItem.expr)
              }
          }
      }
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
    def map[C](f: (A, B) => C): C = f.tupled(tuple)
  }

  implicit class RichIType(val typ: IType) extends AnyVal {
    // We can safely ignore the ctx change here
    def zeroExpr = typ match {
      case IType.IntType | IType.FloatType | IType.DoubleType | IType.LongType | IType.ShortType => 0.toLit
      case IType.BooleanType => "false".toIdent
      case IType.ByteType => "byte".toIdent.call(Seq(0.toLit))
      case IType.CharType => "rune".toIdent.call(Seq(0.toLit))
      case _ => sys.error(s"Unrecognized type to get zero val for: $typ")
    }

    def arrayNewFn[T <: Contextual[T]](ctx: T) = typ match {
      case typ: IType.Simple if typ.isArray =>
        val fnName = typ.elementType match {
          case eTyp: IType.Simple if eTyp.isObject || eTyp.isArray => "NewObjectArray"
          case IType.BooleanType => "NewBoolArray"
          case IType.CharType => "NewCharArray"
          case IType.FloatType => "NewFloatArray"
          case IType.DoubleType => "NewDoubleArray"
          case IType.ByteType => "NewByteArray"
          case IType.ShortType => "NewShortArray"
          case IType.IntType => "NewIntArray"
          case IType.LongType => "NewLongArray"
          case eTyp => sys.error(s"Unrecognized element type: $eTyp")
        }
        ctx.withRuntimeImportAlias.map { case (ctx, rtAlias) =>
          ctx -> rtAlias.toIdent.sel(fnName)
        }
      case _ => sys.error(s"Expected array type, got: $typ")
    }

    def internalName = typ match {
      case s: IType.Simple if s.isRef => s.typ.getInternalName
      case _ => sys.error("Unexpected type to get internal name from")
    }
  }

  implicit class RichTypedExpression(val expr: TypedExpression) extends AnyVal {

    def isThis = expr.maybeName.contains("this")

    def toGeneralArray[T <: Contextual[T]](ctx: T): (T, Node.Expression) = {
      ctx.withRuntimeImportAlias.map { case (ctx, rtAlias) =>
        ctx -> expr.expr.typeAssert(rtAlias.toIdent.sel("Array__Instance"))
      }
    }

    def toExprNode[T <: Contextual[T]](ctx: T, newTyp: IType, noCasting: Boolean = false): (T, Node.Expression) = {
      logger.trace(s"Converting from '${expr.typ.pretty}' to '${newTyp.pretty}'")
      expr.typ -> newTyp match {
        case (oldTyp, newTyp) if oldTyp == newTyp =>
          ctx -> expr.expr
        case (oldTyp: IType.Simple, IType.DoubleType) if oldTyp.isPrimitive =>
          ctx -> "float64".toIdent.call(Seq(expr.expr))
        case (oldTyp: IType.Simple, IType.FloatType) if oldTyp.isPrimitive =>
          ctx -> "float32".toIdent.call(Seq(expr.expr))
        case (oldTyp: IType.Simple, IType.IntType) if oldTyp.isPrimitive =>
          ctx -> "int".toIdent.call(Seq(expr.expr))
        case (oldTyp: IType.Simple, IType.LongType) if oldTyp.isPrimitive =>
          ctx -> "int64".toIdent.call(Seq(expr.expr))
        case (IType.IntType, IType.BooleanType) =>
          // A simple x != 0
          // TODO: should parenthesize?
          ctx -> expr.expr.neq(0.toLit)
        case (IType.IntType, IType.ByteType) =>
          ctx -> "byte".toIdent.call(Seq(expr.expr))
        case (IType.IntType, IType.CharType) =>
          ctx -> "rune".toIdent.call(Seq(expr.expr))
        case (IType.IntType, IType.ShortType) =>
          ctx -> "int16".toIdent.call(Seq(expr.expr))
        case (oldTyp, newTyp) if oldTyp == newTyp =>
          ctx -> expr.expr
        // Null type to object can be set simply
        case (IType.NullType, newTyp: IType.Simple) if newTyp.isObject =>
          ctx -> expr.expr
        // Null type to slice requires type cast
        case (IType.NullType, newTyp: IType.Simple) if newTyp.isArray =>
          ctx.typeToGoType(newTyp).map { case (ctx, newTyp) =>
            ctx -> newTyp.call(Seq(NilExpr))
          }
        case (oldTyp, newTyp: IType.Simple)
          if (newTyp.isObject || newTyp.isArray) && newTyp.isAssignableFrom(ctx.imports.classPath, oldTyp) =>
            ctx -> expr.expr
        // TODO: support primitives
        case (oldTyp: IType.Simple, newTyp: IType.Simple)
          // Needs to be cheap ref since we check for nil
          if (oldTyp.isObject || oldTyp.isArray) && (newTyp.isObject || newTyp.isArray) =>
            // Type assertion which sadly means anon function to be inline to handle possible nil
            ctx.typeToGoType(newTyp).map { case (ctx, newTyp) =>
              ctx -> funcType(
                params = Nil,
                result = Some(newTyp)
              ).toFuncLit(Seq(
                iff(None, expr.expr, Node.Token.Eql, NilExpr, Seq(NilExpr.ret)),
                expr.expr.typeAssert(newTyp).ret
              )).call()
            }
        case (oldTyp, newTyp) =>
          sys.error(s"Unable to assign types: $oldTyp -> $newTyp")
      }
    }
  }
}
