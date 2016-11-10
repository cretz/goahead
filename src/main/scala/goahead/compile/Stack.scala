package goahead.compile

case class Stack(items: Seq[TypedExpression]) {
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
}

object Stack {
  val empty = Stack(Nil)
}