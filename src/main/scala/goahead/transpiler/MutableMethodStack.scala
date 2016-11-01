package goahead.transpiler

import goahead.ast.Node
import org.objectweb.asm.Type
import Helpers._

class MutableMethodStack {
  private[this] var stack = Seq.empty[MutableMethodStack.Entry]

  private[this] var tempVarCounter = 0

  private[this] var tempVariables = Map.empty[Type, Seq[MutableMethodStack.Entry.TempVarAssignEntry]]

  def push(expr: Node.Expression, typ: Type, cheapRef: Boolean): Unit =
    push(MutableMethodStack.Entry(expr, typ, cheapRef))

  def push(entry: MutableMethodStack.Entry): Unit = {
    if (entry.isInstanceOf[MutableMethodStack.Entry.TempVarAssignEntry]) {
      entry.asInstanceOf[MutableMethodStack.Entry.TempVarAssignEntry].inUseCount += 1
    }
    stack :+= entry
  }

  def pop(): MutableMethodStack.Entry =
    pop(1).head

  def pop(amount: Int): Seq[MutableMethodStack.Entry] = {
    val (leftover, popped) = stack.splitAt(stack.length - amount)
    require(popped.size == amount, s"Not enough on stack, expected $amount, got ${popped.size}")
    stack = leftover
    // If there are any temp variables, we have to mark them unused again
    popped.collect {
      case t: MutableMethodStack.Entry.TempVarAssignEntry => t.inUseCount -= 1
    }
    popped
  }

  def newTempVar(typ: Type): MutableMethodStack.Entry.TempVarAssignEntry = {
    // Try to find an unused one of the type, otherwise create anew
    val possibles = tempVariables.getOrElse(typ, Nil)
    val ret = possibles.find(!_.inUse).getOrElse {
      tempVarCounter += 1
      val tmpVar = new MutableMethodStack.Entry.TempVarAssignEntry(s"temp$tempVarCounter".toIdent, typ)
      tempVariables += typ -> (possibles :+ tmpVar)
      tmpVar
    }
    ret
  }

  def pushTempVar(typ: Type): Node.Identifier = {
    val tempVar = newTempVar(typ)
    push(tempVar)
    tempVar.expr
  }

  def tempVars = tempVariables
}

object MutableMethodStack {
  trait Entry {
    def expr: Node.Expression
    def typ: Type
    def cheapRef: Boolean
  }

  object Entry {

    def apply(expr: Node.Expression, typ: Type, cheapRef: Boolean): Entry =
      SimpleExprEntry(expr, typ, cheapRef)

    case class SimpleExprEntry(
      expr: Node.Expression,
      typ: Type,
      cheapRef: Boolean
    ) extends Entry

    class TempVarAssignEntry(
      val expr: Node.Identifier,
      val typ: Type
    ) extends Entry {
      @volatile
      var inUseCount = 0

      def inUse = inUseCount > 0

      override def cheapRef = true
    }
  }
}
