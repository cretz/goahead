package goahead.cli

import java.lang.reflect.Modifier

import goahead.ast.Node
import goahead.compile.ClassPath.ClassDetails
import goahead.compile.MethodCompiler.Context
import goahead.compile._

case class CompileConfig(
  excludeRunningRuntimeJar: Boolean = false,
  classPath: Seq[String] = Nil,
  classes: Seq[String] = Seq("*"),
  anyClassModifiers: Set[String] = Set.empty,
  outDir: String,
  parallel: Boolean = false,
  excludeSuperClassesOfSameEntry: Boolean = false,
  mangler: Option[String] = None,
  fileGrouping: CompileConfig.FileGrouping = CompileConfig.FileGrouping.Cls,
  prependToFile: Option[String] = None,
  excludeAlreadyWrittenFiles: Boolean = false,
  excludeInnerClasses: Boolean = false,
  includeOldVersionClasses: Boolean = false,
  packagePrivateExported: Boolean = false,
  classManips: CompileConfig.ClassManips = CompileConfig.ClassManips.empty
) {
  lazy val manglerInst = mangler.map(Class.forName(_).newInstance().asInstanceOf[Mangler]).getOrElse {
    //Mangler.Simple
    new Mangler.Compact(packagePrivateUnexported = !packagePrivateExported)
  }
}

object CompileConfig {
  import AstDsl._
  import Helpers._

  sealed trait FileGrouping {
    def groupClassBy(det: ClassDetails): String
  }

  object FileGrouping {
    def apply(name: String): FileGrouping = name match {
      case "class" => Cls
      case "class-sans-inner" => ClsSansInner
      case "package" => Pkg
      case str if str.startsWith("first-packages-") => FirstXPkgs(str.substring("first-packages-".length).toInt)
      case _ => sys.error(s"Unrecognized file grouping: $name")
    }

    case object Cls extends FileGrouping {
      // Put inner classes together
      override def groupClassBy(det: ClassDetails) = det.cls.name.indexOf('$') match {
        case -1 => det.cls.name
        case index => det.cls.name.substring(0, index)
      }
    }

    case object ClsSansInner extends FileGrouping {
      override def groupClassBy(det: ClassDetails) = det.cls.name
    }

    case object Pkg extends FileGrouping {
      override def groupClassBy(det: ClassDetails) = det.cls.packageName
    }

    case class FirstXPkgs(count: Int) extends FileGrouping {
      override def groupClassBy(det: ClassDetails) = {
        if (det.cls.packageName.isEmpty) "__root__" else {
          val pieces = det.cls.packageName.split('/')
          pieces.take(count).mkString("/")
        }
      }
    }
  }

  sealed trait ClassPatternMatch {
    def matches(cls: Cls): Boolean = matches(cls.name.replace('/', '.'))
    def matches(cls: String): Boolean
  }

  object ClassPatternMatch {
    lazy val all = apply("*")

    def apply(str: String): ClassPatternMatch = str.last match {
      case '*' => StartsWithDeep(str.init)
      case '?' => StartsWithShallow(str.init)
      case _ => Exact(str)
    }

    case class StartsWithDeep(begin: String) extends ClassPatternMatch {
      override def matches(cls: String) = cls.startsWith(begin)
      override def toString = s"$begin*"
    }
    case class StartsWithShallow(begin: String) extends ClassPatternMatch {
      override def matches(cls: String) = cls.lastIndexOf('.') < begin.length && cls.startsWith(begin)
      override def toString = s"$begin?"
    }
    case class Exact(str: String) extends ClassPatternMatch {
      override def matches(cls: String) = cls == str
      override def toString = str
    }
  }

  sealed trait MemberPatternMatch {
    def matches(method: Method): Boolean
    def matches(field: Field): Boolean
  }

  object MemberPatternMatch {
    def apply(str: String): MemberPatternMatch = {
      if (str == "*") SimpleName.all else str.indexOf('(') match {
        case -1 =>
          SimpleName(Some(str), None)
        case parenIndex =>
          SimpleName(Some(str.substring(0, parenIndex)), Some(str.substring(parenIndex)))
      }
    }

    case class SimpleName(
      name: Option[String],
      desc: Option[String]
    ) extends MemberPatternMatch {

      override def matches(method: Method) =
        name.getOrElse(method.name) == method.name && desc.getOrElse(method.desc) == method.desc

      override def matches(field: Field) =
        name.getOrElse(field.name) == field.name && desc.isEmpty
    }

    object SimpleName {
      val all = SimpleName(None, None)
    }
  }

