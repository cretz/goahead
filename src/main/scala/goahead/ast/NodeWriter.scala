package goahead.ast

import java.lang

import Node._

object NodeWriter {
  def fromNode(node: Node): String = new NodeWriter().appendNode(node).toString()
}

class NodeWriter {

  val builder = new StringBuilder()
  var indention = 0

  def append(c: Char): this.type = {
    builder.append(c)
    this
  }

  def append(s: String): this.type = {
    builder.append(s)
    this
  }
  
  def indent(): this.type = {
    indention += 1
    this
  }

  def dedent(): this.type = {
    indention -= 1
    this
  }

  override def toString: String = builder.toString()

  def __TODO__ : this.type = ???

  def newline(): this.type = append('\n').append("\t" * indention)

  def commaSeparated[T <: Node](seq: Seq[T], f: T => this.type): this.type = {
    seq.zipWithIndex.foreach { case (node, idx) =>
      if (idx > 0) append(", ")
      f(node)
    }
    this
  }

  def appendArrayType(expr: ArrayType): this.type = {
    append('[')
    expr.length.foreach(appendExpression)
    append(']').appendExpression(expr.typ)
  }

  def appendAssignStatement(stmt: AssignStatement): this.type = {
    commaSeparated(stmt.left, appendExpression)
    stmt.token match {
      case Token.Assign => append(" = ")
      case Token.Define => append(" := ")
      case _ => sys.error("Unrecognized token: " + stmt.token)
    }
    commaSeparated(stmt.right, appendExpression)
  }

  def appendBasicLiteral(lit: BasicLiteral): this.type = append(lit.value)

  def appendBinaryExpression(expr: BinaryExpression): this.type = {
    require(expr.operator.string != null)
    appendExpression(expr.left).append(' ').append(expr.operator.string.get).append(' ')
    appendExpression(expr.right)
  }

  def appendBlockStatement(stmt: BlockStatement): this.type = {
    if (stmt.statements.isEmpty) append("{ }")
    else {
      append('{').indent()
      stmt.statements.foreach { newline().appendStatement(_) }
      dedent().newline().append('}')
    }
  }

  def appendBranchStatement(stmt: BranchStatement): this.type = {
    val str = stmt.token.string.getOrElse(sys.error("Token required"))
    append(str)
    stmt.label.foreach(append(' ').appendIdentifier(_))
    this
  }

  def appendCallExpression(expr: CallExpression): this.type = {
    appendExpression(expr.function).append('(').
      commaSeparated(expr.args, appendExpression).append(')')
  }

  def appendCaseClause(stmt: CaseClause): this.type = __TODO__

  def appendChannelType(expr: ChannelType): this.type = __TODO__

  def appendCommClause(stmt: CommClause): this.type = __TODO__

  def appendComment(node: Comment): this.type = __TODO__

  def appendCompositeLiteral(expr: CompositeLiteral): this.type = __TODO__

  def appendDeclaration(decl: Declaration): this.type = decl match {
    case f: FunctionDeclaration => appendFunctionDeclaration(f)
    case g: GenericDeclaration => appendGenericDeclaration(g)
  }

  def appendDeclarationStatement(stmt: DeclarationStatement): this.type = __TODO__

  def appendDeferStatement(stmt: DeferStatement): this.type = __TODO__

  def appendEllipsis(expr: Ellipsis): this.type = __TODO__

  def appendEmptyStatement(): this.type = __TODO__

  def appendExpression(expr: Expression): this.type = expr match {
    case a: ArrayType => appendArrayType(a)
    case b: BasicLiteral => appendBasicLiteral(b)
    case b: BinaryExpression => appendBinaryExpression(b)
    case c: CallExpression => appendCallExpression(c)
    case c: ChannelType => appendChannelType(c)
    case c: CompositeLiteral => appendCompositeLiteral(c)
    case e: Ellipsis => appendEllipsis(e)
    case f: FunctionLiteral => appendFunctionLiteral(f)
    case f: FunctionType => appendFunctionType(f)
    case i: Identifier => appendIdentifier(i)
    case i: IndexExpression => appendIndexExpression(i)
    case i: InterfaceType => appendInterfaceType(i)
    case k: KeyValueExpression => appendKeyValueExpression(k)
    case m: MapType => appendMapType(m)
    case p: ParenthesizedExpression => appendParenthesizedExpression(p)
    case s: SelectorExpression => appendSelectorExpression(s)
    case s: SliceExpression => appendSliceExpression(s)
    case s: StarExpression => appendStarExpression(s)
    case s: StructType => appendStructType(s)
    case t: TypeAssertExpression => appendTypeAssertExpression(t)
    case u: UnaryExpression => appendUnaryExpression(u)
  }

