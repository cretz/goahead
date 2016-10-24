package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.{Opcodes, Type}

trait ZeroOpInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: InsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
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
