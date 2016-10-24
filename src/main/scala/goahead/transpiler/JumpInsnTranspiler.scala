package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.{Opcodes, Type}
import org.objectweb.asm.tree.JumpInsnNode

trait JumpInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: JumpInsnNode): Seq[Node.Statement] = {
    val label = insn.label.getLabel.toString
    insn.getOpcode match {
      case Opcodes.GOTO =>
        ctx.usedLabels += label
        Seq(Node.BranchStatement(Node.Token.Goto, Some(label.toIdent)))
      case Opcodes.IFEQ =>
        val left = ctx.stack.pop
        ctx.usedLabels += label
        Seq(
          Node.IfStatement(
            condition = Node.BinaryExpression(
              left = left.expr,
              operator = Node.Token.Eql,
              right = ctx.zeroOfType(left.typ)
            ),
            body = Node.BlockStatement(
              Seq(Node.BranchStatement(Node.Token.Goto, Some(label.toIdent)))
            )
          )
        )
      case code => sys.error(s"Unrecognized code: $code")
    }
  }
}