  def appendExpressionStatement(stmt: ExpressionStatement): this.type = appendExpression(stmt.expression)

  def appendField(field: Field, padNameTo: Option[Int] = None): this.type = {
    val namesLength = field.names.map(_.name.length).sum
    commaSeparated(field.names, appendIdentifier)
    if (field.names.nonEmpty) {
      padNameTo.foreach { v => append(" " * (v - (namesLength + ((field.names.length - 1) * 2)))) }
      append(" ")
    }
    field.tag.foreach { append(' ').appendBasicLiteral(_) }
    this
  }

  def appendFile(file: File): this.type = {
    append("package ").appendIdentifier(file.packageName).newline()
    file.declarations.foreach { newline().appendDeclaration(_).newline() }
    this
  }

  def appendForStatement(stmt: ForStatement): this.type = __TODO__

  def appendFunctionDeclaration(decl: FunctionDeclaration): this.type = {
    append("func ").appendParameters(decl.receivers)
    if (decl.receivers.nonEmpty) append(' ')
    appendIdentifier(decl.name).appendParameters(decl.typ.parameters, "()")
    if (decl.typ.results.nonEmpty) append(' ')
    if (decl.typ.results.length == 1) appendField(decl.typ.results.head)
    else appendParameters(decl.typ.results)
    decl.body.foreach { append(' ').appendBlockStatement(_) }
    this
  }

  def appendFunctionLiteral(expr: FunctionLiteral): this.type = __TODO__

  def appendFunctionType(expr: FunctionType): this.type = __TODO__

  def appendGenericDeclaration(decl: GenericDeclaration): this.type = {
    require(decl.specifications.nonEmpty)
    decl.token match {
      case Token.Import => append("import ")
      case Token.Const => append("const ")
      case Token.Type => append("type ")
      case Token.Var => append("var ")
      case _ => sys.error("Unrecognized token: " + decl.token)
    }
    val multiSpec = decl.specifications.length > 1
    if (multiSpec) append('(').indent()
    decl.specifications.foreach {
      if (multiSpec) newline()
      appendSpecification
    }
    if (multiSpec) dedent().newline().append(')')
    this
  }

  def appendGoStatement(stmt: GoStatement): this.type = __TODO__

  def appendIdentifier(id: Identifier): this.type = append(id.name)

  def appendIfStatement(stmt: IfStatement): this.type = {
    append("if ")
    stmt.init.foreach { appendStatement(_).append("; ") }
    appendExpression(stmt.condition).append(' ')
    appendBlockStatement(stmt.body)
    stmt.elseStatement.foreach { append(" else ").appendStatement(_) }
    this
  }

  def appendImportSpecification(spec: ImportSpecification): this.type = {
    spec.name.foreach { appendIdentifier(_).append(' ') }
    appendBasicLiteral(spec.path)
  }

  def appendIncrementDecrementStatement(stmt: IncrementDecrementStatement): this.type = __TODO__

  def appendIndexExpression(expr: IndexExpression): this.type = __TODO__

  def appendInterfaceType(expr: InterfaceType): this.type = __TODO__

  def appendKeyValueExpression(expr: KeyValueExpression): this.type = __TODO__

  def appendLabeledStatement(stmt: LabeledStatement): this.type = {
    // Remove all characters back to last newline
    builder.delete(builder.lastIndexOf("\n") + 1, builder.length)
    appendIdentifier(stmt.label).append(':')
    stmt.statement.foreach { newline().appendStatement(_) }
    this
  }

  def appendMapType(expr: MapType): this.type = __TODO__

