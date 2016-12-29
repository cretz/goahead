package goahead.ast

trait NodeWalker {
  def apply[A <: Node](n: A): Option[A] = Some(n.walk(this).asInstanceOf[A])

  def applyOpt[A <: Node](n: Option[A]): Option[A] = n.flatMap(apply)

  def applyGet[A <: Node](n: A): A = apply(n).get

  def applyAll[A <: Node](n: Seq[A]): Seq[A] = n.flatMap(v => apply(v))
}

object NodeWalker {

  def selfAndChildren(n: Node): Seq[Node] = {
    var seq = Seq.empty[Node]
    new NodeWalker {
      override def apply[A <: Node](n: A) = {
        seq :+= n
        super.apply(n)
      }
    }.apply(n)
    seq
  }

  def flatMappedNonRecursive(pf: PartialFunction[Node, Option[Node]]) = new NodeWalker {
    override def apply[B <: Node](n: B) = pf.lift(n) match {
      case Some(v) => v.asInstanceOf[Option[B]]
      case _ => super.apply(n)
    }
  }

  def flatMapAllNonRecursive[A <: Node](n: Seq[A])(pf: PartialFunction[Node, Option[Node]]) =
    flatMappedNonRecursive(pf).applyAll(n)

  def mappedNonRecursive(pf: PartialFunction[Node, Node]) = new NodeWalker {
    override def apply[B <: Node](n: B) = pf.lift(n) match {
      case v: Some[_] => v.asInstanceOf[Some[B]]
      case None => super.apply(n)
    }
  }

  def mapAllNonRecursive[A <: Node](n: Seq[A])(pf: PartialFunction[Node, Node]) =
    mappedNonRecursive(pf).applyAll(n)

  def filtered(pf: PartialFunction[Node, Boolean]) = new NodeWalker {
    override def apply[B <: Node](n: B) = {
      pf.lift(n) match {
        case Some(false) => None
        case _ => super.apply(n)
      }
    }
  }

  def filterAll[A <: Node](n: Seq[A])(pf: PartialFunction[Node, Boolean]) =
    filtered(pf).applyAll(n)

  def collect[B](n: Node)(pf: PartialFunction[Node, B]): Seq[B] = {
    val lifted = pf.lift
    var found = Seq.empty[B]
    new NodeWalker {
      override def apply[A <: Node](n: A) = {
        lifted(n).foreach(found :+= _)
        super.apply(n)
      }
    }.apply(n)
    found
  }

  def collectFirst[B](n: Node)(pf: PartialFunction[Node, B]): Option[B] = {
    val lifted = pf.lift
    var found = Option.empty[B]
    new NodeWalker {
      override def apply[A <: Node](n: A) = {
        lifted(n) match {
          // Short circuit if found
          case _ if found.isDefined =>
            Some(n)
          case Some(v) =>
            found = Some(v)
            Some(n)
          case None =>
            super.apply(n)
        }
      }
    }.apply(n)
    found
  }
}
