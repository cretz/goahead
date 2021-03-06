package goahead.compile
import java.nio.charset.StandardCharsets

import goahead.Logger
import goahead.ast.Node

object AstDsl extends Logger {
  import goahead.ast.Node._

  // TODO: check all of these later to see if they are used

  def arrayType(typ: Expression, length: Option[Expression] = None) = ArrayType(typ, length)

  def assignExisting(left: Expression, right: Expression) = left.assignExisting(right)

  def assignExistingMultiple(left: Seq[Expression], right: Seq[Expression]) = AssignStatement(left, Token.Assign, right)

  def assignDefineMultiple(left: Seq[Expression], right: Seq[Expression]) = AssignStatement(left, Token.Define, right)

  def block(stmts: Seq[Statement]) = BlockStatement(stmts)

  def comment(text: String) = Comment(text)

  def emptyInterface = InterfaceType(Nil)

  def emptyReturn = ReturnStatement(Nil)

  def emptyStmt = EmptyStatement

  def emptyStruct = StructType(Nil)

  def field(str: String, typ: Expression) = Field(Seq(str.toIdent), typ)

  def file(pkg: String, decls: Seq[Declaration]) = File(pkg.toIdent, decls)

  def funcDecl(
    rec: Option[(String, Expression)],
    name: String,
    params: Seq[(String, Expression)],
    results: Option[Expression],
    stmts: Seq[Statement]
  ): FunctionDeclaration = {
    funcDecl(rec.map((field _).tupled), name, funcType(params, results), stmts)
  }

  def funcDecl(
    rec: Option[Node.Field],
    name: String,
    funcType: FunctionType,
    stmts: Seq[Statement]
  ): FunctionDeclaration = {
    FunctionDeclaration(rec.toSeq, name.toIdent, funcType, Some(block(stmts)))
  }

  def funcType(params: Seq[(String, Expression)], result: Option[Expression] = None) = FunctionType(
    parameters = params.map((field _).tupled),
    results = result.map(Node.Field(Seq.empty, _)).toSeq
  )

  def funcTypeWithFields(params: Seq[Node.Field], result: Option[Expression] = None) = FunctionType(
    parameters = params,
    results = result.map(Node.Field(Seq.empty, _)).toSeq
  )

  def goto(label: String) = BranchStatement(Token.Goto, Some(label.toIdent))

  def id(name: String) = Identifier(name)

  def iff(init: Option[Statement], cond: Expression, body: Seq[Statement]) =
    IfStatement(init = init, condition = cond, body = block(body))

  def iff(init: Option[Statement], lhs: Expression, op: Token, rhs: Expression, body: Seq[Statement]) =
    IfStatement(
      init = init,
      condition = BinaryExpression(
        left = lhs,
        operator = op,
        right = rhs
      ),
      body = block(body)
    )

  def ifEq(left: Expression, right: Expression, body: Statement*) =
    IfStatement(
      condition = BinaryExpression(
        left = left,
        operator = Token.Eql,
        right = right
      ),
      body = block(body)
    )

  // Key is full path, value is alias; Unneeded aliases are weeded out
  def imports(mports: Seq[(String, String)]) = GenericDeclaration(
    token = Token.Import,
    specifications = mports.map { case (alias, mport) =>
      ImportSpecification(
        name = if (alias == mport || mport.endsWith(s"/$alias")) None else Some(alias.toIdent),
        path = mport.toStringLit
      )
    }
  )

  def interface(name: String, fields: Seq[Node.Field]) = GenericDeclaration(
    token = Token.Type,
    specifications = Seq(TypeSpecification(name.toIdent, InterfaceType(fields)))
  )

  def labeled(name: String, stmts: Seq[Statement]) = block(stmts).labeled(name)

  def literal(typ: Option[Expression], elems: Expression*) = CompositeLiteral(typ, elems)

  def mapLit(keyTyp: Expression, valTyp: Expression, m: Seq[(Expression, Expression)]) =
    literal(
      typ = Some(MapType(keyTyp, valTyp)),
      elems = m.map({ case (k, v) => k.withValue(v) }):_*
    )

  def sel(left: Expression, right: String) = left.sel(right)

  def struct(name: String, fields: Seq[Node.Field]) = GenericDeclaration(
    token = Token.Type,
    specifications = Seq(TypeSpecification(name.toIdent, StructType(fields)))
  )

  def varDecl(name: String, typ: Expression, value: Option[Expression] = None) = GenericDeclaration(
    token = Token.Var,
    specifications = Seq(ValueSpecification(names = Seq(name.toIdent), typ = Some(typ), values = value.toSeq))
  )

  def varDecls(namedTypes: (String, Expression)*): GenericDeclaration = {
    // We need to group the named types by type
    GenericDeclaration(
      token = Token.Var,
      specifications = namedTypes.groupBy(_._2).toSeq.map { case (typ, vars) =>
        ValueSpecification(
          names = vars.map(_._1.toIdent),
          typ = Some(typ),
          values = Nil
        )
      }
    )
  }

