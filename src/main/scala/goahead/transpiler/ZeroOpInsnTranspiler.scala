package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.{Opcodes, Type}

trait ZeroOpInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: InsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.ACONST_NULL =>
        ctx.stack.push("nil".toIdent, Type.VOID_TYPE, cheapRef = true)
        Nil
      case Opcodes.ARETURN =>
        val subject = ctx.stack.pop()
        Seq(Node.ReturnStatement(Seq(
          ctx.exprToType(subject.expr, subject.typ, ctx.methodType.getReturnType)
        )))
      case Opcodes.DUP =>
        // We only dupe things that are not cheap references, otherwise we make a temp var
        val (entry, stmts) = ctx.stack.pop() match {
          case e if !e.cheapRef =>
            val tempVar = ctx.stack.newTempVar(e.typ)
            tempVar -> Seq(
              Node.AssignStatement(
                left = Seq(tempVar.expr),
                token = Node.Token.Assign,
                right = Seq(e.expr)
              )
            )
          case e =>
            e -> Nil
        }
        // Now push it twice
        ctx.stack.push(entry)
        ctx.stack.push(entry)
        stmts
      case Opcodes.IADD =>
        val Seq(left, right) = ctx.stack.pop(2)
        ctx.stack.push(
          Node.BinaryExpression(
            left = left.expr,
            operator = Node.Token.Add,
            right = right.expr
          ),
          // TODO: properly determine combined type
          Type.INT_TYPE,
          cheapRef = false
        )
        Nil
      case Opcodes.ICONST_0 =>
        iconst(ctx, 0)
      case Opcodes.ICONST_1 =>
        iconst(ctx, 1)
      case Opcodes.ICONST_2 =>
        iconst(ctx, 2)
      case Opcodes.ICONST_3 =>
        iconst(ctx, 3)
      case Opcodes.ICONST_4 =>
        iconst(ctx, 4)
      case Opcodes.ICONST_5 =>
        iconst(ctx, 5)
      case Opcodes.ICONST_M1 =>
        iconst(ctx, -1)
      case Opcodes.POP =>
        // We need to just take what is on the stack and make it a statement as this
        // is often just an ignored return value or something
        Seq(Node.ExpressionStatement(ctx.stack.pop.expr))
      case Opcodes.RETURN =>
        Seq(Node.ReturnStatement(Nil))
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }

  private[this] def iconst(ctx: MethodTranspiler.Context, i: Int): Seq[Node.Statement] = {
    ctx.stack.push(i.toLit, Type.INT_TYPE, cheapRef = true)
    Nil
  }
}
