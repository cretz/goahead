package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode

trait JumpInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: JumpInsnNode): (Context, Seq[Node.Statement]) = {
    val label = insn.label.getLabel.uniqueStr
    ctx.copy(usedLabels = ctx.usedLabels + label).map { ctx =>

      @inline
      def ifCondStmt(ctx: Context, cond: Node.Expression) =
        ctx.prepareToGotoLabel(insn.label).map { case (ctx, stmts) =>
          ctx -> iff(None, cond, stmts :+ goto(label)).singleSeq
        }

      @inline
      def ifStmt(ctx: Context, left: Node.Expression, token: Node.Token, right: Node.Expression) =
        ctx.prepareToGotoLabel(insn.label).map { case (ctx, stmts) =>
          ctx -> iff(None, left, token, right, stmts :+ goto(label)).singleSeq
        }

      @inline
      def ifZero(ctx: Context, token: Node.Token) = ctx.stackPopped { case (ctx, item) =>
        ifStmt(ctx, item.expr, token, item.typ.zeroExpr)
      }

      @inline
      def ifInt(ctx: Context, token: Node.Token) = ctx.stackPopped(2, { case (ctx, Seq(lhs, rhs)) =>
        // If the types are the same, we don't need to do any conversion
        if (lhs.typ == rhs.typ) ifStmt(ctx, lhs.expr, token, rhs.expr) else {
          lhs.toExprNode(ctx, IType.IntType).map { case (ctx, lhs) =>
            rhs.toExprNode(ctx, IType.IntType).map { case (ctx, rhs) =>
              ifStmt(ctx, lhs, token, rhs)
            }
          }
        }
      })

      insn.byOpcode {
        case Opcodes.GOTO =>
          ctx.prepareToGotoLabel(insn.label).map { case (ctx, stmts) => ctx -> (stmts :+ goto(label)) }
        case Opcodes.IFEQ =>
          ifZero(ctx, Node.Token.Eql)
        case Opcodes.IFGE =>
          ifZero(ctx, Node.Token.Geq)
        case Opcodes.IFGT =>
          ifZero(ctx, Node.Token.Gtr)
        case Opcodes.IFLE =>
          ifZero(ctx, Node.Token.Leq)
        case Opcodes.IFLT =>
          ifZero(ctx, Node.Token.Lss)
        case Opcodes.IFNE =>
          ifZero(ctx, Node.Token.Neq)
        case Opcodes.IFNONNULL | Opcodes.IFNULL =>
          ctx.stackPopped { case (ctx, ref) =>
            val tok = if (insn.getOpcode == Opcodes.IFNONNULL) Node.Token.Neq else Node.Token.Eql
            ifStmt(ctx, ref.expr, tok, NilExpr)
          }
        case Opcodes.IF_ACMPEQ | Opcodes.IF_ACMPNE =>
          ctx.stackPopped(2, { case (ctx, Seq(lhs, rhs)) =>
            ctx.importRuntimeQualifiedName("SameIdentity").map { case (ctx, sameIdent) =>
              val cond = sameIdent.call(Seq(lhs.expr, rhs.expr))
              ifCondStmt(ctx, if (insn.getOpcode == Opcodes.IF_ACMPEQ) cond else cond.unary(Node.Token.Not))
            }
          })
        case Opcodes.IF_ICMPEQ =>
          ifInt(ctx, Node.Token.Eql)
        case Opcodes.IF_ICMPGE =>
          ifInt(ctx, Node.Token.Geq)
        case Opcodes.IF_ICMPGT =>
          ifInt(ctx, Node.Token.Gtr)
        case Opcodes.IF_ICMPLE =>
          ifInt(ctx, Node.Token.Leq)
        case Opcodes.IF_ICMPLT =>
          ifInt(ctx, Node.Token.Lss)
        case Opcodes.IF_ICMPNE =>
          ifInt(ctx, Node.Token.Neq)
        case Opcodes.JSR =>
          // TODO: confirm deprecation
          sys.error("Opcode JSR not supported")
      }
    }
  }
}
