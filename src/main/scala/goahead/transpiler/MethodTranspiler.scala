package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.{Label, Type}
import org.objectweb.asm.tree.{AbstractInsnNode, _}

trait MethodTranspiler extends
  FieldInsnTranspiler
  with FrameTranspiler
  with IntInsnTranspiler
  with JumpInsnTranspiler
  with LabelTranspiler
  with LdcInsnTranspiler
  with LineNumberTranspiler
  with MethodInsnTranspiler
  with TypeInsnTranspiler
  with VarInsnTranspiler
  with ZeroOpInsnTranspiler
{

  import MethodTranspiler._

  def transpile(classCtx: ClassTranspiler.Context, methodNode: MethodNode): Node.FunctionDeclaration =
    transpile(Context(classCtx, methodNode))

  def transpile(ctx: Context): Node.FunctionDeclaration = {
    ctx.pushBuilder(baseStatementBuilder)
    Node.FunctionDeclaration(
      receivers = Seq(
        if (ctx.methodNode.access.isAccessStatic) ctx.classCtx.staticThisField else ctx.classCtx.instanceThisField
      ),
      name = goMethodName(ctx.methodNode.name, ctx.methodType).toIdent,
      typ = ctx.classCtx.methodToFunctionType(ctx.methodType.getReturnType, ctx.methodType.getArgumentTypes),
      body = Some(Node.BlockStatement(transpileInsns(ctx)))
    )
  }

  def transpileInsns(ctx: Context): Seq[Node.Statement] = {
    var stmts: Seq[Node.Statement] = ctx.instructions.flatMap { insn =>
      ctx.instructionIndex += 1
      val currentBuilder = ctx.currentBuilder.get
      currentBuilder.fn(ctx, insn, currentBuilder.parent)
    }

    stmts = addTryCatchBlocks(ctx, stmts)
    stmts = removeUnusedLabelsFromStmts(ctx, stmts)
    stmts = addTempVars(ctx, stmts)
    // TODO: add local vars
    stmts
  }

  def baseStatementBuilder(
    ctx: Context,
    insn: AbstractInsnNode,
    parent: Option[StatementBuilder]
  ): Seq[Node.Statement] = {
    val stmts = insn match {
      case i: FieldInsnNode => transpile(ctx, i)
      case i: FrameNode => transpile(ctx, i)
      case i: InsnNode => transpile(ctx, i)
      case i: IntInsnNode => transpile(ctx, i)
      case i: JumpInsnNode => transpile(ctx, i)
      case i: LabelNode => transpile(ctx, i)
      case i: LdcInsnNode => transpile(ctx, i)
      case i: LineNumberNode => transpile(ctx, i)
      case i: MethodInsnNode => transpile(ctx, i)
      case i: TypeInsnNode => transpile(ctx, i)
      case i: VarInsnNode => transpile(ctx, i)
      case i => sys.error(s"Unknown instruction: $i")
    }
    // Run through the last statement handler if there is one
    ctx.peekStatementHandler match {
      case None => stmts
      case Some(handler) => handler(stmts)
    }
  }

  private[this] def addTryCatchBlocks(ctx: Context, stmts: Seq[Node.Statement]): Seq[Node.Statement] = {
    case class Scope(start: Label, end: Label)
    case class Handler(label: Label, exType: Option[String])

    val blocks = ctx.methodNode.tryCatchBlockNodes.map { node =>
      ctx.usedLabels += node.start.getLabel.toString
      ctx.usedLabels += node.end.getLabel.toString
      ctx.usedLabels += node.handler.getLabel.toString
      Scope(node.start.getLabel, node.end.getLabel) -> Handler(node.handler.getLabel, Option(node.`type`))
    }

    val groupedBlocks = blocks.foldLeft(Seq.empty[(Scope, Seq[Handler])]) { case (grouped, (scope, handler)) =>
      grouped.indexWhere(_._1 == scope) match {
        case -1 => grouped :+ (scope -> Seq(handler))
        case index =>
          val existingScope = grouped(index)
          grouped.updated(index, existingScope.copy(_2 = existingScope._2 :+ handler))
      }
    }

    // Now that we have them grouped by common blocks in order, we'll wrap them in inner functions w/ defers
    groupedBlocks.foldRight(stmts) { case ((scope, handlers), stmts) =>
      // Wrap the statements from begin-to-end with labels on the outside
      val withIndex = stmts.zipWithIndex
      val Some((startStmt, startLabelIndex)) = withIndex.collectFirst {
        case (Node.LabeledStatement(Node.Identifier(name), Some(stmt)), index) if name == scope.start.toString =>
          stmt -> index
      }
      val Some(endIndex) = withIndex.collectFirst {
        case (Node.LabeledStatement(Node.Identifier(name), _), index) if name == scope.end.toString =>
          index
      }

      // Now that we have the range, extract it to an anonymous func in the label
      val newLabel = Node.LabeledStatement(
        label = scope.start.toString.toIdent,
        statement = Some(Node.ExpressionStatement(
          Node.CallExpression(
            function = Node.FunctionLiteral(
              typ = Node.FunctionType(Nil, Nil),
              // TODO: add defer in here
              body = Node.BlockStatement(startStmt +: stmts.slice(startLabelIndex + 1, endIndex))
            )
          )
        ))
      )

      // Patch the existing statement set
      stmts.patch(startLabelIndex, Seq(newLabel), endIndex - startLabelIndex)
    }
  }

  private[this] def removeUnusedLabelsFromStmts(ctx: Context, stmts: Seq[Node.Statement]): Seq[Node.Statement] = {
    stmts.map {
      case stmt @ Node.LabeledStatement(label, Some(innerStmt)) =>
        if (!ctx.usedLabels.contains(label.name)) innerStmt
        else stmt.copy(statement = removeUnusedLabelsFromStmts(ctx, Seq(innerStmt)).headOption)
      case stmt: Node.BlockStatement =>
        stmt.copy(removeUnusedLabelsFromStmts(ctx, stmt.statements))
      case other =>
        other
    }
  }

  private[this] def addTempVars(ctx: Context, stmts: Seq[Node.Statement]): Seq[Node.Statement] = {
    if (ctx.stack.tempVars.isEmpty) stmts
    else Node.DeclarationStatement(
      Node.GenericDeclaration(
        token = Node.Token.Var,
        specifications = ctx.stack.tempVars.toSeq.map { case (typ, tempVars) =>
          Node.ValueSpecification(
            names = tempVars.map(_.expr),
            typ = Some(ctx.classCtx.typeToGoType(typ)),
            values = Nil
          )
        }
      )
    ) +: stmts
  }
}

