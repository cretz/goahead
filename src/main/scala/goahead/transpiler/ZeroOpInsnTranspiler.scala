package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.{InsnNode, TypeInsnNode}
import org.objectweb.asm.{Opcodes, Type}

trait ZeroOpInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: InsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.DUP =>
        // If the last instruction was "NEW" then we ignore
        ctx.previousInstructions.lastOption match {
          case Some(i: TypeInsnNode) if i.getOpcode == Opcodes.NEW => Nil
          // TODO: handle the rest
          case i => sys.error(s"Unknown insn: $i")
        }
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
        Seq(Node.ReturnStatement(Seq.empty))
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }

  private[this] def iconst(ctx: MethodTranspiler.Context, i: Int): Seq[Node.Statement] = {
    ctx.stack.push(i.toLit, Type.INT_TYPE)
    Nil
  }
}