  case class ClassManips(prioritized: Seq[ClassManip]) {

    // TODO: might we want later ones to uninclude, e.g. check for first boolean?
    def isExcluded(field: Field, cmp: FilteringCompiler) =
      prioritized.view.flatMap(_.isExcluded(field, cmp)).headOption.contains(true)
    def isExcluded(method: Method, cmp: FilteringCompiler) =
      prioritized.view.flatMap(_.isExcluded(method, cmp)).headOption.contains(true)
    def shouldTransform(method: Method, cmp: FilteringCompiler) =
      prioritized.view.flatMap(_.shouldTransform(method, cmp)).headOption.contains(true)

    def transform(
      cmp: FilteringCompiler,
      method: Method,
      ctx: => MethodCompiler.Context
    ): Option[(MethodCompiler.Context, Seq[Node.Statement])] = {
      if (shouldTransform(method, cmp)) prioritized.view.flatMap(_.transform(cmp, method, ctx)).headOption else None
    }

    def goFields(cls: Cls) = prioritized.flatMap(_.goFields(cls))
  }

  object ClassManips {
    def empty = ClassManips(Nil)
  }

  case class ClassManip(
    pattern: ClassPatternMatch = ClassPatternMatch.all,
    priority: Int = 0,
    name: String = "",
    fieldFilter: FieldFilter = FieldFilter.IncludeAll,
    fields: FieldManips = FieldManips(Nil),
    methodFilter: MethodFilter = MethodFilter.IncludeAll,
    methods: MethodManips = MethodManips(Nil)
  ) {
    def matchesClass(cls: Cls) = pattern.matches(cls)

    def matchesFilter(field: Field, cmp: FilteringCompiler) = fieldFilter.matches(field, cmp)

    def matchesFilter(method: Method, cmp: FilteringCompiler) = methodFilter.matches(method, cmp)

    def isExcluded(field: Field, cmp: FilteringCompiler) = {
      if (!matchesClass(field.cls) || !matchesFilter(field, cmp)) None
      else fields.isExcluded(field, cmp)
    }

    def isExcluded(method: Method, cmp: FilteringCompiler) = {
      if (!matchesClass(method.cls) || !matchesFilter(method, cmp)) None
      else methods.isExcluded(method, cmp)
    }

    def shouldTransform(method: Method, cmp: FilteringCompiler) = {
      if (!matchesClass(method.cls) || !matchesFilter(method, cmp)) None
      else methods.shouldTransform(method, cmp)
    }

    def transform(
      cmp: FilteringCompiler,
      method: Method,
      ctx: => MethodCompiler.Context
    ): Option[(MethodCompiler.Context, Seq[Node.Statement])] = {
      if (!matchesClass(method.cls) || !matchesFilter(method, cmp)) None else methods.transform(cmp, method, ctx)
    }

    def goFields(cls: Cls) = if (!matchesClass(cls)) Nil else fields.goFields
  }

  sealed trait MemberFilter {
    def anyModifiers: Set[String]
    def matchesMod(access: Int) = anyModifiers.isEmpty || {
      val modStr = Modifier.toString(access)
      anyModifiers.exists {
        case "package-private" => access.isAccessPackagePrivate
        case mod => modStr.contains(mod)
      }
    }

    def matchesExcludedTypes(
      referencesExcludedClass: Option[Boolean],
      cmp: FilteringCompiler,
      me: Cls,
      typs: => Iterable[IType]
    ): Boolean = referencesExcludedClass match {
      case None => true
      case Some(shouldMatch) => typs.exists(isExcludedType(cmp, me, _)) == shouldMatch
    }

    def isExcludedType(cmp: FilteringCompiler, me: Cls, toCheck: IType): Boolean = toCheck match {
      case typ: IType.Simple if typ.isArray =>
        isExcludedType(cmp, me, typ.arrayElementType)
      case typ: IType.Simple if typ.isObject =>
        // First check if included, if not check if in my entry
        val (otherEntry, otherDet) = cmp.classPath.getFirstClassWithEntry(toCheck.internalName)
        !cmp.includedClasses.contains(otherDet) && {
          val (myEntry, _) = cmp.classPath.getFirstClassWithEntry(me.name)
          otherEntry == myEntry
        }
      case _ =>
        false
    }
  }

  case class FieldFilter(
    referencesExcludedClass: Option[Boolean] = None,
    anyModifiers: Set[String] = Set.empty
  ) extends MemberFilter {
    def matches(f: Field, cmp: FilteringCompiler): Boolean = {
      matchesMod(f.access) &&
        matchesExcludedTypes(referencesExcludedClass, cmp, f.cls, Seq(IType.getType(f.desc)))
    }
  }

  object FieldFilter {
    val IncludeAll = FieldFilter()
  }

  case class MethodFilter(
    signatureReferencesExcludedClass: Option[Boolean] = None,
    bodyReferencesExcludedClass: Option[Boolean] = None,
    anyModifiers: Set[String] = Set.empty
  ) extends MemberFilter {
    def matches(m: Method, cmp: FilteringCompiler): Boolean = {
      matchesMod(m.access) &&
        matchesExcludedTypes(signatureReferencesExcludedClass, cmp, m.cls, m.argTypes :+ m.returnType) &&
        matchesExcludedTypes(bodyReferencesExcludedClass, cmp, m.cls, m.instructionRefTypes())
    }
  }

