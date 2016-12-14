package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnNode

trait ZeroOpInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: InsnNode): (Context, Seq[Node.Statement]) = {

    insn.byOpcode {
      case Opcodes.ACONST_NULL =>
        ctx.stackPushed(TypedExpression(NilExpr, IType.NullType, cheapRef = true)) -> Nil
      case Opcodes.ARRAYLENGTH =>
        ctx.stackPopped { case (ctx, arrayRef) =>
          arrayRef.toGeneralArray(ctx).map { case (ctx, arrayRef) =>
            ctx.stackPushed(TypedExpression(
              arrayRef.sel("Len").call(),
              IType.IntType,
              cheapRef = false
            )) -> Nil
          }
        }
      case Opcodes.ARETURN =>
        ctx.stackPopped { case (ctx, item) =>
          item.toExprNode(ctx, IType.getReturnType(ctx.method.desc)).map { case (ctx, item) =>
            ctx -> item.ret.singleSeq
          }
        }
      case Opcodes.ATHROW =>
        ctx.stackPopped { case (ctx, item) =>
          ctx -> "panic".toIdent.call(item.expr.singleSeq).toStmt.singleSeq
        }
      case Opcodes.DCONST_0 | Opcodes.DCONST_1 =>
        dconst(ctx, insn.getOpcode)
      case Opcodes.DCMPG | Opcodes.DCMPL =>
        dcmp(ctx, insn.getOpcode == Opcodes.DCMPG)
      case Opcodes.DUP =>
        // We only dupe things that are not cheap references, otherwise we make a temp var
        ctx.stackPopped { case (ctx, item) =>
          val (newCtx, entry, stmtOpt) =
            if (item.cheapRef) (ctx, item, None)
            else {
              ctx.getTempVar(item.typ).map { case (ctx, tempVar) =>
                (ctx, tempVar, Some(tempVar.name.toIdent.assignExisting(item.expr)))
              }
            }
          // Push it twice
          newCtx.stackPushed(entry).stackPushed(entry) -> stmtOpt.toSeq
        }
      case Opcodes.F2D =>
        ctx.stackPopped { case (ctx, item) =>
          item.toExprNode(ctx, IType.DoubleType).map { case (ctx, convertedItem) =>
            ctx.stackPushed(TypedExpression(convertedItem, IType.DoubleType, cheapRef = true)) -> Nil
          }
        }
      case Opcodes.FCMPG | Opcodes.FCMPL =>
        fcmp(ctx, insn.getOpcode == Opcodes.FCMPG)
      case Opcodes.FCONST_0 | Opcodes.FCONST_1 | Opcodes.FCONST_2 =>
        fconst(ctx, insn.getOpcode)
      case Opcodes.IADD =>
        ctx.stackPopped(2, { case (ctx, Seq(left, right)) =>
          // TODO: determine proper union type between the two
          ctx.stackPushed(TypedExpression(left.expr + right.expr, IType.IntType, cheapRef = false)) -> Nil
        })
      case Opcodes.IALOAD | Opcodes.LALOAD | Opcodes.FALOAD | Opcodes.DALOAD |
           Opcodes.AALOAD | Opcodes.BALOAD | Opcodes.CALOAD | Opcodes.SALOAD =>
        aload(ctx, insn.getOpcode)
      case Opcodes.IASTORE | Opcodes.LASTORE | Opcodes.FASTORE | Opcodes.DASTORE |
           Opcodes.AASTORE | Opcodes.BASTORE | Opcodes.CASTORE | Opcodes.SASTORE =>
        astore(ctx, insn.getOpcode)
      case Opcodes.ICONST_0 | Opcodes.ICONST_1 | Opcodes.ICONST_2 | Opcodes.ICONST_3 |
           Opcodes.ICONST_4 | Opcodes.ICONST_5 | Opcodes.ICONST_M1 =>
        iconst(ctx, insn.getOpcode)
      case Opcodes.LCMP =>
        lcmp(ctx)
      case Opcodes.LCONST_0 | Opcodes.LCONST_1 =>
        lconst(ctx, insn.getOpcode)
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

  protected def astore(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped(3, { case (ctx, Seq(arrayRef, index, value)) =>
      opcode match {
        case Opcodes.BASTORE =>
          ctx.withRuntimeImportAlias.map { case (ctx, rtAlias) =>
            ctx -> rtAlias.toIdent.sel("SetBoolOrByte").call(Seq(
              arrayRef.expr, index.expr, value.expr
            )).toStmt.singleSeq
          }
        case _ =>
          val jvmType = opcode match {
            case Opcodes.IASTORE => IType.IntType.asArray
            case Opcodes.LASTORE => IType.LongType.asArray
            case Opcodes.FASTORE => IType.FloatType.asArray
            case Opcodes.DASTORE => IType.DoubleType.asArray
            case Opcodes.AASTORE => IType.getObjectType("java/lang/Object").asArray
            case Opcodes.CASTORE => IType.CharType.asArray
            case Opcodes.SASTORE => IType.ShortType.asArray
          }
          value.toExprNode(ctx, jvmType.elementType).map { case (ctx, typedValue) =>
            arrayRef.toExprNode(ctx, jvmType).map { case (ctx, convArrayRef) =>
              ctx -> convArrayRef.sel("Set").call(Seq(index.expr, typedValue)).toStmt.singleSeq
            }
          }
      }
    })
  }

  protected def aload(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped(2, { case (ctx, Seq(arrayRef, index)) =>
      // Convert to byte or bool array if necessary
      opcode match {
        case Opcodes.BALOAD =>
          ctx.withRuntimeImportAlias.map { case (ctx, rtAlias) =>
            ctx.stackPushed(TypedExpression(
              rtAlias.toIdent.sel("GetBoolOrByte").call(Seq(arrayRef.expr, index.expr)),
              IType.IntType,
              cheapRef = true
            )) -> Nil
          }
        case _ =>
          val jvmType = opcode match {
            case Opcodes.IALOAD => IType.IntType.asArray
            case Opcodes.LALOAD => IType.LongType.asArray
            case Opcodes.FALOAD => IType.FloatType.asArray
            case Opcodes.DALOAD => IType.DoubleType.asArray
            case Opcodes.AALOAD => IType.getObjectType("java/lang/Object").asArray
            case Opcodes.CALOAD => IType.CharType.asArray
            case Opcodes.SALOAD => IType.ShortType.asArray
          }
          arrayRef.toExprNode(ctx, jvmType).map { case (ctx, convArrayRef) =>
            ctx.stackPushed(TypedExpression(
              convArrayRef.sel("Get").call(Seq(index.expr)),
              jvmType.elementType,
              cheapRef = true
            )) -> Nil
          }
      }
    })
  }

  protected def cmp(
    ctx: Context,
    methodName: String,
    additionalArg: Option[Node.Expression] = None
  ): (Context, Seq[Node.Statement]) = {
    ctx.withRuntimeImportAlias.map { case (ctx, rtAlias) =>
      ctx.stackPopped(2, { case (ctx, Seq(val1, val2)) =>
        ctx.stackPushed(TypedExpression(
          rtAlias.toIdent.sel(methodName).call(Seq(val1.expr, val2.expr) ++ additionalArg),
          IType.IntType,
          cheapRef = false
        )) -> Nil
      })
    }
  }

  protected def dcmp(ctx: Context, nanMeansOne: Boolean): (Context, Seq[Node.Statement]) = {
    cmp(ctx, "CompareDouble", Some(nanMeansOne.toLit))
  }

  protected def fcmp(ctx: Context, nanMeansOne: Boolean): (Context, Seq[Node.Statement]) = {
    cmp(ctx, "CompareFloat", Some(nanMeansOne.toLit))
  }

  protected def lcmp(ctx: Context): (Context, Seq[Node.Statement]) = {
    cmp(ctx, "CompareLong")
  }

  protected def dconst(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    @inline
    def dconst(d: Double): (Context, Seq[Node.Statement]) =
      ctx.stackPushed(TypedExpression(d.toLit, IType.DoubleType, cheapRef = true)) -> Nil
    opcode match {
      case Opcodes.DCONST_0 => dconst(0)
      case Opcodes.DCONST_1 => dconst(1)
    }
  }

  protected def fconst(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    @inline
    def fconst(f: Float): (Context, Seq[Node.Statement]) =
      ctx.stackPushed(TypedExpression(f.toLit, IType.FloatType, cheapRef = true)) -> Nil
    opcode match {
      case Opcodes.FCONST_0 => fconst(0)
      case Opcodes.FCONST_1 => fconst(1)
      case Opcodes.FCONST_2 => fconst(2)
    }
  }

  protected def iconst(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    @inline
    def iconst(i: Int): (Context, Seq[Node.Statement]) =
      ctx.stackPushed(TypedExpression(i.toLit, IType.IntType, cheapRef = true)) -> Nil

    opcode match {
      case Opcodes.ICONST_0 => iconst(0)
      case Opcodes.ICONST_1 => iconst(1)
      case Opcodes.ICONST_2 => iconst(2)
      case Opcodes.ICONST_3 => iconst(3)
      case Opcodes.ICONST_4 => iconst(4)
      case Opcodes.ICONST_5 => iconst(5)
      case Opcodes.ICONST_M1 => iconst(-1)
    }
  }

  protected def lconst(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    @inline
    def lconst(l: Long): (Context, Seq[Node.Statement]) =
      ctx.stackPushed(TypedExpression(l.toLit, IType.LongType, cheapRef = true)) -> Nil
    opcode match {
      case Opcodes.LCONST_0 => lconst(0)
      case Opcodes.LCONST_1 => lconst(1)
    }
  }
}
