package goahead.compile

case class LocalVars(
  thisVar: Option[TypedExpression],
  vars: collection.SortedMap[Int, TypedExpression] = collection.SortedMap.empty,
  removedVars: Seq[TypedExpression] = Seq.empty,
  nameCounter: Int = 0
) {
  import Helpers._

  def appendLocalVar(ctx: Contextual[_], typ: IType): (LocalVars, TypedExpression) = {
    // Find the max index and add one past it (or two past if it's a double or long)
    val index = if (vars.isEmpty) thisVar.size else {
      vars.last match {
        case (index, t) if t.typ == IType.LongType || t.typ == IType.DoubleType => index + 2
        case (index, _) => index + 1
      }
    }
    getLocalVar(ctx, index, typ, forWriting = false)
  }

  @inline def size = vars.size

  def take(amount: Int): LocalVars = {
    val (savedVars, lostVars) = vars.splitAt(amount)
    copy(vars = savedVars, removedVars = removedVars ++ lostVars.values)
  }

  def dropRight(amount: Int): LocalVars = take(size - amount)

  def drop(amount: Int): LocalVars = {
    val (lostVars, savedVars) = vars.splitAt(amount)
    copy(vars = savedVars, removedVars = removedVars ++ lostVars.values)
  }

  def allTimeVars = vars.values ++ removedVars

  def getLocalVar(
    ctx: Contextual[_],
    index: Int,
    typ: IType,
    forWriting: Boolean
  ): (LocalVars, TypedExpression) = {
    if (index == 0 && thisVar.isDefined) this -> thisVar.get else {
      vars.get(index) match {
        case None =>
          // Need to create a new local var
          addOrReplaceLocalVar(index, typ)
        case Some(existing) =>
          // If the existing and current types are objects, but the existing is not assignable from
          // the new type, we need to replace the local var to a new thing
          if (forWriting && shouldReplaceExistingVar(ctx, existing.typ, typ)) {
            addOrReplaceLocalVar(index, typ)
          } else this -> existing
      }
    }
  }

  private[this] def addOrReplaceLocalVar(index: Int, typ: IType): (LocalVars, TypedExpression) = {
    nextUnusedLocalVarName().map { case (localVars, name) =>
      val localVar = TypedExpression.namedVar(name, typ)
      @inline
      def withUpdatedVar = localVars.copy(vars = vars + (index -> localVar))
      localVars.vars.get(index) match {
        case None => withUpdatedVar -> localVar
        case Some(existing) => withUpdatedVar.copy(removedVars = withUpdatedVar.removedVars :+ existing) -> localVar
      }
    }
  }

  private[this] def nextUnusedLocalVarName() =
    copy(nameCounter = nameCounter + 1) -> s"var$nameCounter"

  private[this] def shouldReplaceExistingVar(ctx: Contextual[_], existing: IType, updated: IType) = {
    existing -> updated match {
      case (e: IType.Simple, u: IType.Simple) =>
        (e.isRef || u.isRef) && !e.isAssignableFrom(ctx.imports.classPath, u)
      case _ =>
        false
    }
  }

  def prettyLines: Seq[String] = "Locals: " +: vars.values.map("  " + _.pretty).toSeq
}