  object MethodFilter {
    val IncludeAll = MethodFilter()
  }

  case class FieldManips(values: Seq[(MemberPatternMatch, FieldManip)]) {
    def isExcluded(field: Field, cmp: FilteringCompiler) = {
      values.collectFirst { case (mtch, FieldManip.Exclude(ex)) if mtch.matches(field) => ex }
    }

    // First string is field name, second is optional go package then string go type name
    def goFields: Seq[(String, (Option[String], String))] = values.collect {
      case (MemberPatternMatch.SimpleName(Some(name), _), g: FieldManip.GoType) =>
        name -> g.withPackage
      case (_, FieldManip.GoType(n)) =>
        sys.error(s"Cannot make go field of $n without specific field name, instead had no name")
    }
  }

  sealed trait FieldManip
  object FieldManip {
    case class Exclude(exclude: Boolean) extends FieldManip
    case class GoType(go: String) extends FieldManip {
      def withPackage = go.lastIndexOf('.') match {
        case -1 => None -> go
        case index => Some(go.take(index)) -> go.drop(index + 1)
      }
    }
  }

  case class MethodManips(values: Seq[(MemberPatternMatch, MethodManip)]) {
    def isExcluded(method: Method, cmp: FilteringCompiler) = {
      values.collectFirst { case (mtch, MethodManip.Exclude(ex)) if mtch.matches(method) => ex }
    }

    def shouldTransform(method: Method, cmp: FilteringCompiler) = {
      values.view.collect({
        case (mtch, manip) if mtch.matches(method) => manip.shouldTransform(method, cmp)
      }).flatten.headOption
    }

    def transform(
      cmp: FilteringCompiler,
      method: Method,
      ctx: => MethodCompiler.Context
    ): Option[(MethodCompiler.Context, Seq[Node.Statement])] = {
      values.view.flatMap({
        case (mtch, manip) if mtch.matches(method) => manip.transform(cmp, method, ctx)
        case _ => None
      }).headOption
    }
  }

  sealed trait MethodManip {
    def shouldTransform(method: Method, cmp: FilteringCompiler): Option[Boolean] = None
    def transform(
      cmp: FilteringCompiler,
      method: Method,
      ctx: => MethodCompiler.Context
    ): Option[(MethodCompiler.Context, Seq[Node.Statement])] = None
  }

  object MethodManip {
    case object Empty extends MethodManip {
      override def shouldTransform(method: Method, cmp: FilteringCompiler) = Some(true)
      override def transform(cmp: FilteringCompiler, method: Method, ctx: => MethodCompiler.Context) = Some {
        ctx.method.returnType match {
          case IType.VoidType => ctx -> Nil
          case other => ctx -> Seq(other.zeroExpr.ret)
        }
      }
    }

    case class Exclude(exclude: Boolean) extends MethodManip

    case object Panic extends MethodManip {
      override def shouldTransform(method: Method, cmp: FilteringCompiler) = Some(true)
      override def transform(cmp: FilteringCompiler, method: Method, ctx: => MethodCompiler.Context) = Some {
        ctx -> Seq("panic".toIdent.call(Seq(s"Method not implemented - $method".toLit)).toStmt)
      }
    }

    case object AsIs extends MethodManip {
      override def shouldTransform(method: Method, cmp: FilteringCompiler) = Some(false)
    }

    case class GoForward(go: String) extends MethodManip {
      override def transform(cmp: FilteringCompiler, method: Method, ctx: => MethodCompiler.Context) = Some {
        val call = go.toIdent.call("thsyis".toIdent +: ctx.method.argTypes.indices.map(i => s"var$i".toIdent))
        ctx.method.returnType match {
          case IType.VoidType => ctx -> Seq(call.toStmt)
          case _ => ctx -> Seq(call.ret)
        }
      }
    }

    object GoForwardFromOutFolder extends MethodManip {

      def forwarder(method: Method, cmp: FilteringCompiler) = {
        cmp.forwarders.get(method.cls.name).flatMap { fwds =>
          fwds.find(_.instance == !method.access.isAccessStatic).flatMap(v =>
            v.methods.find(_.from == method).map(v -> _)
          )
        }
      }

      override def shouldTransform(method: Method, cmp: FilteringCompiler) = {
        forwarder(method, cmp).map(_ => true)
      }

      override def transform(cmp: FilteringCompiler, method: Method, ctx: => Context) = {
        forwarder(method, cmp).map { case (fwd, fwdMethod) =>
          val call = "this".toIdent.sel(fwd.forwardFieldName).sel(fwdMethod.targetName).
            call(ctx.method.argTypes.indices.map(i => s"var$i".toIdent))
          method.returnType match {
            case IType.VoidType => ctx -> Seq(call.toStmt)
            case _ => ctx -> Seq(call.ret)
          }
        }
      }
    }
  }
}