package goahead.ast

sealed trait Node { self =>
  def walk(w: NodeWalker): Node
}

object Node {
  case class Comment(text: String) extends Node {
    def walk(w: NodeWalker) = this
  }

  case class Field(
    names: Seq[Identifier],
    typ: Expression,
    tag: Option[BasicLiteral] = None
  ) extends Node {
    def walk(w: NodeWalker) = copy(
      names = w.applyAll(names),
      typ = w.applyGet(typ),
      tag = w.applyOpt(tag)
    )
  }

  case class File(
    packageName: Identifier,
    declarations: Seq[Declaration]
  ) extends Node {
    def walk(w: NodeWalker) = copy(
      packageName = w.applyGet(packageName),
      declarations = w.applyAll(declarations)
    )
  }

  case class Package(
    name: String,
    files: Seq[File]
  ) extends Node {
    def walk(w: NodeWalker) = copy(
      files = w.applyAll(files)
    )
  }

  sealed trait Expression extends Node

  case class Identifier(name: String) extends Expression {
    def walk(w: NodeWalker) = this
  }

  case class Ellipsis(elementType: Option[Expression]) extends Expression {
    def walk(w: NodeWalker) = copy(
      elementType = w.applyOpt(elementType)
    )
  }

  case class BasicLiteral(
    token: Token,
    value: String
  ) extends Expression {
    def walk(w: NodeWalker) = this
  }

