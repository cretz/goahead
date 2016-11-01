package goahead.ast

sealed trait Node

object Node {
  case class Comment(text: String) extends Node

  case class Field(
    names: Seq[Identifier],
    typ: Expression,
    tag: Option[BasicLiteral] = None
  ) extends Node

  case class File(
    packageName: Identifier,
    declarations: Seq[Declaration]
  ) extends Node

  case class Package(
    name: String,
    files: Seq[File]
  ) extends Node

  sealed trait Expression extends Node

  case class Identifier(name: String) extends Expression

  case class Ellipsis(elementType: Option[Expression]) extends Expression

  case class BasicLiteral(
    token: Token,
    value: String
  ) extends Expression

  case class FunctionLiteral(
    typ: FunctionType,
    body: BlockStatement
  ) extends Expression

  case class CompositeLiteral(
    typ: Option[Expression],
    elements: Seq[Expression]
  ) extends Expression

  case class ParenthesizedExpression(expression: Expression) extends Expression

  case class SelectorExpression(
    expression: Expression,
    selector: Identifier
  ) extends Expression

  case class IndexExpression(
    expression: Expression,
    index: Expression
  ) extends Expression

  case class SliceExpression(
    expression: Expression,
    low: Option[Expression],
    high: Option[Expression],
    max: Option[Expression],
    slice3: Boolean
  ) extends Expression

  case class TypeAssertExpression(
    expression: Expression,
    typ: Option[Expression]
  ) extends Expression

  case class CallExpression(
    function: Expression,
    args: Seq[Expression] = Seq.empty
  ) extends Expression

  case class StarExpression(expression: Expression) extends Expression

  case class UnaryExpression(
    operator: Token,
    operand: Expression
  ) extends Expression

  case class BinaryExpression(
    left: Expression,
    operator: Token,
    right: Expression
  ) extends Expression

  case class KeyValueExpression(
    key: Expression,
    value: Expression
  ) extends Expression

  case class ArrayType(
    typ: Expression,
    length: Option[Expression] = None
  ) extends Expression

  case class StructType(fields: Seq[Field]) extends Expression

  case class FunctionType(
    parameters: Seq[Field],
    results: Seq[Field]
  ) extends Expression

  case class InterfaceType(methods: Seq[Field]) extends Expression

  case class MapType(
    key: Expression,
    value: Expression
  ) extends Expression

  sealed trait ChannelDirection
  object ChannelDirection {
    case object Send extends ChannelDirection
    case object Receive extends ChannelDirection
  }

  case class ChannelType(
    direction: ChannelDirection,
    value: Expression
  ) extends Expression

  sealed trait Statement extends Node

  case class DeclarationStatement(declaration: Declaration) extends Statement

  case object EmptyStatement extends Statement

  case class LabeledStatement(
    label: Identifier,
    statement: Statement
  ) extends Statement

  case class ExpressionStatement(expression: Expression) extends Statement

  case class SendStatement(
    channel: Expression,
    value: Expression
  ) extends Statement

  case class IncrementDecrementStatement(
    expression: Expression,
    token: Token
  ) extends Statement

  case class AssignStatement(
    left: Seq[Expression],
    token: Token,
    right: Seq[Expression]
  ) extends Statement

  case class GoStatement(call: CallExpression) extends Statement

  case class DeferStatement(call: CallExpression) extends Statement

  case class ReturnStatement(results: Seq[Expression]) extends Statement

  case class BranchStatement(
    token: Token,
    label: Option[Identifier]
  ) extends Statement

  case class BlockStatement(statements: Seq[Statement]) extends Statement

  case class IfStatement(
    init: Option[Statement] = None,
    condition: Expression,
    body: BlockStatement,
    elseStatement: Option[Statement] = None
  ) extends Statement

  case class CaseClause(
    expressions: Seq[Expression],
    body: BlockStatement
  ) extends Statement

  case class SwitchStatement(
    init: Option[Statement],
    tag: Option[Expression],
    body: BlockStatement
  ) extends Statement

  case class CommClause(
    comm: Option[Statement],
    body: Seq[Statement]
  ) extends Statement

  case class SelectStatement(body: BlockStatement) extends Statement

  case class ForStatement(
    init: Option[Statement],
    condition: Option[Statement],
    post: Option[Statement],
    body: BlockStatement
  ) extends Statement

  case class RangeStatement(
    key: Option[Expression],
    value: Option[Expression],
    token: Option[Token],
    expression: Expression,
    body: BlockStatement
  ) extends Statement

  sealed trait Specification extends Node

  case class ImportSpecification(
    name: Option[Identifier],
    path: BasicLiteral
  ) extends Specification

  case class ValueSpecification(
    names: Seq[Identifier],
    typ: Option[Expression],
    values: Seq[Identifier]
  ) extends Specification

  case class TypeSpecification(
    name: Identifier,
    typ: Expression
  ) extends Specification

  sealed trait Declaration extends Node

  case class GenericDeclaration(
    token: Token,
    specifications: Seq[Specification]
  ) extends Declaration

  case class FunctionDeclaration(
    receivers: Seq[Field],
    name: Identifier,
    typ: FunctionType,
    body: Option[BlockStatement]
  ) extends Declaration

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