  implicit class RichCallExpression(val expr: CallExpression) extends AnyVal {
    def defer = DeferStatement(expr)
  }

  implicit class RichComment(val comment: Comment) extends AnyVal {
    def toStmt = CommentStatement(comment)
  }

  implicit class RichDeclaration(val decl: Declaration) extends AnyVal {
    def toStmt = DeclarationStatement(decl)
  }

  implicit class RichExpression(val expr: Expression) extends AnyVal {
    def `+`(right: Expression) = binary(Token.Add, right)

    def addressOf = unary(Token.And)

    def andAnd(right: Expression) = BinaryExpression(expr, Token.LAnd, right)

    def assignExisting(right: Expression) = AssignStatement(Seq(expr), Token.Assign, Seq(right))

    def assignDefine(right: Expression) = AssignStatement(Seq(expr), Token.Define, Seq(right))

    def binary(tok: Token, right: Expression) = BinaryExpression(expr, tok, right)

    def call(args: Seq[Expression] = Nil) = CallExpression(expr, args)

    def eql(right: Expression) = BinaryExpression(expr, Token.Eql, right)

    def ellipsis = Ellipsis(Some(expr))

    def inParens = ParenthesizedExpression(expr)

    def indexed(index: Expression) = IndexExpression(expr, index)

    def loopOver(
      key: Option[Expression],
      value: Option[Expression],
      stmts: Seq[Statement],
      token: Option[Token] = Some(Token.Define)
    ) = RangeStatement(key, value, token, expr, block(stmts))

    def namelessField = Field(Nil, expr)

    def neq(right: Expression) = BinaryExpression(expr, Token.Neq, right)

    def orOr(right: Expression) = BinaryExpression(expr, Token.LOr, right)

    def ret = ReturnStatement(Seq(expr))

    def toStmt = ExpressionStatement(expr)

    def sel(right: String) = SelectorExpression(expr, right.toIdent)

    def sliceType = ArrayType(expr)

    def star = StarExpression(expr)

    def switchOn(cases: Seq[(Expression, Seq[Statement])], default: Option[Seq[Statement]]) =
      SwitchStatement(
        init = None,
        tag = Some(expr),
        body = block(
          cases.map(c => CaseClause(c._1.singleSeq, block(c._2))) ++
          default.map(c => CaseClause(Nil, block(c)))
        )
      )

    def typeAssert(right: Expression) = TypeAssertExpression(expr, Some(right))

    def unary(tok: Token) = UnaryExpression(tok, expr)

    def withValue(v: Expression) = KeyValueExpression(expr, v)
  }

  implicit class RichFunctionType(val fnType: FunctionType) extends AnyVal {
    def toFuncLit(stmts: Seq[Statement]) = FunctionLiteral(fnType, block(stmts))

    def sansNames = FunctionType(
      parameters = fnType.parameters.map(_.copy(names = Nil)),
      results = fnType.results.map(_.copy(names = Nil))
    )
  }

  implicit class RichIfStatement(val ifStmt: IfStatement) extends AnyVal {
    def els(stmts: Statement*) = ifStmt.copy(elseStatement = Some(block(stmts)))
  }

  implicit class RichNode[T <: goahead.ast.Node](val node: T) extends AnyVal {
    def singleSeq = Seq(node)
  }

  implicit class RichStatement(val stmt: Statement) extends AnyVal {
    def labeled(label: String) = LabeledStatement(label.toIdent, stmt)
  }

  val reservedWords = Set(
    "break", "default", "func", "interface", "select",
    "case", "defer", "go", "map", "struct",
    "chan", "else", "goto", "package", "switch",
    "const", "fallthrough", "if", "range", "type",
    "continue", "for", "import", "return", "var"
  )

  implicit class RichString(val str: String) extends AnyVal {
    def dot(right: String) = sel(str.toIdent, right)

    def goSafeIdent: String = {
      // Can't be a reserved word
      if (reservedWords.contains(str)) {
        str + "_"
      } else {
        // Only dollar sign so far is off
        str.replace("$", "ŧ")
      }
    }

    def toIdent = Identifier(str.goSafeIdent)

    def toStringLit = BasicLiteral(
      Token.String,
      '"' + str.replace("\n", "\\n").replace("\r", "\\r").replace("\\", "\\\\").replace("\"", "\\\"") + '"'
    )

    def toLit = {
      // If there are any bytes outside of a normal printable range, then we will
      // just encode an array of bytes
      val outsideNorm = str.exists(c => c < 0x20 || c > 0x7f)
      if (!outsideNorm) toStringLit else {
        "string".toIdent.call(
          literal(
            typ = Some(arrayType("byte".toIdent)),
            elems = str.getBytes(StandardCharsets.UTF_8).map(v => BasicLiteral(Token.Inc, (v.toInt & 0xff).toString)):_*
          ).singleSeq
        )
      }
    }
  }
}
