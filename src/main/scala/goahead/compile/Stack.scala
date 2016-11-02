package goahead.compile

import goahead.ast.Node
import org.objectweb.asm.Type

case class Stack(
  items: Seq[TypedExpression],
  tempVars: Seq[Stack.TempVar]
) {
  def push(item: TypedExpression) = copy(items = items :+ item)

  def pop() = {
    require(items.nonEmpty, "Trying to pop from empty stack")
    copy(items = items.init) -> items.last
  }

  def pop(amount: Int) = {
    val (leftover, popped) = items.splitAt(items.length - amount)
    require(popped.size == amount, s"Not enough on stack, expected $amount, got ${popped.size}")
    copy(items = leftover) -> popped
  }

  def tempVar(typ: Type) = {
    // Try to find one not in use, otherwise create
    val existingTempVar = tempVars.collectFirst {
      case t if t.typ == typ && !items.contains(t.toTypedExpr) => t
    }
    existingTempVar match {
      case Some(temp) =>
        this -> temp
      case None =>
        val temp = Stack.TempVar("temp" + tempVars.size, typ)
        copy(tempVars = tempVars :+ temp) -> temp
    }
  }
}

object Stack {
  val empty = Stack(Nil, Nil)

  case class TempVar(name: String, typ: Type) {
    def toTypedExpr = TypedExpression(Node.Identifier(name), typ, cheapRef = true)
  }
}