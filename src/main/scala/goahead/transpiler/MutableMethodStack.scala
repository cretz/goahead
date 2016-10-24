package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.Type

class MutableMethodStack {
  @volatile
  private[this] var stack = Seq.empty[MutableMethodStack.Entry]

  @volatile
  private[this] var tempVarCounter = 0

  def push(expr: Node.Expression, typ: Type): Unit =
    push(MutableMethodStack.Entry(expr, typ))

  def push(entry: MutableMethodStack.Entry): Unit = synchronized {
    stack :+= entry
  }

  def pop(): MutableMethodStack.Entry =
    pop(1).head

  def pop(amount: Int): Seq[MutableMethodStack.Entry] = synchronized {
    val (leftover, popped) = stack.splitAt(stack.length - amount)
    require(popped.size == amount, s"Not enough on stack, expected $amount, got ${popped.size}")
    stack = leftover
    popped
  }

  def newTempVarName(): String = synchronized {
    tempVarCounter += 1
    s"temp$tempVarCounter"
  }
}

object MutableMethodStack {
  case class Entry(
    expr: Node.Expression,
    typ: Type
  )
}