  def appendNode(node: Node): this.type = node match {
    case c: Comment => appendComment(c)
    case d: Declaration => appendDeclaration(d)
    case e: Expression => appendExpression(e)
    case f: Field => appendField(f)
    case f: File => appendFile(f)
    case p: Package => appendPackage(p)
    case s: Specification => appendSpecification(s)
    case s: Statement => appendStatement(s)
  }

  def appendPackage(node: Package): this.type = __TODO__

  def appendParameters(params: Seq[Field], appendOnEmpty: String = ""): this.type = {
    if (params.isEmpty) append(appendOnEmpty)
    else append('(').commaSeparated(params, appendField(_: Field, None)).append(')')
  }

  def appendParenthesizedExpression(expr: ParenthesizedExpression): this.type = __TODO__

  def appendRangeStatement(stmt: RangeStatement): this.type = __TODO__

  def appendReturnStatement(stmt: ReturnStatement): this.type = {
    append("return")
    if (stmt.results.nonEmpty) append(' ').commaSeparated(stmt.results, appendExpression)
    this
  }

  def appendSelectorExpression(expr: SelectorExpression): this.type = {
    appendExpression(expr.expression).append('.').appendIdentifier(expr.selector)
  }

  def appendSelectStatement(stmt: SelectStatement): this.type = __TODO__

  def appendSendStatement(stmt: SendStatement): this.type = __TODO__

  def appendSliceExpression(expr: SliceExpression): this.type = __TODO__

  def appendSpecification(spec: Specification): this.type = spec match {
    case i: ImportSpecification => appendImportSpecification(i)
    case v: ValueSpecification => appendValueSpecification(v)
    case t: TypeSpecification => appendTypeSpecification(t)
  }

  def appendStarExpression(expr: StarExpression): this.type = {
    append('*').appendExpression(expr.expression)
  }

  def appendStatement(stmt: Statement): this.type = stmt match {
    case a: AssignStatement => appendAssignStatement(a)
    case b: BlockStatement => appendBlockStatement(b)
    case b: BranchStatement => appendBranchStatement(b)
    case c: CaseClause => appendCaseClause(c)
    case c: CommClause => appendCommClause(c)
    case d: DeclarationStatement => appendDeclarationStatement(d)
    case d: DeferStatement => appendDeferStatement(d)
    case EmptyStatement => appendEmptyStatement()
    case e: ExpressionStatement => appendExpressionStatement(e)
    case f: ForStatement => appendForStatement(f)
    case g: GoStatement => appendGoStatement(g)
    case i: IfStatement => appendIfStatement(i)
    case i: IncrementDecrementStatement => appendIncrementDecrementStatement(i)
    case l: LabeledStatement => appendLabeledStatement(l)
    case r: RangeStatement => appendRangeStatement(r)
    case r: ReturnStatement => appendReturnStatement(r)
    case s: SelectStatement => appendSelectStatement(s)
    case s: SendStatement => appendSendStatement(s)
    case s: SwitchStatement => appendSwitchStatement(s)
  }

  def appendStructType(expr: StructType): this.type = {
    if (expr.fields.isEmpty) append("struct{}")
    else {
      append("struct {").indent()
      val padTo = expr.fields.map({ f =>
        if (f.names.isEmpty) 0
        else f.names.map(_.name.length).sum + (2 * (f.names.length - 1))
      }).max
      expr.fields.foreach { f => newline().appendField(f, Some(padTo)) }
      dedent()
      if (expr.fields.isEmpty) append(' ') else newline()
      append('}')
    }
  }

  def appendSwitchStatement(stmt: SwitchStatement): this.type = __TODO__

  def appendTypeAssertExpression(expr: TypeAssertExpression): this.type = __TODO__

  def appendTypeSpecification(spec: TypeSpecification): this.type = {
    appendIdentifier(spec.name).append(' ').appendExpression(spec.typ)
  }

  def appendUnaryExpression(expr: UnaryExpression): this.type = expr.operator.string match {
    case None => sys.error("No string for token")
    case Some(str) => append(str).appendExpression(expr.operand)
  }

  def appendValueSpecification(spec: ValueSpecification): this.type = {
    commaSeparated(spec.names, appendIdentifier)
    spec.typ.foreach { append(' ').appendExpression(_) }
    if (spec.values.nonEmpty) append(" = ").commaSeparated(spec.values, appendIdentifier)
    this
  }
}
