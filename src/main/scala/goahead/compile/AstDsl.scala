package goahead.compile
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode

object AstDsl {
  import goahead.ast.Node._

  // TODO: check all of these later to see if they are used

  def assignExisting(left: Expression, right: Expression) = left.assignExisting(right)

  def block(stmts: Seq[Statement]) = BlockStatement(stmts)

  def field(str: String, typ: Expression) = Field(Seq(str.toIdent), typ)

  def funcType(params: Seq[(String, Expression)], results: Seq[(String, Expression)] = Nil) = FunctionType(
    parameters = params.map((field _).tupled),
    results = results.map((field _).tupled)
  )

  def goto(label: String) = BranchStatement(Token.Goto, Some(label.toIdent))

  def id(name: String) = Identifier(name)

  def ifEq(left: Expression, right: Expression, body: Statement*) =
    IfStatement(
      condition = BinaryExpression(
        left = left,
        operator = Token.Eql,
        right = right
      ),
      body = block(body)
    )

  def labeled(name: String, stmts: Seq[Statement]) = block(stmts).labeled(name)

  def sel(left: Expression, right: String) = left.sel(right)

  def varDecl(name: String, typ: Expression) = GenericDeclaration(
    token = Token.Var,
    specifications = Seq(ValueSpecification(names = Seq(name.toIdent), typ = Some(typ), Nil))
  )

  implicit class RichDeclaration(val decl: Declaration) extends AnyVal {
    def toStmt = DeclarationStatement(decl)
  }

  implicit class RichExpression(val expr: Expression) extends AnyVal {
    def `+`(right: Expression) = BinaryExpression(expr, Token.Add, right)

    def assignExisting(right: Expression) = AssignStatement(Seq(expr), Token.Assign, Seq(right))

    def call(args: Seq[Expression] = Nil) = CallExpression(expr, args)

    def ret = ReturnStatement(Seq(expr))

    def toStmt = ExpressionStatement(expr)

    def toType(oldType: Type, typ: Type): Expression = {
      oldType.getSort -> typ.getSort match {
        case (Type.INT, Type.BOOLEAN) => expr match {
          case BasicLiteral(Token.Int, "1") => "true".toIdent
          case BasicLiteral(Token.Int, "0") => "false".toIdent
          case _ => sys.error(s"Unable to change int $expr to boolean")
        }
        case (oldSort, newSort) if oldSort == newSort =>
          expr
        case _ =>
          sys.error(s"Unable to convert from type $oldType to $typ")
      }
    }

    def sel(right: String) = SelectorExpression(expr, right.toIdent)

    def star = StarExpression(expr)

  }

  implicit class RichFunctionType(val fnType: FunctionType) extends AnyVal {
    def toFuncLit(stmts: Seq[Statement]) = FunctionLiteral(fnType, block(stmts))
  }

  implicit class RichNode[T <: goahead.ast.Node](val node: T) extends AnyVal {
    def singleSeq = Seq(node)
  }

  implicit class RichStatement(val stmt: Statement) extends AnyVal {
    def labeled(label: String) = LabeledStatement(label.toIdent, stmt)
  }

  implicit class RichString(val str: String) extends AnyVal {
    def dot(right: String) = sel(str.toIdent, right)

    def goUnescaped: String =
      // TODO
      str

    def toIdent = Identifier(str)

    def toLit = BasicLiteral(Token.String, '"' + goUnescaped + '"')
  }
}
