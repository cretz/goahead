package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.{Opcodes, Type}

trait VarInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: VarInsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.ALOAD =>
        val localVar = ctx.localVariable(insn.`var`, Type.getDescriptor(classOf[Object]))
        // Just push the ref on the stack
        ctx.stack.push(localVar.name.toIdent, Type.getType(localVar.desc), cheapRef = true)
        Nil
      case Opcodes.ASTORE =>
        val subject = ctx.stack.pop()
        val localVar = ctx.localVariable(insn.`var`, subject.typ.getDescriptor)
        Seq(
          Node.AssignStatement(
            left = Seq(goVarName(localVar.name).toIdent),
            token = Node.Token.Assign,
            right = Seq(ctx.exprToType(subject.expr, subject.typ, Type.getType(localVar.desc)))
          )
        )
      case code =>
        sys.error(s"Unrecognized code: $code")
    }
  }
}
