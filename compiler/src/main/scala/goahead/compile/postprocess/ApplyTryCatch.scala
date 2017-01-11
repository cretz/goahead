package goahead.compile
package postprocess

import goahead.ast.Node
import goahead.compile.MethodCompiler._
import org.objectweb.asm.tree.LabelNode

trait ApplyTryCatch extends PostProcessor {
  import AstDsl._
  import Helpers._

  override def apply(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    val blocks = ctx.method.tryCatchBlocks
    // No try catch blocks means no change
    if (blocks.isEmpty) ctx -> stmts else {
      addCurrentLabelAssign(ctx, stmts).map { case (ctx, stmts) =>
        wrapEntireBody(ctx, stmts)
      }
    }
  }

  protected def addCurrentLabelAssign(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Add the current label setter to each label section
    ctx -> stmts.map {
      case Node.LabeledStatement(Node.Identifier(label), Node.BlockStatement(stmts)) =>
        labeled(label, "currentLabel".toIdent.assignExisting(label.toLit) +: stmts)
      case stmt =>
        stmt
    }
  }

  protected def wrapEntireBody(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Need the defer statement to catch the panic
    deferStmt(ctx).map { case (ctx, deferStmt) =>
      currentExIfStmt(ctx).map { case (ctx, exIfStmt) =>
        wrapAndInvokeStmts(ctx, Seq(deferStmt, exIfStmt) ++ stmts).map { case (ctx, stmts) =>
          handleAfterInvoke(ctx, stmts).map { case (ctx, stmts) =>
            addCurrentVars(ctx, stmts)
          }
        }
      }
    }
  }

  protected def deferStmt(ctx: Context): (Context, Node.Statement) = {
    ctx.importRuntimeQualifiedName("PanicToThrowable").map { case (ctx, panicToThrowable) =>
      val recoverStmt = iff(
        init = Some("r".toIdent.assignDefine("recover".toIdent.call())),
        lhs = "r".toIdent,
        op = Node.Token.Neq,
        rhs = NilExpr,
        body = Seq("currentEx".toIdent.assignExisting(panicToThrowable.call(Seq("r".toIdent))))
      )
      ctx -> funcType(Nil, None).toFuncLit(Seq(
        "currentEx".toIdent.assignExisting(NilExpr),
        recoverStmt
      )).call().defer
    }
  }

  protected def currentExIfStmt(ctx: Context): (Context, Node.IfStatement) = {
    handlerIfElseStmt(ctx).map { case (ctx, handlerIfElseStmt) =>
      ctx -> iff(
        init = None,
        lhs = "currentEx".toIdent,
        op = Node.Token.Neq,
        rhs = NilExpr,
        body = handlerIfElseStmt.singleSeq
      )
    }
  }

