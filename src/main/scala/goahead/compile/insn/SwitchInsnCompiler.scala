package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.tree.{LabelNode, LookupSwitchInsnNode, TableSwitchInsnNode}

trait SwitchInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: LookupSwitchInsnNode): (Context, Seq[Node.Statement]) = {
    compileSwitch(
      ctx = ctx,
      labels = {
        import scala.collection.JavaConverters._
        insn.keys.asScala.asInstanceOf[Seq[Integer]].map(_.intValue()).zip(
          insn.labels.asScala.asInstanceOf[Seq[LabelNode]]
        )
      },
      default = insn.dflt
    )
  }

  def compile(ctx: Context, insn: TableSwitchInsnNode): (Context, Seq[Node.Statement]) = {
    compileSwitch(
      ctx = ctx,
      labels = {
        import scala.collection.JavaConverters._
        insn.labels.asScala.asInstanceOf[Seq[LabelNode]].zipWithIndex.map(v => (v._2 + insn.min) -> v._1)
      },
      default = insn.dflt
    )
  }

  protected def compileSwitch(
    ctx: Context,
    labels: Seq[(Int, LabelNode)],
    default: LabelNode
  ): (Context, Seq[Node.Statement]) = {
    ctx.stackPopped { case (ctx, index) =>

      val ctxAndCases = labels.foldLeft(ctx -> Seq.empty[(Node.Expression, Seq[Node.Statement])]) {
        case ((ctx, cases), (index, label)) =>
          val labelStr = label.getLabel.toString
          ctx.copy(usedLabels = ctx.usedLabels + labelStr).prepareToGotoLabel(label).map { case (ctx, stmts) =>
            ctx -> (cases :+ (index.toLit -> (stmts :+ goto(labelStr))))
          }
      }

      ctxAndCases.map { case (ctx, cases) =>
        val labelStr = default.getLabel.toString
        ctx.copy(usedLabels = ctx.usedLabels + labelStr).prepareToGotoLabel(default).map { case (ctx, stmts) =>
          ctx -> index.expr.switchOn(cases, Some(stmts :+ goto(labelStr))).singleSeq
        }
      }
    }
  }
}
