package goahead.ast

import goahead.ast.Node._

object NodeWriter {
  def fromNode(node: Node): String = {
    val fullBuilder = new java.lang.StringBuilder()
    fromNode(node, fullBuilder)
    fullBuilder.toString
  }

  def fromNode(node: Node, appendable: Appendable): Unit =
    new NodeWriter(new BufferedBuilder(appendable)).appendNode(node).builder.flushAll()

  private[ast] def sortableFieldTuple(f: Field) = f match {
    case Field(Nil, typ, _) => 1 -> fromNode(typ)
    case Field(ids, _: FunctionType, _) => 3 -> ids.head.name
    case Field(ids, _, _) => 2 -> ids.head.name
  }

  // We hold two newlines at a time
  private[ast] class BufferedBuilder(val appendable: Appendable) {
    val builder = new StringBuilder

    def append(c: Char): Unit = {
      builder.append(c)
      flushAllButLastTwoNewlines()
    }

    def append(s: String): Unit = {
      builder.append(s)
      flushAllButLastTwoNewlines()
    }

    def length = builder.length

    def deleteCharAt(i: Int): Unit = {
      builder.deleteCharAt(i)
      ()
    }

    def lastIndexOf(str: String) = builder.lastIndexOf(str)

    def indexWhere(p: Char => Boolean, from: Int) = builder.indexWhere(p, from)

    def flushAllButLastTwoNewlines(): Unit = {
      builder.lastIndexOf('\n') match {
        case -1 => ()
        case index => builder.lastIndexOf('\n', index - 1) match {
          case -1 => ()
          case index =>
            appendable.append(builder, 0, index)
            builder.delete(0, index)
            ()
        }
      }
    }

    def flushAll(): Unit = {
      appendable.append(builder)
      builder.clear()
    }
  }
}

class NodeWriter(val builder: NodeWriter.BufferedBuilder) {
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

  def removeLastIndent(): this.type = {
    // We need to remove one level of indention which is really just the last \t char
    builder.deleteCharAt(builder.length - 1)
    this
  }

  def __TODO__ : this.type = ???

  def newline(): this.type = append('\n').append("\t" * indention)

  def commaSeparated[T <: Node](seq: Seq[T], f: T => this.type): this.type = {
    seq.zipWithIndex.foreach { case (node, idx) =>
      if (idx > 0) append(", ")
      f(node)
    }
    this
  }

  case class PaddedNodeSection[N <: Node](leftMax: Option[Int], nodes: Seq[N])

  def paddedLeftLength(node: Node): Option[Int] = node match {
    case Node.Field(names, _, _) =>
      // Add 2 for each comma+space in between
      Some(names.map(_.name.length).sum + ((names.size - 1) * 2))
    case Node.KeyValueExpression(Node.Identifier(ident), _) =>
      // Add 1 for the colon
      Some(ident.length + 1)
    case Node.ValueSpecification(names, _, _) =>
      // Add 2 for each comma+space in between, but take one away for the space that's already there
      Some(names.map(_.name.length).sum + ((names.size - 1) * 2) - 1)
    case _ =>
      None
  }

  def paddedSections[N <: Node](nodes: Seq[N]): Seq[PaddedNodeSection[N]] = {
    nodes.foldLeft(Seq.empty[PaddedNodeSection[N]]) { case (sections, node) =>
      paddedLeftLength(node) match {
        case Some(leftLength) => sections.lastOption match {
          case Some(PaddedNodeSection(Some(leftMax), nodes)) =>
            sections.dropRight(1) :+ PaddedNodeSection(Some(leftLength.max(leftMax)), nodes :+ node)
          case _ =>
            sections :+ PaddedNodeSection(Some(leftLength), nodes = Seq(node))
        }
        case None => sections.lastOption match {
          case Some(PaddedNodeSection(None, nodes)) =>
            sections.dropRight(1) :+ PaddedNodeSection(None, nodes :+ node)
          case _ =>
            sections :+ PaddedNodeSection(None, nodes = Seq(node))
        }
      }
    }
  }