  protected def handlerIfElseStmt(ctx: Context): (Context, Node.Statement) = {
    require(ctx.method.tryCatchBlocks.nonEmpty)
    // Conditional for each try catch node
    val ctxAndIfStmts = ctx.method.tryCatchBlocks.foldLeft(ctx -> Seq.empty[Node.IfStatement]) {
      case ((ctx, ifStmts), tryCatchBlock) =>
        val labelNames = getLabelNamesInRange(ctx, tryCatchBlock.start, tryCatchBlock.end)
        require(labelNames.nonEmpty)

        // Build the label checks
        @inline
        def currentLabelCheck(labelName: String): Node.Expression = "currentLabel".toIdent.eql(labelName.toLit)
        val labelCond = labelNames.tail.foldLeft(currentLabelCheck(labelNames.head)) {
          case (cond, labelName) => cond.orOr(currentLabelCheck(labelName))
        }

        // Do type assertion if necessary
        val ctxAndCondWithInitOpt = Option(tryCatchBlock.`type`) match {
          case None => ctx -> (labelCond -> None)
          case Some(exType) => ctx.typeToGoType(IType.getObjectType(exType)).map { case (ctx, exType) =>
            val init = assignDefineMultiple(
              Seq("ex".toIdent, "ok".toIdent),
              Seq("currentEx".toIdent.typeAssert(exType))
            )
            val cond = "ok".toIdent.andAnd(if (labelNames.size > 1) labelCond.inParens else labelCond)
            ctx -> (cond -> Some(init))
          }
        }

        ctxAndCondWithInitOpt.map { case (ctx, (cond, initOpt)) =>
          // Need to set stack var and then goto handler label
          val exIdent = if (initOpt.isDefined) "ex".toIdent else "currentEx".toIdent
          val labelStr = tryCatchBlock.handler.getLabel.uniqueStr
          val stmts = Seq(
            (tryCatchBlock.handler.getLabel + "_stack0").toIdent.assignExisting(exIdent),
            goto(labelStr)
          )
          ctx.copy(usedLabels = ctx.usedLabels + labelStr) -> (ifStmts :+ iff(initOpt, cond, stmts))
        }
    }

    // Now combine the if statements w/ a panic on the last else
    ctxAndIfStmts.map { case (ctx, ifStmts) =>
      val panicStmt: Node.Statement = block("panic".toIdent.call(Seq("currentEx".toIdent)).toStmt.singleSeq)
      ctx -> ifStmts.foldRight(panicStmt) { case (ifStmt, prevStmt) => ifStmt.copy(elseStatement = Some(prevStmt)) }
    }
  }

  protected def getLabelNamesInRange(ctx: Context, start: LabelNode, endExclusive: LabelNode): Seq[String] = {
    val startIndex = ctx.sets.indexWhere(_.label.getLabel == start.getLabel)
    val endIndex = ctx.sets.lastIndexWhere(_.label.getLabel == endExclusive.getLabel)
    require(startIndex != -1 && endIndex != -1 && startIndex <= endIndex)
    ctx.sets.slice(startIndex, endIndex).map(_.label.getLabel.uniqueStr)
  }

  protected def wrapAndInvokeStmts(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Take all the statements and wrap in one big anon func, invoke, and store to result var if there is a result
    val ctxAndRetTypeOpt = IType.getReturnType(ctx.method.desc) match {
      case IType.VoidType => ctx -> None
      case typ => ctx.typeToGoType(typ).map { case (ctx, retTyp) => ctx -> Some(retTyp) }
    }

    ctxAndRetTypeOpt.map { case (ctx, retTypOpt) =>
      val called = funcType(Nil, retTypOpt).toFuncLit(stmts).call()
      val wrapped = retTypOpt match {
        case None => called.toStmt
        case Some(_) => "ret".toIdent.assignDefine(called)
      }
      ctx -> wrapped.labeled("body").singleSeq
    }
  }

  protected def handleAfterInvoke(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    val ifGotoStmt = iff(
      init = None,
      lhs = "currentEx".toIdent,
      op = Node.Token.Neq,
      rhs = NilExpr,
      body = goto("body").singleSeq
    )
    val retStmtOpt = if (IType.getReturnType(ctx.method.desc) == IType.VoidType) None else {
      Some("ret".toIdent.ret)
    }
    ctx.copy(usedLabels = ctx.usedLabels + "body") -> (stmts ++ ifGotoStmt.singleSeq ++ retStmtOpt)
  }

  protected def addCurrentVars(ctx: Context, stmts: Seq[Node.Statement]): (Context, Seq[Node.Statement]) = {
    // Add a function var for currentEx and a regular string currentLabel var
    val currentExVar = TypedExpression.namedVar("currentEx", IType.getObjectType("java/lang/Throwable"))
    val currentLabelVarDecl = varDecl("currentLabel", "string".toIdent).toStmt
    ctx.copy(functionVars = ctx.functionVars :+ currentExVar) -> (currentLabelVarDecl +: stmts)
  }
}

object ApplyTryCatch extends ApplyTryCatch
