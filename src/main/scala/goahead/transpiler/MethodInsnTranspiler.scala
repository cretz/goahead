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
        val nullCheckStmt = ctx.nullPointerAssertion(subject.expr)

        // Create the call
        val callExpr = Node.CallExpression(
          Node.SelectorExpression(
            subject.expr,
            goMethodName(insn.name, insn.desc).toIdent
          ),
          // TODO: exprToType for each param type
          params.map(_.expr)
        )

        // Put the result on the stack if not void, otherwise just run
        val execStmt =
          if (callDesc.getReturnType != Type.VOID_TYPE) {
            val tempVar = ctx.stack.newTempVarName.toIdent
            ctx.stack.push(tempVar, callDesc.getReturnType)
            Node.AssignStatement(
              left = Seq(tempVar),
              token = Node.Token.Define,
              right = Seq(callExpr)
            )
          } else {
            Node.ExpressionStatement(callExpr)
          }

        Seq(nullCheckStmt, execStmt)
      case code =>
        sys.error(s"Unrecognized opcode: $code")
    }
  }
}