  def appendPaddingFromFirstNonWhitespace(max: Int): this.type = {
    // First non-whitespace character is the start
    val latestNewline = builder.lastIndexOf("\n")
    val start = builder.indexWhere(!_.isWhitespace, latestNewline + 1)
    append(" " * (max - ((builder.length - 1) - start)))
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
    // TODO: follow binary space rules as described at
    // https://github.com/golang/go/blob/96414ca39f4a63d04ddc5cea6d4c63237f2a5fd4/src/go/printer/nodes.go#L598
    require(expr.operator.string != null)
    appendExpression(expr.left).append(' ').append(expr.operator.string.get).append(' ').appendExpression(expr.right)
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

  def appendCaseClause(stmt: CaseClause): this.type = {
    // The case part is one indent back, and we leave the rest indented
    removeLastIndent()
    if (stmt.expressions.isEmpty) append("default:")
    else append("case ").commaSeparated(stmt.expressions, appendExpression).append(':')
    stmt.body.statements.foreach { newline().appendStatement(_) }
    this
  }

  def appendChannelType(expr: ChannelType): this.type = __TODO__

  def appendCommClause(stmt: CommClause): this.type = __TODO__

  def appendComment(node: Comment): this.type = {
    // Just do simple comment for now
    require(!node.text.contains("\n"), "Newlines not supported yet in comments")
    append(s"// ${node.text}")
  }

  def appendCommentStatement(node: CommentStatement): this.type = {
    appendComment(node.comment)
  }

  def appendCompositeLiteral(expr: CompositeLiteral): this.type = {
    expr.typ.foreach(appendExpression)
    append('{').indent()
    paddedSections(expr.elements).foreach { section =>
      section.nodes.foreach {
        case kv: Node.KeyValueExpression => newline().appendKeyValueExpression(kv, section.leftMax).append(',')
        case n => newline().appendExpression(n).append(',')
      }
    }
    dedent()
    if (expr.elements.nonEmpty) newline()
    append('}')
  }

  def appendDeclaration(decl: Declaration): this.type = decl match {
    case f: FunctionDeclaration => appendFunctionDeclaration(f)
    case g: GenericDeclaration => appendGenericDeclaration(g)
  }

  def appendDeclarationStatement(stmt: DeclarationStatement): this.type = {
    appendDeclaration(stmt.declaration)
  }

  def appendDeferStatement(stmt: DeferStatement): this.type = {
    append("defer ").appendCallExpression(stmt.call)
  }

  def appendEllipsis(expr: Ellipsis): this.type = {
    append("...")
    expr.elementType.foreach(appendExpression)
    this
  }

  def appendEmptyStatement(): this.type = this

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
    case k: KeyValueExpression => appendKeyValueExpression(k, None)
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

  def appendField(field: Field, padNameTo: Option[Int] = None, interfaceField: Boolean = false): this.type = {
    // As a special case, interface fields don't use the word func in their name nor are they padded in
    // any way
    if (interfaceField && field.typ.isInstanceOf[FunctionType]) {
      require(field.names.length == 1, "Expected interface fn to have single name")
      appendIdentifier(field.names.head).appendFunctionTypeSignature(field.typ.asInstanceOf[FunctionType])
    } else {
      if (field.names.nonEmpty) commaSeparated(field.names, appendIdentifier).append(" ")
      padNameTo.foreach(appendPaddingFromFirstNonWhitespace)
      appendExpression(field.typ)
      field.tag.foreach {
        append(' ').appendBasicLiteral(_)
      }
    }
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
    appendIdentifier(decl.name).appendFunctionTypeSignature(decl.typ)
    decl.body.foreach { append(' ').appendBlockStatement(_) }
    this
  }

  def appendFunctionLiteral(expr: FunctionLiteral): this.type = {
    appendFunctionType(expr.typ).append(' ').appendBlockStatement(expr.body)
  }

  def appendFunctionType(typ: FunctionType): this.type = {
    append("func").appendFunctionTypeSignature(typ)
  }

  def appendFunctionTypeSignature(typ: FunctionType): this.type = {
    appendParameters(typ.parameters, "()")
    if (typ.results.nonEmpty) append(' ')
    if (typ.results.length == 1) appendField(typ.results.head)
    else appendParameters(typ.results)
  }

  def appendGenericDeclaration(decl: GenericDeclaration): this.type = {
    require(decl.specifications.nonEmpty, s"Unexpected empty specs: $decl")
    var specs = decl.specifications
    decl.token match {
      case Token.Import =>
        append("import ")
        // Have to sort specs for import
        specs = specs.sortBy {
          case ImportSpecification(name, path) => name.map(_.name).getOrElse(path.value)
          case s => sys.error(s"Unrecognized import spec: $s")
        }
      case Token.Const => append("const ")
      case Token.Type => append("type ")
      case Token.Var => append("var ")
      case _ => sys.error("Unrecognized token: " + decl.token)
    }
    val multiSpec = specs.length > 1
    if (multiSpec) append('(').indent()
    paddedSections(specs).foreach { section =>
      section.nodes.foreach { spec =>
        if (multiSpec) newline()
        appendSpecification(spec, section.leftMax)
      }

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

  def appendIndexExpression(expr: IndexExpression): this.type = {
    appendExpression(expr.expression).append('[').appendExpression(expr.index).append(']')
  }

  def appendInterfaceType(expr: InterfaceType): this.type = {
    if (expr.methods.isEmpty) append("interface{}")
    else {
      append("interface {").indent()
      // We want to sort the fields
      val methods = expr.methods.sortBy(NodeWriter.sortableFieldTuple)
      methods.foreach(f => newline().appendField(f, interfaceField = true))
      dedent().newline().append('}')
    }
  }

  def appendKeyValueExpression(expr: KeyValueExpression, padLeftTo: Option[Int]): this.type = {
    appendExpression(expr.key).append(':')
    padLeftTo.foreach(appendPaddingFromFirstNonWhitespace)
    appendExpression(expr.value)
  }

  def appendLabeledStatement(stmt: LabeledStatement): this.type = {
    // Labels need to be one back
    removeLastIndent()
    appendIdentifier(stmt.label).append(':').newline().appendStatement(stmt.statement)
  }

  def appendMapType(expr: MapType): this.type = {
    append("map[").appendExpression(expr.key).append(']').appendExpression(expr.value)
  }

  def appendNode(node: Node): this.type = node match {
    case c: Comment => appendComment(c)
    case d: Declaration => appendDeclaration(d)
    case e: Expression => appendExpression(e)
    case f: Field => appendField(f)
    case f: File => appendFile(f)
    case p: Package => appendPackage(p)
    case s: Specification => appendSpecification(s, None)
    case s: Statement => appendStatement(s)
  }

  def appendPackage(node: Package): this.type = __TODO__

  def appendParameters(params: Seq[Field], appendOnEmpty: String = ""): this.type = {
    if (params.isEmpty) append(appendOnEmpty)
    else append('(').commaSeparated(params, appendField(_: Field, None)).append(')')
  }

  def appendParenthesizedExpression(expr: ParenthesizedExpression): this.type = {
    append('(').appendExpression(expr.expression).append(')')
  }

  def appendRangeStatement(stmt: RangeStatement): this.type = {
    require(stmt.token.exists(_.string.isDefined), "Token with string required for now...")
    append("for ")
    stmt.key match {
      case None => append('_')
      case Some(key) => appendExpression(key)
    }
    stmt.value.foreach(v => append(", ").appendExpression(v))
    append(' ').append(stmt.token.get.string.get).append(" range ").
      appendExpression(stmt.expression).append(' ').appendBlockStatement(stmt.body)
  }

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

  def appendSpecification(spec: Specification, padLeftTo: Option[Int]): this.type = spec match {
    case i: ImportSpecification => appendImportSpecification(i)
    case v: ValueSpecification => appendValueSpecification(v, padLeftTo)
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
    case c: CommentStatement => appendCommentStatement(c)
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
      paddedSections(expr.fields).foreach { section =>
        val sortedNodes = section.nodes.sortBy(NodeWriter.sortableFieldTuple)
        sortedNodes.foreach { f => newline().appendField(f, section.leftMax) }
      }
      dedent().newline().append('}')
    }
  }

  def appendSwitchStatement(stmt: SwitchStatement): this.type = {
    append("switch ")
    stmt.init.foreach { appendStatement(_).append("; ") }
    stmt.tag.foreach { appendExpression(_).append(' ') }
    appendBlockStatement(stmt.body)
  }

  def appendTypeAssertExpression(expr: TypeAssertExpression): this.type = {
    require(expr.typ.isDefined, "Type for assertion required")
    appendExpression(expr.expression).append(".").append('(')
    expr.typ match {
      case None => append("type")
      case Some(typ) => appendExpression(typ)
    }
    append(')')
  }

  def appendTypeSpecification(spec: TypeSpecification): this.type = {
    appendIdentifier(spec.name).append(' ').appendExpression(spec.typ)
  }

  def appendUnaryExpression(expr: UnaryExpression): this.type = expr.operator.string match {
    case None => sys.error("No string for token")
    case Some(str) => append(str).appendExpression(expr.operand)
  }

  def appendValueSpecification(spec: ValueSpecification, padLeftTo: Option[Int]): this.type = {
    commaSeparated(spec.names, appendIdentifier)
    padLeftTo.foreach(appendPaddingFromFirstNonWhitespace)
    spec.typ.foreach { append(' ').appendExpression(_) }
    if (spec.values.nonEmpty) append(" = ").commaSeparated(spec.values, appendExpression)
    this
  }
}
