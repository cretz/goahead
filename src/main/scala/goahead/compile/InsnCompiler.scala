package goahead.compile

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.tree._
import org.objectweb.asm.Opcodes

import scala.annotation.tailrec
import scala.util.control.NonFatal

trait InsnCompiler extends Logger {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insns: Seq[AbstractInsnNode]): (Context, Seq[Node.Statement]) =
    recursiveCompile(ctx, insns)

  @tailrec
  protected final def recursiveCompile(
    ctx: Context,
    insns: Seq[AbstractInsnNode],
    appendTo: Seq[Node.Statement] = Nil
  ): (Context, Seq[Node.Statement]) = {
    if (insns.isEmpty) ctx -> appendTo
    else {
      logger.debug(s"Compiling instruction - ${insns.head.pretty}")
      val ctxAndStmts = try {
        insns.head match {
          case i: FieldInsnNode => compile(ctx, i)
          case i: InsnNode => compile(ctx, i)
          case i: IntInsnNode => compile(ctx, i)
          case i: JumpInsnNode => compile(ctx, i)
          case i: LdcInsnNode => compile(ctx, i)
          case i: MethodInsnNode => compile(ctx, i)
          case i: TypeInsnNode => compile(ctx, i)
          case i: VarInsnNode => compile(ctx, i)
          case node => sys.error(s"Unrecognized node type: $node")
        }
      } catch {
        case NonFatal(e) =>
          val insn = insns.head
          throw new Exception(
            s"Unable to compile method ${ctx.cls.name}::${ctx.method.name} insn #${insn.index} - ${insn.pretty}",
            e
          )
      }
      recursiveCompile(ctxAndStmts._1, insns.tail, appendTo ++ ctxAndStmts._2)
    }
  }

  protected def compile(ctx: Context, insn: FieldInsnNode): (Context, Seq[Node.Statement]) = {
    import ctx.mangler
    insn.byOpcode {
      case Opcodes.GETFIELD =>
        ctx.stackPopped { case (ctx, item) =>
          ctx.stackPushed(
            TypedExpression(
              expr = item.expr.sel(mangler.fieldName(insn.owner, insn.name)),
              typ = IType.getType(insn.desc),
              cheapRef = true
            )
          ) -> Nil
        }
      case Opcodes.GETSTATIC =>
        ctx.staticInstRefExpr(insn.owner).leftMap { case (ctx, expr) =>
          ctx.stackPushed(TypedExpression(
            expr = expr.sel(mangler.fieldName(insn.owner, insn.name)),
            typ = IType.getType(insn.desc),
            cheapRef = true
          )) -> Nil
        }
      case Opcodes.PUTFIELD =>
        ctx.stackPopped(2, { case (ctx, Seq(objectRef, value)) =>
          val field = objectRef.expr.sel(mangler.fieldName(insn.owner, insn.name))
          value.toExprNode(ctx, IType.getType(insn.desc)).leftMap { case (ctx, value) =>
            ctx -> field.assignExisting(value).singleSeq
          }
        })
      case Opcodes.PUTSTATIC =>
        ctx.staticInstRefExpr(insn.owner).leftMap { case (ctx, expr) =>
          ctx.stackPopped { case (ctx, value) =>
            value.toExprNode(ctx, IType.getType(insn.desc)).leftMap { case (ctx, value) =>
              ctx -> expr.sel(mangler.fieldName(insn.owner, insn.name)).assignExisting(value).singleSeq
            }
          }
        }
    }
  }

  protected def compile(ctx: Context, insn: InsnNode): (Context, Seq[Node.Statement]) = {
    @inline
    def iconst(i: Int): (Context, Seq[Node.Statement]) =
      ctx.stackPushed(TypedExpression(i.toLit, IType.IntType, cheapRef = true)) -> Nil

    insn.byOpcode {
      case Opcodes.ACONST_NULL =>
        ctx.stackPushed(TypedExpression(NilExpr, IType.NullType, cheapRef = true)) -> Nil
      case Opcodes.ARETURN =>
        ctx.stackPopped { case (ctx, item) =>
          item.toExprNode(ctx, IType.getReturnType(ctx.method.desc)).leftMap { case (ctx, item) =>
            ctx -> item.ret.singleSeq
          }
        }
      case Opcodes.ATHROW =>
        ctx.stackPopped { case (ctx, item) =>
          ctx -> "panic".toIdent.call(item.expr.singleSeq).toStmt.singleSeq
        }
      case Opcodes.DUP =>
        // We only dupe things that are not cheap references, otherwise we make a temp var
        ctx.stackPopped { case (ctx, item) =>
          val (newCtx, entry, stmtOpt) =
            if (item.cheapRef) (ctx, item, None)
            else {
              ctx.getTempVar(item.typ).leftMap { case (ctx, tempVar) =>
                (ctx, tempVar, Some(tempVar.name.toIdent.assignExisting(item.expr)))
              }
            }
            // Push it twice
            newCtx.stackPushed(entry).stackPushed(entry) -> stmtOpt.toSeq
        }
      case Opcodes.IADD =>
        ctx.stackPopped(2, { case (ctx, Seq(left, right)) =>
          // TODO: determine proper union type between the two
          ctx.stackPushed(TypedExpression(left.expr + right.expr, IType.IntType, cheapRef = false)) -> Nil
        })
      case Opcodes.ICONST_0 =>
        iconst(0)
      case Opcodes.ICONST_1 =>
        iconst(1)
      case Opcodes.ICONST_2 =>
        iconst(2)
      case Opcodes.ICONST_3 =>
        iconst(3)
      case Opcodes.ICONST_4 =>
        iconst(4)
      case Opcodes.ICONST_5 =>
        iconst(5)
      case Opcodes.ICONST_M1 =>
        iconst(-1)
      case Opcodes.POP =>
        // We need to just take what is on the stack and make it a statement as this
        // is often just an ignored return value or something
        // TODO: ignore cheap refs?
        ctx.stackPopped { case (ctx, item) =>
          ctx -> item.expr.toStmt.singleSeq
        }
      case Opcodes.RETURN =>
        ctx -> emptyReturn.singleSeq
    }
  }

  protected def compile(ctx: Context, insn: IntInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.BIPUSH =>
        ctx.stackPushed(insn.operand.toTypedLit) -> Nil
    }
  }

  protected def compile(ctx: Context, insn: JumpInsnNode): (Context, Seq[Node.Statement]) = {
    val label = insn.label.getLabel.toString
    insn.byOpcode {
      case Opcodes.GOTO =>
        // TODO: can we trust that these jumps are not to F_FULL/F_SAME1?
        ctx.copy(usedLabels = ctx.usedLabels + label) -> goto(label).singleSeq
      case Opcodes.IFEQ =>
        ctx.copy(usedLabels = ctx.usedLabels + label).stackPopped { case (ctx, item) =>
          ctx -> ifEq(item.expr, item.typ.zeroExpr, goto(label)).singleSeq
        }
    }
  }

  protected def compile(ctx: Context, insn: LdcInsnNode): (Context, Seq[Node.Statement]) = {
    insn.cst match {
      case s: String =>
        ctx.newString(s).leftMap { case (ctx, str) =>
          ctx.stackPushed(TypedExpression(str, StringType, cheapRef = true)) -> Nil
        }
      case cst =>
        sys.error(s"Unrecognized LDC type: $cst")
    }
  }

  protected def compile(ctx: Context, insn: MethodInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.INVOKESPECIAL if insn.name == "<init>" && ctx.method.name == "<init>" =>
        require(IType.getReturnType(insn.desc) == IType.VoidType)
        // If we're inside of init ourselves and this call is init, we know we're
        // actually running init on ourselves
        ctx.stackPopped { case (ctx, subject) =>
          subject.toExprNode(ctx, IType.getObjectType(insn.owner)).leftMap { case (ctx, subject) =>
            val inst = subject.sel(ctx.mangler.instanceObjectName(insn.owner))
            val init = inst.sel(ctx.mangler.methodName(insn.name, insn.desc))
            ctx -> init.call().toStmt.singleSeq
          }
        }
      case Opcodes.INVOKEVIRTUAL | Opcodes.INVOKESPECIAL =>
        // Note: we let null pointer exceptions cause a panic instead of checking them
        val (argTypes, retType) = IType.getArgumentAndReturnTypes(insn.desc)
        ctx.stackPopped(argTypes.length + 1, { case (ctx, subject +: args) =>
          subject.toExprNode(ctx, IType.getObjectType(insn.owner)).leftMap { case (ctx, subject) =>
            val method = subject.sel(ctx.mangler.methodName(insn.name, insn.desc))
            val ctxAndCallVals = args.zip(argTypes).foldLeft(ctx -> Seq.empty[Node.Expression]) {
              case ((ctx, prevExprs), (typedExpr, typ)) =>
                typedExpr.toExprNode(ctx, typ).leftMap { case (ctx, newExpr) =>
                  ctx -> (prevExprs :+ newExpr)
                }
            }
            ctxAndCallVals.leftMap { case (ctx, callVals) =>
              val call = method.call(callVals)
              // Put it on the stack if not void
              if (retType == IType.VoidType) ctx -> call.toStmt.singleSeq
              else ctx.stackPushed(TypedExpression(call, retType, cheapRef = false)) -> Nil
            }
          }
        })
    }
  }

  protected def compile(ctx: Context, insn: TypeInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode {
      case Opcodes.NEW =>
        // Just create the struct and put the entire instantiation on the stack
        ctx.staticNewExpr(insn.desc).leftMap { case (ctx, newExpr) =>
          ctx.stackPushed(
            TypedExpression(newExpr, IType.getObjectType(insn.desc), cheapRef = false)
          ) -> Nil
        }
    }
  }

  protected def compile(ctx: Context, insn: VarInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode  {
      case Opcodes.ALOAD =>
        ctx.getLocalVar(insn.`var`, ObjectType).leftMap { case (ctx, local) =>
          ctx.stackPushed(local) -> Nil
        }
      case Opcodes.ASTORE =>
        ctx.stackPopped { case (ctx, value) =>
          ctx.getLocalVar(insn.`var`, value.typ).leftMap { case (ctx, local) =>
            value.toExprNode(ctx, local.typ).leftMap { case (ctx, value) =>
              ctx -> local.expr.assignExisting(value).singleSeq
            }
          }
        }
    }
  }
}

object InsnCompiler extends InsnCompiler