  case class FunctionLiteral(
    typ: FunctionType,
    body: BlockStatement
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      typ = w.applyGet(typ),
      body = w.applyGet(body)
    )
  }

  case class CompositeLiteral(
    typ: Option[Expression],
    elements: Seq[Expression]
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      typ = w.applyOpt(typ),
      elements = w.applyAll(elements)
    )
  }

  case class ParenthesizedExpression(expression: Expression) extends Expression {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression)
    )
  }

  case class SelectorExpression(
    expression: Expression,
    selector: Identifier
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression),
      selector = w.applyGet(selector)
    )
  }

  case class IndexExpression(
    expression: Expression,
    index: Expression
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression),
      index = w.applyGet(index)
    )
  }

  case class SliceExpression(
    expression: Expression,
    low: Option[Expression],
    high: Option[Expression],
    max: Option[Expression],
    slice3: Boolean
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression),
      low = w.applyOpt(low),
      high = w.applyOpt(high),
      max = w.applyOpt(max)
    )
  }

  case class TypeAssertExpression(
    expression: Expression,
    typ: Option[Expression]
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression),
      typ = w.applyOpt(typ)
    )
  }

  case class CallExpression(
    function: Expression,
    args: Seq[Expression] = Seq.empty
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      function = w.applyGet(function),
      args = w.applyAll(args)
    )
  }

  case class StarExpression(expression: Expression) extends Expression {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression)
    )
  }

  case class UnaryExpression(
    operator: Token,
    operand: Expression
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      operand = w.applyGet(operand)
    )
  }

  case class BinaryExpression(
    left: Expression,
    operator: Token,
    right: Expression
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      left = w.applyGet(left),
      right = w.applyGet(right)
    )
  }

  case class KeyValueExpression(
    key: Expression,
    value: Expression
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      key = w.applyGet(key),
      value = w.applyGet(value)
    )
  }

  case class ArrayType(
    typ: Expression,
    length: Option[Expression] = None
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      typ = w.applyGet(typ),
      length = w.applyOpt(length)
    )
  }

  case class StructType(fields: Seq[Field]) extends Expression {
    def walk(w: NodeWalker) = copy(
      fields = w.applyAll(fields)
    )
  }

  case class FunctionType(
    parameters: Seq[Field],
    results: Seq[Field]
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      parameters = w.applyAll(parameters),
      results = w.applyAll(results)
    )
  }

  case class InterfaceType(methods: Seq[Field]) extends Expression {
    def walk(w: NodeWalker) = copy(
      methods = w.applyAll(methods)
    )
  }

  case class MapType(
    key: Expression,
    value: Expression
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      key = w.applyGet(key),
      value = w.applyGet(value)
    )
  }

  sealed trait ChannelDirection
  object ChannelDirection {
    case object Send extends ChannelDirection
    case object Receive extends ChannelDirection
  }

  case class ChannelType(
    direction: ChannelDirection,
    value: Expression
  ) extends Expression {
    def walk(w: NodeWalker) = copy(
      value = w.applyGet(value)
    )
  }

  sealed trait Statement extends Node

  case class DeclarationStatement(declaration: Declaration) extends Statement {
    def walk(w: NodeWalker) = copy(
      declaration = w.applyGet(declaration)
    )
  }

  case object EmptyStatement extends Statement {
    def walk(w: NodeWalker) = this
  }

  case class LabeledStatement(
    label: Identifier,
    statement: Statement
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      label = w.applyGet(label),
      statement = w.applyGet(statement)
    )
  }

  case class ExpressionStatement(expression: Expression) extends Statement {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression)
    )
  }

  case class SendStatement(
    channel: Expression,
    value: Expression
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      channel = w.applyGet(channel),
      value = w.applyGet(value)
    )
  }

  case class IncrementDecrementStatement(
    expression: Expression,
    token: Token
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      expression = w.applyGet(expression)
    )
  }

  case class AssignStatement(
    left: Seq[Expression],
    token: Token,
    right: Seq[Expression]
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      left = w.applyAll(left),
      right = w.applyAll(right)
    )
  }

  case class GoStatement(call: CallExpression) extends Statement {
    def walk(w: NodeWalker) = copy(
      call = w.applyGet(call)
    )
  }

  case class DeferStatement(call: CallExpression) extends Statement {
    def walk(w: NodeWalker) = copy(
      call = w.applyGet(call)
    )
  }

  case class ReturnStatement(results: Seq[Expression]) extends Statement {
    def walk(w: NodeWalker) = copy(
      results = w.applyAll(results)
    )
  }

  case class BranchStatement(
    token: Token,
    label: Option[Identifier]
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      label = w.applyOpt(label)
    )
  }

  case class BlockStatement(statements: Seq[Statement]) extends Statement {
    def walk(w: NodeWalker) = copy(
      statements = w.applyAll(statements)
    )
  }

  case class IfStatement(
    init: Option[Statement] = None,
    condition: Expression,
    body: BlockStatement,
    elseStatement: Option[Statement] = None
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      init = w.applyOpt(init),
      condition = w.applyGet(condition),
      body = w.applyGet(body),
      elseStatement = w.applyOpt(elseStatement)
    )
  }

  case class CaseClause(
    expressions: Seq[Expression],
    body: BlockStatement
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      expressions = w.applyAll(expressions),
      body = w.applyGet(body)
    )
  }

  case class SwitchStatement(
    init: Option[Statement],
    tag: Option[Expression],
    body: BlockStatement
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      init = w.applyOpt(init),
      tag = w.applyOpt(tag),
      body = w.applyGet(body)
    )
  }

  case class CommClause(
    comm: Option[Statement],
    body: Seq[Statement]
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      comm = w.applyOpt(comm),
      body = w.applyAll(body)
    )
  }

  case class SelectStatement(body: BlockStatement) extends Statement {
    def walk(w: NodeWalker) = copy(
      body = w.applyGet(body)
    )
  }

  case class ForStatement(
    init: Option[Statement],
    condition: Option[Statement],
    post: Option[Statement],
    body: BlockStatement
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      init = w.applyOpt(init),
      condition = w.applyOpt(condition),
      post = w.applyOpt(post),
      body = w.applyGet(body)
    )
  }

  case class RangeStatement(
    key: Option[Expression],
    value: Option[Expression],
    token: Option[Token],
    expression: Expression,
    body: BlockStatement
  ) extends Statement {
    def walk(w: NodeWalker) = copy(
      key = w.applyOpt(key),
      value = w.applyOpt(value),
      expression = w.applyGet(expression),
      body = w.applyGet(body)
    )
  }

  sealed trait Specification extends Node

  case class ImportSpecification(
    name: Option[Identifier],
    path: BasicLiteral
  ) extends Specification {
    def walk(w: NodeWalker) = copy(
      name = w.applyOpt(name),
      path = w.applyGet(path)
    )
  }

  case class ValueSpecification(
    names: Seq[Identifier],
    typ: Option[Expression],
    values: Seq[Identifier]
  ) extends Specification {
    def walk(w: NodeWalker) = copy(
      names = w.applyAll(names),
      typ = w.applyOpt(typ),
      values = w.applyAll(values)
    )
  }

  case class TypeSpecification(
    name: Identifier,
    typ: Expression
  ) extends Specification {
    def walk(w: NodeWalker) = copy(
      name = w.applyGet(name),
      typ = w.applyGet(typ)
    )
  }

  sealed trait Declaration extends Node

  case class GenericDeclaration(
    token: Token,
    specifications: Seq[Specification]
  ) extends Declaration {
    def walk(w: NodeWalker) = copy(
      specifications = w.applyAll(specifications)
    )
  }

  case class FunctionDeclaration(
    receivers: Seq[Field],
    name: Identifier,
    typ: FunctionType,
    body: Option[BlockStatement]
  ) extends Declaration {
    def walk(w: NodeWalker) = copy(
      receivers = w.applyAll(receivers),
      name = w.applyGet(name),
      typ = w.applyGet(typ),
      body = w.applyOpt(body)
    )
  }

  sealed abstract class Token(val string: Option[String] = None)
  object Token {
    case object Illegal extends Token
    case object Eof extends Token
    case object Comment extends Token

    case object Ident extends Token
    case object Int extends Token
    case object Float extends Token
    case object Imag extends Token
    case object Char extends Token
    case object String extends Token

    case object Add extends Token(Some("+"))
    case object Sub extends Token(Some("-"))
    case object Mul extends Token(Some("*"))
    case object Quo extends Token(Some("/"))
    case object Rem extends Token(Some("%"))

    case object And extends Token(Some("&"))
    case object Or extends Token(Some("|"))
    case object Xor extends Token(Some("^"))
    case object Shl extends Token(Some("<<"))
    case object Shr extends Token(Some(">>"))
    case object AndNot extends Token(Some("&^"))

    case object AddAssign extends Token(Some("+="))
    case object SubAssign extends Token(Some("-="))
    case object MulAssign extends Token(Some("*="))
    case object QuoAssign extends Token(Some("/="))
    case object RemAssign extends Token(Some("%="))

    case object AndAssign extends Token(Some("&="))
    case object OrAssign extends Token(Some("|="))
    case object XorAssign extends Token(Some("^="))
    case object ShlAssign extends Token(Some("<<="))
    case object ShrAssign extends Token(Some(">>="))
    case object AndNotAssign extends Token(Some("&^="))

    case object LAnd extends Token(Some("&&"))
    case object LOr extends Token(Some("||"))
    case object Arrow extends Token(Some("<-"))
    case object Inc extends Token(Some("++"))
    case object Dec extends Token(Some("--"))

    case object Eql extends Token(Some("=="))
    case object Lss extends Token(Some("<"))
    case object Gtr extends Token(Some(">"))
    case object Assign extends Token(Some("="))
    case object Not extends Token(Some("!"))

    case object Neq extends Token(Some("!="))
    case object Leq extends Token(Some("<="))
    case object Geq extends Token(Some(">="))
    case object Define extends Token(Some(":="))
    case object Ellipsis extends Token(Some("..."))

    case object LParen extends Token(Some("("))
    case object LBrack extends Token(Some("["))
    case object LBrace extends Token(Some("{"))
    case object Comma extends Token(Some(","))
    case object Period extends Token(Some("."))

    case object RParen extends Token(Some(")"))
    case object RBrack extends Token(Some("]"))
    case object RBrace extends Token(Some("}"))
    case object Semicolon extends Token(Some(";"))
    case object Colon extends Token(Some(":"))

    case object Break extends Token(Some("break"))
    case object Case extends Token
    case object Chan extends Token
    case object Const extends Token
    case object Continue extends Token(Some("continue"))

    case object Default extends Token
    case object Defer extends Token
    case object Else extends Token
    case object Fallthrough extends Token(Some("fallthrough"))
    case object For extends Token

    case object Func extends Token
    case object Go extends Token
    case object Goto extends Token(Some("goto"))
    case object If extends Token
    case object Import extends Token

    case object Interface extends Token
    case object Map extends Token
    case object Package extends Token
    case object Range extends Token
    case object Return extends Token

    case object Select extends Token
    case object Struct extends Token
    case object Switch extends Token
    case object Type extends Token
    case object Var extends Token
  }
}
