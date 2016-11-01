package goahead.transpiler

import goahead.ast.Node
import goahead.transpiler.Helpers._
import org.objectweb.asm.tree.LdcInsnNode

trait LdcInsnTranspiler {

  def transpile(ctx: MethodTranspiler.Context, insn: LdcInsnNode): Seq[Node.Statement] = {
    insn.cst match {
      case s: String =>
        val internalHelpersAlias = ctx.classCtx.transpileCtx.imports.loadImportAlias("rt").toIdent
        ctx.stack.push(
          expr = Node.CallExpression(
            function = Node.SelectorExpression(
              internalHelpersAlias,
              "NewString".toIdent
            ),
            args = Seq(s.toLit)
          ),
          typ = StringType,
          cheapRef = true
        )
        Nil
      case cst =>
        sys.error(s"Unrecognized LDC type: $cst")
    }
  }
}
