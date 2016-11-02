package goahead.compile
import org.objectweb.asm.Type

object AstDsl {
  import goahead.ast.Node._

  // TODO: check all of these later to see if they are used

  def assignExisting(left: Expression, right: Expression) = left.assignExisting(right)

  def block(stmts: Seq[Statement]) = BlockStatement(stmts)

  def emptyReturn = ReturnStatement(Nil)

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
    rec: Option[Field],
    name: String,
    funcType: FunctionType,
    stmts: Seq[Statement]
  ): FunctionDeclaration = {
    FunctionDeclaration(rec.toSeq, name.toIdent, funcType, Some(block(stmts)))
  }

  def funcType(params: Seq[(String, Expression)], result: Option[Expression] = None) = FunctionType(
    parameters = params.map((field _).tupled),
    results = result.map(Field(Seq.empty, _)).toSeq
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

  // Key is full path, value is alias; Unneeded aliases are weeded out
  def imports(mports: Seq[(String, String)]) = GenericDeclaration(
    token = Token.Import,
    specifications = mports.map { case (alias, mport) =>
      ImportSpecification(
        name = if (alias == mport || mport.endsWith(s"/$alias")) None else Some(alias.toIdent),
        path = mport.toLit
      )
    }
  )

  def labeled(name: String, stmts: Seq[Statement]) = block(stmts).labeled(name)

  def literal(typ: Option[Expression], elems: Expression*) = CompositeLiteral(typ, elems)

  def sel(left: Expression, right: String) = left.sel(right)

  def struct(name: String, fields: Seq[Field]) = GenericDeclaration(
    token = Token.Type,
    specifications = Seq(TypeSpecification(name.toIdent, StructType(fields)))
  )

  def varDecl(name: String, typ: Expression) = GenericDeclaration(
    token = Token.Var,
    specifications = Seq(ValueSpecification(names = Seq(name.toIdent), typ = Some(typ), Nil))
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

  implicit class RichDeclaration(val decl: Declaration) extends AnyVal {
    def toStmt = DeclarationStatement(decl)
  }

  implicit class RichExpression(val expr: Expression) extends AnyVal {
    def `+`(right: Expression) = BinaryExpression(expr, Token.Add, right)

    def assignExisting(right: Expression) = AssignStatement(Seq(expr), Token.Assign, Seq(right))

    def call(args: Seq[Expression] = Nil) = CallExpression(expr, args)

    def namelessField = Field(Nil, expr)

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

    def unary(tok: Token) = UnaryExpression(tok, expr)

    def withValue(v: Expression) = KeyValueExpression(expr, v)
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
