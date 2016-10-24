package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.{Opcodes, Type}

trait MethodInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: MethodInsnNode): Seq[Node.Statement] = {
    val callDesc = Type.getMethodType(insn.desc)
    val params = ctx.stack.pop(callDesc.getArgumentTypes.size)

    insn.getOpcode match {
      case Opcodes.INVOKEVIRTUAL =>
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
          ctx.stack.push(callExpr, callDesc.getReturnType)
          Nil
        }
          /*
        // Put the result on the stack if not void, otherwise just run
        val execStmt =
          if (callDesc.getReturnType != Type.VOID_TYPE) {
            /*Node.AssignStatement(
              left = Seq(ctx.stack.useTempVar(callDesc.getReturnType)),
              token = Node.Token.Assign,
              right = Seq(callExpr)
            )*/
            ctx.stack.push(callExpr, callDesc.getReturnType)
          } else {
            Node.ExpressionStatement(callExpr)
          }

        Seq(nullCheckStmt, execStmt)
        */
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }
}
