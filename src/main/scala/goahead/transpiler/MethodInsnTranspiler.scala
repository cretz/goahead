package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.{Opcodes, Type}

trait MethodInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: MethodInsnNode): Seq[Node.Statement] = {
    insn.getOpcode match {
      case Opcodes.INVOKESPECIAL if insn.name == "<init>" && ctx.methodNode.name == "<init>" =>
        require(Type.getReturnType(insn.desc) == Type.VOID_TYPE)
        // If we're inside of init ourselves and this call is init, we know we're actually running
        // init on ourselves
        val subject = ctx.stack.pop()
        Seq(Node.ExpressionStatement(
          Node.CallExpression(
            Node.SelectorExpression(
              Node.SelectorExpression(
                subject.expr,
                goInstanceObjectName(insn.owner).toIdent
              ),
              goMethodName(insn.name, insn.desc).toIdent
            )
          )
        ))
      case Opcodes.INVOKEVIRTUAL | Opcodes.INVOKESPECIAL =>
        normalFuncCall(ctx, insn)
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }

  private[this] def normalFuncCall(ctx: MethodTranspiler.Context, insn: MethodInsnNode): Seq[Node.Statement] = {
    val callDesc = Type.getMethodType(insn.desc)
    val params = ctx.stack.pop(callDesc.getArgumentTypes.size)
    val subject = ctx.stack.pop()

    // Null pointer check for the subject
    // TODO: actually, none of this, we'll let go panic
    // since we can't easily inline nil check
    // val nullCheckStmt = ctx.nullPointerAssertion(subject.expr)

    // Create the call
    val callExpr = Node.CallExpression(
      Node.SelectorExpression(
        subject.expr,
        goMethodName(insn.name, insn.desc).toIdent
      ),
      // TODO: exprToType for each param type
      params.map(_.expr)
    )

    // Just put on the stack if it's not void
    if (callDesc.getReturnType == Type.VOID_TYPE) Seq(Node.ExpressionStatement(callExpr))
    else {
      ctx.stack.push(callExpr, callDesc.getReturnType, cheapRef = false)
      Nil
    }
  }
}
