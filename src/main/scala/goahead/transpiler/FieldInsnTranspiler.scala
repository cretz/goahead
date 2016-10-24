package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.{Opcodes, Type}

trait FieldInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: FieldInsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.GETSTATIC =>
        ctx.stack.push(
          expr = Node.SelectorExpression(
            ctx.classCtx.staticClassRefExpr(insn.owner),
            goFieldName(insn.owner, insn.name).toIdent
          ),
          typ = Type.getType(insn.desc)
        )
        Nil
      case Opcodes.PUTSTATIC =>
        Seq(
          Node.AssignStatement(
            left = Seq(
              Node.SelectorExpression(
                ctx.classCtx.staticClassRefExpr(insn.owner),
                goFieldName(insn.owner, insn.name).toIdent
              )
            ),
            token = Node.Token.Assign,
            right = Seq {
              val right = ctx.stack.pop()
              ctx.exprToType(right.expr, right.typ, Type.getType(insn.desc))
            }
          )
        )
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }
}
