package goahead.compile

import java.io.{PrintWriter, StringWriter}

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.{Label, Opcodes}
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{Textifier, TraceMethodVisitor}

import scala.annotation.tailrec

object Helpers extends Logger {

  import AstDsl._

  val ClassType = IType.getType(classOf[Class[_]])
  val ObjectType = IType.getType(classOf[Object])
  val StringType = IType.getType(classOf[String])
  val NilExpr = "nil".toIdent
  val NilTypedExpr = TypedExpression(expr = NilExpr, IType.NullType, cheapRef = true)

  @inline
  def swallowException[T](f: => T): Unit = { f; () }

  implicit class RichBoolean(val boolean: Boolean) extends AnyVal {
    def toLit = if (boolean) "true".toIdent else "false".toIdent
  }

  implicit class RichContextual[T <: Contextual[T]](val ctx: T) extends AnyVal {

    @inline
    def map[U](f: T => U): U = f(ctx)

    def withImportAlias(dir: String): (T, String) = {
      val (newImports, alias) = ctx.imports.withImportAlias(dir)
      ctx.updatedImports(newImports) -> alias
    }

    def importRuntimeQualifiedName(ident: String): (T, Node.Expression) = {
      importQualifiedName("java/lang/Object", ident)
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
      importRuntimeQualifiedName("NewString").map { case (ctx, newString) =>
        ctx -> newString.call(v.toLit.singleSeq)
      }
    }

    def newTypedString(v: String): (T, TypedExpression) = {
      newString(v).map { case (ctx, newStr) => ctx -> TypedExpression(newStr, StringType, cheapRef = true) }
    }

    def typeToGoType(typ: IType): (T, Node.Expression) = {
      import org.objectweb.asm.Type
      typ match {
        case IType.Simple(asmTyp) => asmTyp.getSort match {
          case Type.VOID => ctx -> emptyStruct.star
          case Type.ARRAY => importRuntimeQualifiedName(typ.goTypeName(ctx.mangler))
          case Type.OBJECT => importQualifiedName(asmTyp.getInternalName, typ.goTypeName(ctx.mangler))
          case _ => ctx -> typ.goTypeName(ctx.mangler).toIdent
        }
        case _ => ctx -> emptyInterface
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

    def createVarDecl(vars: Seq[TypedExpression]): (T, Node.Statement) = {
      // Collect them all and then send at once to a single var decl
      val ctxAndNamedTypes = vars.foldLeft(ctx -> Seq.empty[(String, Node.Expression)]) {
        case ((ctx, prevNamedTypes), localVar) =>
          // We ignore undefined types here...
          ctx.typeToGoType(localVar.typ).map { case (ctx, typ) =>
            ctx -> (prevNamedTypes :+ (localVar.name -> typ))
          }
      }
      ctxAndNamedTypes.map { case (ctx, namedTypes) =>
        ctx -> varDecls(namedTypes: _*).toStmt
      }
    }

    def isOptimizable(insn: InvokeDynamicInsnNode): Boolean = {
      ctx.conf.optimizeRecognizedInvokeDynamic &&
        insn.bsm.getTag == Opcodes.H_INVOKESTATIC &&
        insn.bsm.getOwner == "java/lang/invoke/LambdaMetafactory" &&
        // TODO: we actually need to remove alt meta factory and do it somewhere else
        // since it can implement other interfaces and have bridges and what not
        (insn.bsm.getName == "metafactory" || insn.bsm.getName == "altMetafactory")
    }
  }

  implicit class RichCls(val cls: Cls) extends AnyVal {
    def hasStaticInit = cls.methods.exists(_.name == "<clinit>")

    def frameStack(frame: FrameNode): Seq[IType] = Option(frame.stack) match {
      case None => Nil
      case Some(stack) =>
        import scala.collection.JavaConverters._
        stack.asScala.map(IType.fromFrameVarType(cls, _))
    }

    def frameLocals(frame: FrameNode): Seq[IType] = Option(frame.local) match {
      case None => Nil
      case Some(locals) =>
        import scala.collection.JavaConverters._
        locals.asScala.map(IType.fromFrameVarType(cls, _))
    }
  }

  implicit class RichDouble(val double: Double) extends AnyVal {
    def toLit[T <: Contextual[T]](ctx: T): (T, Node.Expression) = {
      // Some special cases we have to account for
      @inline def mathCall(fn: String, params: Node.Expression*) = ctx.withImportAlias("math").map {
        case (ctx, math) => ctx -> math.toIdent.sel(fn).call(params)
      }
      double match {
        case Double.PositiveInfinity => mathCall("Inf", 1.toLit)
        case Double.NegativeInfinity => mathCall("Inf", (-1).toLit)
        case 0 if 1 / double < 0 => mathCall("Copysign", 0.toLit, (-1).toLit)
        case d if d.isNaN => mathCall("NaN")
        case d => ctx -> Node.BasicLiteral(Node.Token.Float, d.toString)
      }
    }

    def toTypedLit[T <: Contextual[T]](ctx: T): (T, TypedExpression) =
      toLit(ctx).map { case (ctx, lit) => ctx -> TypedExpression(lit, IType.DoubleType, cheapRef = true) }
  }

  implicit class RichFloat(val float: Float) extends AnyVal {
    def toLit[T <: Contextual[T]](ctx: T): (T, Node.Expression) = {
      // Some special cases we have to account for
      @inline def mathCall(fn: String, params: Node.Expression*) = ctx.withImportAlias("math").map {
        case (ctx, math) => ctx -> "float32".toIdent.call(Seq(math.toIdent.sel(fn).call(params)))
      }
      float match {
        case Float.PositiveInfinity => mathCall("Inf", 1.toLit)
        case Float.NegativeInfinity => mathCall("Inf", (-1).toLit)
        case 0 if 1 / float < 0 => mathCall("Copysign", 0.toLit, (-1).toLit)
        case f if f.isNaN => mathCall("NaN")
        case f => ctx -> Node.BasicLiteral(Node.Token.Float, f.toString)
      }
    }

    def toTypedLit[T <: Contextual[T]](ctx: T): (T, TypedExpression) =
      toLit(ctx).map { case (ctx, lit) => ctx -> TypedExpression(lit, IType.FloatType, cheapRef = true) }
  }

  implicit class RichInt(val int: Int) extends AnyVal {
    @inline
    def isAccess(access: Int) = (int & access) == access
    def isAccessAbstract = isAccess(Opcodes.ACC_ABSTRACT)
    def isAccessInterface = isAccess(Opcodes.ACC_INTERFACE)
    def isAccessNative = isAccess(Opcodes.ACC_NATIVE)
    def isAccessPrivate = isAccess(Opcodes.ACC_PRIVATE)
    def isAccessProtected = isAccess(Opcodes.ACC_PROTECTED)
    def isAccessPublic = isAccess(Opcodes.ACC_PUBLIC)
    def isAccessPackagePrivate = !isAccessPrivate && !isAccessProtected && !isAccessPublic
    def isAccessStatic = isAccess(Opcodes.ACC_STATIC)
    def isAccessSuper = isAccess(Opcodes.ACC_SUPER)
    def isAccessVarargs = isAccess(Opcodes.ACC_VARARGS)

    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, int.toString)
    def toTypedLit = TypedExpression(toLit, IType.IntType, cheapRef = true)
  }

  implicit class RichLong(val long: Long) extends AnyVal {
    def toLit: Node.BasicLiteral = Node.BasicLiteral(Node.Token.Int, long.toString)
    def toTypedLit = TypedExpression(toLit, IType.LongType, cheapRef = true)
  }

  implicit class RichMethodContext(val ctx: MethodCompiler.Context) extends AnyVal {

    def pushString(v: String) = ctx.newTypedString(v).map(_.stackPushed(_))

    def pushTypeLit(typ: IType) = typedTypeLit(typ).map(_.stackPushed(_))

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

    def nextUnusedVarName(): (MethodCompiler.Context, String) = {
      ctx.localVars.nextUnusedVarName().map { case (localVars, name) =>
        ctx.copy(localVars = localVars) -> name
      }
    }

    def nextUnusedVarNames(amount: Int): (MethodCompiler.Context, Seq[String]) = {
      (0 until amount).foldLeft(ctx -> Seq.empty[String]) { case ((ctx, names), _) =>
        ctx.nextUnusedVarName().map { case (ctx, name) => ctx -> (names :+ name) }
      }
    }

    def getTempVar(typ: IType) = {
      // Previously we tried to find one not in use, but unfortunately it is very difficult
      // to tell whether one is in use. Can't just check the top level of the stack, but would
      // have to walk all stack expressions. Instead, just create a new temp var and live with
      // the consequences which should be minimal because frames don't usually last long.
      // We are going to keep a temp var counter for naming temp vars.
      nextUnusedVarName().map { case (ctx, name) =>
        val tempVar = TypedExpression.namedVar(name, typ)
        ctx.copy(localTempVars = ctx.localTempVars :+ tempVar) -> tempVar
      }
    }

    def withTempVar[T](typ: IType, f: (MethodCompiler.Context, TypedExpression) => T) = {
      f.tupled(getTempVar(typ))
    }

    def prepareToGotoLabel(label: LabelNode): (MethodCompiler.Context, Seq[Node.Statement]) = {
      // Set a stack vars in prep for the jump
      ctx.sets.find(_.label.getLabel == label.getLabel).get.newFrame match {
        case None => ctx -> Nil
        case Some(otherFrame) =>
          ctx.cls.frameStack(otherFrame).zipWithIndex.foldLeft(ctx -> Seq.empty[Node.Statement]) {
            case ((ctx, prevStmts), (frameType, index)) =>
              ctx.stack.items.lift(index) match {
                case None => ctx -> prevStmts
                case Some(existingStackItem) => existingStackItem.toExprNode(ctx, frameType).map { case (ctx, item) =>
                  ctx -> (prevStmts :+ s"${label.getLabel}_stack$index".toIdent.assignExisting(item))
                }
              }
          }
      }
    }

    def typeLit(typ: IType): (MethodCompiler.Context, Node.Expression) = {
      ctx.staticInstRefExpr("java/lang/Class").map { case (ctx, staticInstExpr) =>
        ctx.newString(typ.className).map { case (ctx, className) =>
          ctx -> staticInstExpr.sel(
            ctx.mangler.implMethodName("forName", "(Ljava/lang/String;)Ljava/lang/Class;", None, isPriv = false)
          ).call(Seq(className))
        }
      }
    }

    def typedTypeLit(typ: IType): (MethodCompiler.Context, TypedExpression) = {
      typeLit(typ).map { case (ctx, lit) => ctx -> TypedExpression(lit, ClassType, cheapRef = true) }
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
      case IType.ByteType => "int8".toIdent.call(Seq(0.toLit))
      case IType.CharType => "rune".toIdent.call(Seq(0.toLit))
      case _ => NilExpr
    }

    def arrayNewFnCall[T <: Contextual[T]](ctx: T, size: TypedExpression): (T, Node.Expression) = {
      val (simpleType, fnName) = typ match {
        case typ: IType.Simple if typ.isArray =>
          typ -> (typ.elementType match {
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
          })
        case _ => sys.error(s"Expected array type, got: $typ")
      }
      ctx.importRuntimeQualifiedName(fnName).map { case (ctx, arrayNewFn) =>
        size.toExprNode(ctx, IType.IntType).map { case (ctx, sizeExpr) =>
          // We have to give the class name if it's an object array
          val componentNameOpt =
            if (fnName != "NewObjectArray") None
            else if (typ.elementType.asInstanceOf[IType.Simple].isArray) Some(simpleType.elementType.className.toLit)
            else Some(s"L${simpleType.elementType.className};".toLit)
          ctx -> arrayNewFn.call(sizeExpr +: componentNameOpt.toSeq)
        }
      }
    }

    def goTypeName(mangler: Mangler): String = {
      import org.objectweb.asm.Type
      require(typ.isInstanceOf[IType.Simple])
      val asmTyp = typ.asInstanceOf[IType.Simple].typ
      asmTyp.getSort match {
        case Type.VOID => sys.error("Cannot get name from void type")
        case Type.BOOLEAN => "bool"
        case Type.CHAR => "rune"
        case Type.BYTE => "int8"
        case Type.SHORT => "int16"
        case Type.INT => "int32"
        case Type.LONG => "int64"
        case Type.FLOAT => "float32"
        case Type.DOUBLE => "float64"
        case Type.ARRAY =>
          if (asmTyp.getDimensions > 1) "ObjectArray__Instance" else {
            asmTyp.getElementType.getSort match {
              case Type.BOOLEAN => "BoolArray__Instance"
              case Type.CHAR => "CharArray__Instance"
              case Type.BYTE => "ByteArray__Instance"
              case Type.SHORT => "ShortArray__Instance"
              case Type.INT => "IntArray__Instance"
              case Type.LONG => "LongArray__Instance"
              case Type.FLOAT => "FloatArray__Instance"
              case Type.DOUBLE => "DoubleArray__Instance"
              case Type.ARRAY | Type.OBJECT => "ObjectArray__Instance"
              case sort => sys.error(s"Unrecognized array type $sort")
            }
          }
        case Type.OBJECT => mangler.instanceInterfaceName(asmTyp.getInternalName)
        case sort => sys.error(s"Unrecognized type $sort")
      }
    }

    def internalName = typ match {
      case s: IType.Simple if s.isRef => s.typ.getInternalName
      case _ => sys.error("Unexpected type to get internal name from")
    }

    def isNumeric = typ match {
      case IType.IntType | IType.FloatType | IType.DoubleType | IType.LongType |
           IType.ShortType | IType.ByteType | IType.CharType => true
      case _ => false
    }
  }

  implicit class RichTypedExpression(val expr: TypedExpression) extends AnyVal {

    def isThis = expr.maybeName.contains("this")

    def toGeneralArray[T <: Contextual[T]](ctx: T): (T, Node.Expression) = {
      ctx.importRuntimeQualifiedName("Array__Instance").map { case (ctx, arrayInst) =>
        ctx -> expr.expr.typeAssert(arrayInst)
      }
    }

    def toExprNode[T <: Contextual[T]](ctx: T, newTyp: IType, noCasting: Boolean = false): (T, Node.Expression) = {
      logger.trace(s"Converting from '${expr.typ.pretty}' to '${newTyp.pretty}'")
      expr.typ -> newTyp match {
        case (oldTyp, newTyp) if oldTyp == newTyp =>
          ctx -> expr.expr
        case (o, IType.DoubleType) if o.isNumeric =>
          ctx -> "float64".toIdent.call(Seq(expr.expr))
        case (o, IType.FloatType) if o.isNumeric =>
          ctx -> "float32".toIdent.call(Seq(expr.expr))
        case (o, IType.IntType) if o.isNumeric =>
          ctx -> "int32".toIdent.call(Seq(expr.expr))
        case (IType.BooleanType, o) if o.isNumeric =>
          ctx.importRuntimeQualifiedName("BoolToInt").map { case (ctx, boolToInt) =>
            TypedExpression(boolToInt.call(Seq(expr.expr)), IType.IntType, cheapRef = false).toExprNode(ctx, o)
          }
        case (o, IType.LongType) if o.isNumeric =>
          ctx -> "int64".toIdent.call(Seq(expr.expr))
        case (o, IType.BooleanType) if o.isNumeric =>
          // A simple x != 0
          // TODO: should parenthesize?
          ctx -> expr.expr.neq(0.toLit)
        case (o, IType.ByteType) if o.isNumeric =>
          ctx -> "int8".toIdent.call(Seq(expr.expr))
        case (o, IType.CharType) if o.isNumeric =>
          ctx -> "rune".toIdent.call(Seq(expr.expr))
        case (o, IType.ShortType) if o.isNumeric =>
          ctx -> "int16".toIdent.call(Seq(expr.expr))
        case (oldTyp, newTyp) if oldTyp == newTyp =>
          ctx -> expr.expr
        // Actual null expressions to refs need no cast
        case (IType.NullType, newTyp: IType.Simple) if expr.expr == NilExpr && newTyp.isRef =>
          ctx -> expr.expr
        // Null type to ref just needs to do a normal null safe assert
        case (IType.NullType, newTyp: IType.Simple) if newTyp.isRef =>
          nullSafeTypeAssert(ctx, newTyp)
        case (oldTyp, newTyp: IType.Simple)
          if (newTyp.isObject || newTyp.isArray) && newTyp.isAssignableFrom(ctx.imports.classPath, oldTyp) =>
            ctx -> expr.expr
        case (oldTyp: IType.Simple, newTyp: IType.Simple) if oldTyp.isRef && newTyp.isRef =>
          nullSafeTypeAssert(ctx, newTyp)
        // Unknown source means we do a null-safe cast
        case (oldTyp, newTyp: IType.Simple) if oldTyp.isUnknown && newTyp.isRef =>
          nullSafeTypeAssert(ctx, newTyp)
        // Unknown target types just means use the old type
        case (_, newTyp) if newTyp.isUnknown =>
          ctx -> expr.expr
        // Auto-box
        case (oldTyp: IType.Simple, newTyp: IType.Simple) if oldTyp.isPrimitive && newTyp.isObject =>
          val primitiveWrapper = IType.primitiveWrappers(oldTyp)
          val method = ctx.classPath.getFirstClass(primitiveWrapper.internalName).cls.methods.find(m =>
            m.access.isAccessStatic && m.name == "valueOf" &&
              m.returnType == primitiveWrapper && m.argTypes == Seq(oldTyp)
          ).getOrElse(sys.error(s"Unable to find valueOf on primitive wrapper for $primitiveWrapper"))
          ctx.staticInstRefExpr(primitiveWrapper.internalName).map { case (ctx, staticInst) =>
            ctx -> staticInst.sel(ctx.mangler.implMethodName(method)).call(expr.expr.singleSeq)
          }
        // Auto-unbox
        case (oldTyp: IType.Simple, newTyp: IType.Simple) if oldTyp.isObject && newTyp.isPrimitive =>
          val primitiveWrapper = IType.primitiveWrappers(newTyp)
          val methodName = newTyp.typ.getClassName + "Value"
          val method = ctx.classPath.getFirstClass(primitiveWrapper.internalName).cls.methods.find(m =>
            !m.access.isAccessStatic && m.name == methodName && m.returnType == newTyp
          ).getOrElse(sys.error(s"Unable to find $methodName on primitive wrapper for $primitiveWrapper"))
          toExprNode(ctx, primitiveWrapper).map { case (ctx, boxed) =>
            ctx -> boxed.sel(ctx.mangler.forwardMethodName(method)).call()
          }
        case (oldTyp, newTyp) =>
          sys.error(s"Unable to assign types: $oldTyp -> $newTyp")
      }
    }

    protected def nullSafeTypeAssert[T <: Contextual[T]](ctx: T, newTyp: IType): (T, Node.Expression) = {
      ctx.typeToGoType(newTyp).map { case (ctx, newTyp) =>
        // We have to make a copy of the expression if it's not cheap
        val (cheapExpr, preStmts) =
          if (expr.cheapRef) expr.expr -> Nil
          else "castTemp".toIdent -> Seq("castTemp".toIdent.assignDefine(expr.expr))
        ctx -> funcType(
          params = Nil,
          result = Some(newTyp)
        ).toFuncLit(preStmts ++ Seq(
          iff(None, cheapExpr, Node.Token.Eql, NilExpr, Seq(NilExpr.ret)),
          cheapExpr.typeAssert(newTyp).ret
        )).call()
      }
    }
  }

  implicit class RichLabel(val label: Label) extends AnyVal {
    def uniqueStr = "L" + label.hashCode()
  }
}
