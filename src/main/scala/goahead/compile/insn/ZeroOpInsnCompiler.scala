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
      case Opcodes.ARETURN | Opcodes.DRETURN | Opcodes.FRETURN | Opcodes.IRETURN | Opcodes.LRETURN =>
        ctx.stackPopped { case (ctx, item) =>
          item.toExprNode(ctx, IType.getReturnType(ctx.method.desc)).map { case (ctx, item) =>
            ctx -> item.ret.singleSeq
          }
        }
      case Opcodes.ATHROW =>
        ctx.stackPopped { case (ctx, item) =>
          ctx -> "panic".toIdent.call(item.expr.singleSeq).toStmt.singleSeq
        }
      case Opcodes.D2F | Opcodes.D2I | Opcodes.D2L | Opcodes.F2D | Opcodes.F2I | Opcodes.F2L |
           Opcodes.I2B | Opcodes.I2C | Opcodes.I2D | Opcodes.I2F | Opcodes.I2L | Opcodes.I2S |
           Opcodes.L2D | Opcodes.L2F | Opcodes.L2I =>
        primitive2Primitive(ctx, insn.getOpcode)
      case Opcodes.DADD | Opcodes.FADD | Opcodes.IADD | Opcodes.LADD =>
        add(ctx, insn.getOpcode)
      case Opcodes.DCONST_0 | Opcodes.DCONST_1 =>
        dconst(ctx, insn.getOpcode)
      case Opcodes.DCMPG | Opcodes.DCMPL =>
        dcmp(ctx, insn.getOpcode == Opcodes.DCMPG)
      case Opcodes.DDIV | Opcodes.FDIV | Opcodes.IDIV | Opcodes.LDIV =>
        div(ctx, insn.getOpcode)
      case Opcodes.DMUL | Opcodes.FMUL | Opcodes.IMUL | Opcodes.LMUL =>
        mul(ctx, insn.getOpcode)
      case Opcodes.DNEG | Opcodes.FNEG | Opcodes.INEG | Opcodes.LNEG =>
        neg(ctx, insn.getOpcode)
      case Opcodes.DREM | Opcodes.FREM | Opcodes.IREM | Opcodes.LREM =>
        rem(ctx, insn.getOpcode)
      case Opcodes.DSUB | Opcodes.FSUB | Opcodes.ISUB | Opcodes.LSUB =>
        sub(ctx, insn.getOpcode)
      case Opcodes.DUP =>
        dup(ctx, 1)(s => s ++ s)
      case Opcodes.DUP_X1 =>
        dup(ctx, 2)(s => s.last +: s)
      case Opcodes.DUP_X2 =>
        dup(ctx, 3)(s => s.last +: s)
      case Opcodes.DUP2 =>
        ctx.stack.peek().typ match {
          case IType.LongType | IType.DoubleType => dup(ctx, 1)(s => s ++ s)
          case _ => dup(ctx, 2)(s => s ++ s)
        }
      case Opcodes.DUP2_X1 =>
        ctx.stack.peek().typ match {
          case IType.LongType | IType.DoubleType => dup(ctx, 2)(s => s.last +: s)
          case _ => dup(ctx, 3)(s => s.takeRight(2) ++ s)
        }
      case Opcodes.DUP2_X2 =>
        ctx.stack.peek().typ match {
          case IType.LongType | IType.DoubleType => dup(ctx, 3)(s => s.last +: s)
          case _ => dup(ctx, 4)(s => s.takeRight(2) ++ s)
        }
      case Opcodes.FCMPG | Opcodes.FCMPL =>
        fcmp(ctx, insn.getOpcode == Opcodes.FCMPG)
      case Opcodes.FCONST_0 | Opcodes.FCONST_1 | Opcodes.FCONST_2 =>
        fconst(ctx, insn.getOpcode)
      case Opcodes.IALOAD | Opcodes.LALOAD | Opcodes.FALOAD | Opcodes.DALOAD |
           Opcodes.AALOAD | Opcodes.BALOAD | Opcodes.CALOAD | Opcodes.SALOAD =>
        aload(ctx, insn.getOpcode)
      case Opcodes.IAND | Opcodes.LAND =>
        and(ctx, insn.getOpcode)
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
      case Opcodes.IOR | Opcodes.LOR =>
        or(ctx, insn.getOpcode)
      case Opcodes.ISHL | Opcodes.ISHR | Opcodes.IUSHR |
           Opcodes.LSHL | Opcodes.LSHR | Opcodes.LUSHR =>
        sh(ctx, insn.getOpcode)
      case Opcodes.IXOR | Opcodes.LXOR =>
        xor(ctx, insn.getOpcode)
      case Opcodes.MONITORENTER | Opcodes.MONITOREXIT =>
        monitor(ctx, insn.getOpcode)
      case Opcodes.NOP =>
        ctx -> Nil
      case Opcodes.POP | Opcodes.POP2 =>
        pop(ctx, insn.getOpcode)
      case Opcodes.RETURN =>
        ctx -> emptyReturn.singleSeq
      case Opcodes.SWAP =>
        ctx.stackPopped(2, { case (ctx, Seq(first, second)) =>
          ctx.stackPushed(second).stackPushed(first) -> Nil
        })
    }
  }

  protected def add(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.Add, opcode match {
      case Opcodes.DADD => IType.DoubleType
      case Opcodes.FADD => IType.FloatType
      case Opcodes.IADD => IType.IntType
      case Opcodes.LADD => IType.LongType
    })
  }

  protected def aload(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped(2, { case (ctx, Seq(arrayRef, index)) =>
      // Convert to byte or bool array if necessary
      opcode match {
        case Opcodes.BALOAD =>
          ctx.importRuntimeQualifiedName("GetBoolOrByte").map { case (ctx, getBoolOrByte) =>
            index.toExprNode(ctx, IType.IntType).map { case (ctx, index) =>
              ctx.stackPushed(TypedExpression(
                getBoolOrByte.call(Seq(arrayRef.expr, index)),
                IType.IntType,
                cheapRef = true
              )) -> Nil
            }
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
              cheapRef = false
            )) -> Nil
          }
      }
    })
  }

  protected def and(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.And, opcode match {
      case Opcodes.IAND => IType.IntType
      case Opcodes.LAND => IType.LongType
    })
  }

  protected def astore(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped(3, { case (ctx, Seq(arrayRef, index, value)) =>
      opcode match {
        case Opcodes.BASTORE =>
          ctx.importRuntimeQualifiedName("SetBoolOrByte").map { case (ctx, setBoolOrByte) =>
            index.toExprNode(ctx, IType.IntType).map { case (ctx, index) =>
              value.toExprNode(ctx, IType.IntType).map { case (ctx, value) =>
                ctx -> setBoolOrByte.call(Seq(arrayRef.expr, index, value)).toStmt.singleSeq
              }
            }
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

  protected def binary(ctx: Context, tok: Node.Token, typ: IType): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped(2, { case (ctx, Seq(val1, val2)) =>
      // Values have to be of the same type
      val1.toExprNode(ctx, typ).map { case (ctx, val1) =>
        val2.toExprNode(ctx, typ).map { case (ctx, val2) =>
          ctx.stackPushed(TypedExpression(
            expr = val1.binary(tok, val2),
            typ = typ,
            cheapRef = false
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
    ctx.importRuntimeQualifiedName(methodName).map { case (ctx, cmpMethod) =>
      ctx.stackPopped(2, { case (ctx, Seq(val1, val2)) =>
        ctx.stackPushed(TypedExpression(
          cmpMethod.call(Seq(val1.expr, val2.expr) ++ additionalArg),
          IType.IntType,
          cheapRef = false
        )) -> Nil
      })
    }
  }

  protected def dcmp(ctx: Context, nanMeansOne: Boolean): (Context, Seq[Node.Statement]) = {
    cmp(ctx, "CompareDouble", Some(nanMeansOne.toLit))
  }

  protected def div(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.Quo, opcode match {
      case Opcodes.DDIV => IType.DoubleType
      case Opcodes.FDIV => IType.FloatType
      case Opcodes.IDIV => IType.IntType
      case Opcodes.LDIV => IType.LongType
    })
  }

  protected def dup(
    ctx: Context,
    pullFromStackCount: Int
  )(fn: Seq[TypedExpression] => Seq[TypedExpression]): (Context, Seq[Node.Statement]) = {
    // Basically, we pop the stack, ask the fn to give us back what to put on
    // and for any that are in there more than once and are not cheap refs we create temp vars for
    ctx.stackPopped(pullFromStackCount, { case (ctx, popped) =>
      val newStackAdd = fn(popped)
      // Keyed by the type, value is a seq of indices where they are in newStackAdd
      val grouped = newStackAdd.zipWithIndex.groupBy(_._1).mapValues(_.map(_._2))
      val ctxAndTempSafeNewStackWithStmts = grouped.foldLeft(ctx -> (newStackAdd -> Seq.empty[Node.Statement])) {
        case (state, (groupedItem, _)) if groupedItem.cheapRef =>
          state
        case ((ctx, (stack, stmts)), (groupedItem, allInstancesOfItem)) =>
          ctx.getTempVar(groupedItem.typ).map { case (ctx, tempVar) =>
            val updatedStack = allInstancesOfItem.foldLeft(stack) { case (stack, index) =>
              stack.updated(index, tempVar)
            }
            ctx -> (updatedStack -> (stmts :+ tempVar.name.toIdent.assignExisting(groupedItem.expr)))
          }
      }
      ctxAndTempSafeNewStackWithStmts.map { case (ctx, (newStackAdd, stmts)) =>
        // Now add everything from our new stack to the existing stack
        newStackAdd.foldLeft(ctx)(_.stackPushed(_)) -> stmts
      }
    })
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

  protected def monitor(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    val func = if (opcode == Opcodes.MONITORENTER) "MonitorEnter" else "MonitorExit"
    ctx.importRuntimeQualifiedName(func).map { case (ctx, monitorFunc) =>
      ctx.stackPopped { case (ctx, ref) =>
        ctx -> monitorFunc.call(Seq(ref.expr)).toStmt.singleSeq
      }
    }
  }

  protected def mul(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.Mul, opcode match {
      case Opcodes.DMUL => IType.DoubleType
      case Opcodes.FMUL => IType.FloatType
      case Opcodes.IMUL => IType.IntType
      case Opcodes.LMUL => IType.LongType
    })
  }

  protected def neg(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    val typ = opcode match {
      case Opcodes.DNEG => IType.DoubleType
      case Opcodes.FNEG => IType.FloatType
      case Opcodes.INEG => IType.IntType
      case Opcodes.LNEG => IType.LongType
    }
    ctx.stackPopped { case (ctx, v) =>
      ctx.stackPushed(TypedExpression(
        expr = v.expr.unary(Node.Token.Sub),
        typ = typ,
        cheapRef = false
      )) -> Nil
    }
  }

  protected def or(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.Or, opcode match {
      case Opcodes.IOR => IType.IntType
      case Opcodes.LOR => IType.LongType
    })
  }

  protected def pop(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    // We need to just take what is on the stack and make it a statement as this
    // is often just an ignored return value or something
    // TODO: ignore cheap refs or somehow otherwise check that this is "statementable"?
    val amountToPop = opcode match {
      case Opcodes.POP => 1
      case Opcodes.POP2 => ctx.stack.peek().typ match {
        case IType.LongType | IType.DoubleType => 1
        case _ => 2
      }
    }
    ctx.stackPopped(amountToPop, { case (ctx, items) =>
      ctx -> items.map { item =>
        // If there is a type, we just assign to a non-existent var, otherwise just run
        item.typ match {
          case t: IType.Simple if t != IType.VoidType => "_".toIdent.assignExisting(item.expr)
          case _ => item.expr.toStmt
        }
      }
    })
  }

  protected def primitive2Primitive(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    // TODO: FPStrict
    // TODO: Test round-to-nearest rules in Go and Java
    ctx.stackPopped { case (ctx, item) =>
      // TODO: reduce this to only the target type once we are sure we don't need the oldType
      val (oldType, newType) = opcode match {
        case Opcodes.D2F => IType.DoubleType -> IType.FloatType
        case Opcodes.D2I => IType.DoubleType -> IType.IntType
        case Opcodes.D2L => IType.DoubleType -> IType.LongType
        case Opcodes.F2D => IType.FloatType -> IType.DoubleType
        case Opcodes.F2I => IType.FloatType -> IType.IntType
        case Opcodes.F2L => IType.FloatType -> IType.LongType
        case Opcodes.I2B => IType.IntType -> IType.ByteType
        case Opcodes.I2C => IType.IntType -> IType.CharType
        case Opcodes.I2D => IType.IntType -> IType.DoubleType
        case Opcodes.I2F => IType.IntType -> IType.FloatType
        case Opcodes.I2L => IType.IntType -> IType.LongType
        case Opcodes.I2S => IType.IntType -> IType.ShortType
        case Opcodes.L2D => IType.LongType -> IType.DoubleType
        case Opcodes.L2F => IType.LongType -> IType.FloatType
        case Opcodes.L2I => IType.LongType -> IType.IntType
      }
      item.toExprNode(ctx, newType).map { case (ctx, convertedItem) =>
        ctx.stackPushed(TypedExpression(convertedItem, newType, cheapRef = false)) -> Nil
      }
    }
  }

  protected def rem(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    // Floating points have to manually call math.Mod
    opcode match {
      case Opcodes.IREM => binary(ctx, Node.Token.Rem, IType.IntType)
      case Opcodes.LREM => binary(ctx, Node.Token.Rem, IType.LongType)
      case Opcodes.DREM | Opcodes.FREM =>
        ctx.stackPopped(2, { case (ctx, Seq(val1, val2)) =>
          val1.toExprNode(ctx, IType.DoubleType).map { case (ctx, val1) =>
            val2.toExprNode(ctx, IType.DoubleType).map { case (ctx, val2) =>
              ctx.withImportAlias("math").map { case (ctx, mathAlias) =>
                val (expr, typ) = opcode match {
                  case Opcodes.DREM =>
                    mathAlias.toIdent.sel("Mod").call(Seq(val1, val2)) -> IType.DoubleType
                  case Opcodes.FREM =>
                    "float32".toIdent.call(Seq(mathAlias.toIdent.sel("Mod").call(Seq(val1, val2)))) -> IType.FloatType
                }
                ctx.stackPushed(TypedExpression(expr, typ, cheapRef = false)) -> Nil
              }
            }
          }
        })
    }
  }

  protected def sh(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    val ((typ, unsignedTo), unsignedFrom) = opcode match {
      case Opcodes.ISHL | Opcodes.ISHR | Opcodes.IUSHR => IType.IntType -> "uint32" -> "int"
      case Opcodes.LSHL | Opcodes.LSHR | Opcodes.LUSHR => IType.LongType -> "uint64" -> "int64"
    }
    @inline
    def applyFn(lhs: Node.Expression, rhs: Node.Expression) = opcode match {
      case Opcodes.ISHL | Opcodes.LSHL => lhs.binary(Node.Token.Shl, rhs)
      case Opcodes.ISHR | Opcodes.LSHR => lhs.binary(Node.Token.Shr, rhs)
      case Opcodes.IUSHR | Opcodes.LUSHR => unsignedTo.toIdent.call(Seq(lhs)).binary(Node.Token.Shr, rhs)
    }
    ctx.stackPopped(2, { case (ctx, Seq(val1, val2)) =>
      ctx.stackPushed(TypedExpression(
        expr = unsignedFrom.toIdent.call(Seq(
          applyFn(val1.expr, unsignedTo.toIdent.call(Seq(val2.expr.binary(Node.Token.And, "0x1f".toIdent))))
        )),
        typ = typ,
        cheapRef = false
      )) -> Nil
    })
  }

  protected def sub(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.Sub, opcode match {
      case Opcodes.DSUB => IType.DoubleType
      case Opcodes.FSUB => IType.FloatType
      case Opcodes.ISUB => IType.IntType
      case Opcodes.LSUB => IType.LongType
    })
  }

  protected def xor(ctx: Context, opcode: Int): (Context, Seq[Node.Statement]) = {
    binary(ctx, Node.Token.Xor, opcode match {
      case Opcodes.IXOR => IType.IntType
      case Opcodes.LXOR => IType.LongType
    })
  }
}
