package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode

trait LdcInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: LdcInsnNode): Seq[Node.Statement] = {
    insn.cst match {
      case s: String =>
        ctx.stack.push(
          expr = Node.CallExpression(
            function = ctx.classCtx.constructorRefExpr(
              internalClassName = "java/lang/String",
              desc = Type.getMethodType(StringType, StringType)
            ),
            args = Seq(s.toLit)
          ),
          typ = StringType
        )
        Nil
      case cst =>
        sys.error(s"Unrecognized LDC type: $cst")
    }
  }
}
