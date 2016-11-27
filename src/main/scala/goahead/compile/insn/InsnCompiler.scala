package goahead.compile
package insn

import goahead.Logger
import goahead.ast.Node
import org.objectweb.asm.tree._

import scala.annotation.tailrec
import scala.util.control.NonFatal

trait InsnCompiler extends Logger with
  FieldInsnCompiler with
  IntInsnCompiler with
  JumpInsnCompiler with
  LdcInsnCompiler with
  MethodInsnCompiler with
  TypeInsnCompiler with
  VarInsnCompiler with
  ZeroOpInsnCompiler
{

  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insns: Seq[AbstractInsnNode]): (Context, Seq[Node.Statement]) =
    recursiveCompile(ctx, insns)

  @tailrec
  protected final def recursiveCompile(
    ctx: Context,
    insns: Seq[AbstractInsnNode],
    appendTo: Seq[Node.Statement] = Nil
  ): (Context, Seq[Node.Statement]) = {
    if (insns.isEmpty) ctx -> appendTo
    else {
      logger.debug(s"Compiling instruction - ${insns.head.pretty}")
      logger.trace(s"Context before ${insns.head.pretty}: ${ctx.prettyAppend}")
      val ctxAndStmts = try {
        insns.head match {
          case i: FieldInsnNode => compile(ctx, i)
          case i: InsnNode => compile(ctx, i)
          case i: IntInsnNode => compile(ctx, i)
          case i: JumpInsnNode => compile(ctx, i)
          case i: LdcInsnNode => compile(ctx, i)
          case i: MethodInsnNode => compile(ctx, i)
          case i: TypeInsnNode => compile(ctx, i)
          case i: VarInsnNode => compile(ctx, i)
          case node => sys.error(s"Unrecognized node type: $node")
        }
      } catch {
        case NonFatal(e) =>
          val insn = insns.head
          throw new Exception(
            s"Unable to compile method ${ctx.cls.name}::${ctx.method.name} insn #${insn.index} - ${insn.pretty}",
            e
          )
      }
      logger.trace(s"Context after ${insns.head.pretty}: ${ctxAndStmts._1.prettyAppend}")
      recursiveCompile(ctxAndStmts._1, insns.tail, appendTo ++ ctxAndStmts._2)
    }
  }
}

object InsnCompiler extends InsnCompiler