object MethodTranspiler extends MethodTranspiler {
  type StatementHandler = Seq[Node.Statement] => Seq[Node.Statement]
  type StatementBuilderFn = (Context, AbstractInsnNode, Option[StatementBuilder]) => Seq[Node.Statement]

  case class StatementBuilder(
    parent: Option[StatementBuilder],
    fn: StatementBuilderFn
  )

  case class Context(
    classCtx: ClassTranspiler.Context,
    methodNode: MethodNode,
    stack: MutableMethodStack = new MutableMethodStack()
  ) {
    // TODO: move this mutable stuff out
    var usedLabels = Set.empty[String]
    private[this] var statementHandlerStack = Seq.empty[StatementHandler]
    lazy val methodType = Type.getMethodType(methodNode.desc)
    lazy val instructions = methodNode.instructionIter.toList
    var instructionIndex = -1
    private[this] var localVariables = methodNode.debugLocalVariableNodes.map(v => v.index -> v).toMap
    private[this] lazy val localThis = {
      if (methodNode.access.isAccessStatic) None
      else Some(localVariables.getOrElse(0, {
        val thisVar = new LocalVariableNode("this", Type.getObjectType(classCtx.classNode.name).getDescriptor, null, null, null, 0)
        localVariables += 0 -> thisVar
        thisVar
      }))
    }

    private[this] var labelExpectedStackVars = Map[String, ]

    def previousInstructions = instructions.slice(0, instructionIndex + 1)

    def pushStatementHandler(handler: StatementHandler): Unit =
      statementHandlerStack.synchronized(statementHandlerStack :+= handler)

    def peekStatementHandler(): Option[StatementHandler] =
      statementHandlerStack.synchronized(statementHandlerStack.lastOption)

    def popStatementHandler(): Option[StatementHandler] =
      statementHandlerStack.synchronized {
        val ret = statementHandlerStack.lastOption
        if (ret.isDefined) statementHandlerStack = statementHandlerStack.dropRight(1)
        ret
      }

    var currentBuilder = Option.empty[StatementBuilder]

    def pushBuilder(fn: StatementBuilderFn): Unit = {
      currentBuilder = Some(StatementBuilder(
        parent = currentBuilder,
        fn = fn
      ))
    }

    def popBuilder(): Unit = {
      currentBuilder = currentBuilder.flatMap(_.parent)
    }

    /*
    TODO: we'll handle this stuff as a panic
    def nullPointerAssertion(expr: Node.Expression): Node.Statement = {
      Node.IfStatement(
        condition = Node.BinaryExpression(
          left = expr,
          operator = Node.Token.Eql,
          right = NilExpr
        ),
        body = Node.BlockStatement(Seq(throwError(
          Node.CallExpression(
            classCtx.constructorRefExpr("java/lang/NullPointerException", Type.getMethodType(Type.VOID_TYPE)),
            Seq.empty
          )
        )))
      )
    }
    */

    def throwError(expr: Node.Expression): Node.Statement = {
      Node.ExpressionStatement(Node.CallExpression("panic".toIdent, Seq(expr)))
    }

    def zeroOfType(typ: Type): Node.Expression = exprToType(0.toLit, Type.INT_TYPE, typ)

    def exprToType(expr: Node.Expression, oldType: Type, typ: Type): Node.Expression = {
      oldType.getSort -> typ.getSort match {
        case (Type.INT, Type.BOOLEAN) => expr match {
          case Node.BasicLiteral(Node.Token.Int, "1") => "true".toIdent
          case Node.BasicLiteral(Node.Token.Int, "0") => "false".toIdent
          case _ => sys.error(s"Unable to change int $expr to boolean")
        }
        case (oldSort, newSort) if oldSort == newSort =>
          expr
        case _ =>
          sys.error(s"Unable to convert from type $oldType to $typ")
      }
    }

    def staticClassRefExpr(internalName: String): Node.Expression = {
      // As a special case, if they want this class's static ref and we're inside the static init
      // of ourselves, only give the var
      if (methodNode.name == "<clinit>" && internalName == classCtx.classNode.name) {
        goStaticVarName(internalName).toIdent
      } else classCtx.staticClassRefExpr(internalName)
    }

    def localVariable(index: Int, lazilyCreateWithDesc: String): LocalVariableNode = {
      if (index == 0 && !methodNode.access.isAccessStatic) localThis.get
      else localVariables.getOrElse(index, {
        val localVariable = new LocalVariableNode(s"arg$index", lazilyCreateWithDesc, null, null, null, index)
        localVariables += index -> localVariable
        localVariable
      })
    }
  }
}
