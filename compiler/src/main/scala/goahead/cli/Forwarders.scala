package goahead.cli

import java.nio.file.Paths

import goahead.Logger
import goahead.ast.Node
import goahead.compile._

import scala.io.Source

case class Forwarders(
  cls: Cls,
  instance: Boolean,
  goStruct: String,
  hasImplField: Boolean = false,
  methods: Set[Forwarders.ForwarderMethod] = Set.empty
) {
  def forwardFieldName = if (goStruct.head.isUpper) "Fwd_" else "fwd_"
}

object Forwarders extends Logger {
  import Helpers._
  import AstDsl._

  def loadForwarders(mangler: Mangler, classPath: ClassPath, path: String): Map[String, Seq[Forwarders]] = {
    // The running list is keyed by struct name
    var runningForwarders = Map.empty[String, Forwarders]
    def addForwarderMethod(
      line: String,
      methodName: Option[String],
      methodDesc: Option[String]
    ): Seq[Method] = {
      // Has to be a func
      if (!line.startsWith("func (this *") && line.contains(')')) Nil else {
        val structName = line.substring(12, line.indexOf(')'))
        runningForwarders.get(structName) match {
          case None => Nil
          case Some(forwarder) =>
            val sig = FunctionSig(line)
            // Try to find methods
            val methods = forwarder.cls.methods.filter { m =>
              m.access.isAccessStatic != forwarder.instance &&
                (methodName.isEmpty || methodName.get.compareToIgnoreCase(m.name) == 0) &&
                (methodDesc.isEmpty || methodDesc.get == m.desc) &&
                sig.matchesMethod(mangler, m)
            }
            if (methods.size == 1) {
              runningForwarders += forwarder.goStruct ->
                forwarder.copy(methods = forwarder.methods + ForwarderMethod(methods.head, sig))
            }
            methods
        }
      }
    }

    // Read all files out of the target, looking for "//goahead-forwarder"
    Paths.get(path).toFile.listFiles().filter(_.getName.endsWith(".go")).foreach { f =>
      // Keyed by Go type
      var prevForwardComment = Option.empty[ForwardComment]
      var inStructFor = Option.empty[String]
      Source.fromFile(f, "UTF-8").getLines().foreach { line =>
        prevForwardComment match {
          // Check lines not preceded by goahead comments
          case None => line match {
            // Mark the comment as seen
            case line if line.startsWith("//goahead:") =>
              prevForwardComment = Some(ForwardComment(classPath, line))
            // If there are no forwarders and no comment, do nothing (optimization)
            case _ if runningForwarders.isEmpty && prevForwardComment.isEmpty =>
            // Mark the struct as ended
            case "}" if inStructFor.isDefined =>
              inStructFor = None
            // Check for the impl field
            case line if line.startsWith("\timpl *") && inStructFor.isDefined =>
              // The impl field
              val structName = inStructFor.get
              val forwarder = runningForwarders(structName)
              val expectedLine =
                if (forwarder.instance) "\timpl *" + mangler.implObjectName(forwarder.cls.name)
                else "\timpl *" + mangler.staticObjectName(forwarder.cls.name)
              require(line == expectedLine, s"Expected line `$expectedLine` but got `$line`")
              runningForwarders += structName -> forwarder.copy(hasImplField = true)
            // Handle function just to see if it's a forwarder
            case line if line.startsWith("func (this *") && line.contains(')') =>
              val methods = addForwarderMethod(line, None, None)
              // We only care about ambig, not empty
              require(methods.size < 2, s"Found multiple methods (${methods.mkString(", ")}) for line `$line`")
              // We will warn on empty when we're a known struct name
              if (methods.isEmpty && line.contains(')')) {
                val structName = line.substring(12, line.indexOf(')'))
                if (runningForwarders.contains(structName))
                  logger.warn(s"Unable to find any methods matching line: $line")
              }
            case _ =>
          }
          // Pre-struct comment
          case Some(ForwardComment(inst, cls, None, None)) =>
            prevForwardComment = None
            // Has to be a struct
            require(
              line.startsWith("type ") && line.drop(6).contains(" struct"),
              s"Expected line of `type SOMETHING struct` but got `$line`"
            )
            val structName = line.substring(5, line.lastIndexOf(" struct")).trim()
            inStructFor = if (line.contains('}')) None else Some(structName)
            require(!runningForwarders.contains(structName), s"Multiple goahead markers for struct $structName")
            runningForwarders += structName -> Forwarders(cls, inst, structName)
          // Pre-method comment
          case Some(ForwardComment(inst, cls, name: Some[_], descOpt)) =>
            prevForwardComment = None
            val methods = addForwarderMethod(line, name, descOpt)
            require(methods.nonEmpty, s"Cannot find matching method for line `$line`")
            require(methods.size < 2, s"Found multiple methods (${methods.mkString(", ")}) for line `$line`")
          case _ =>
        }
      }
    }

    runningForwarders.values.groupBy(_.cls.name).mapValues(_.toSeq)
  }

  case class ForwarderMethod(from: Method, targetSig: FunctionSig) {
    def compileFor(
      fwd: Forwarders,
      cmp: FilteringCompiler,
      ctx: MethodCompiler.Context
    ): (MethodCompiler.Context, Seq[Node.Statement]) = {

      // Convert the args
      val argTypesWithIndexAndTypes = ctx.method.argTypes.zipWithIndex.zip(targetSig.paramTypeNames)
      val argsAndCtx = argTypesWithIndexAndTypes.foldLeft(ctx -> Seq.empty[Node.Expression]) {
        case ((ctx, prevArgs), ((arg, i), typName)) =>
          val name = s"var$i".toIdent
          typName match {
            case "string" => ctx.importRuntimeQualifiedName("GetString").map { case (ctx, getString) =>
              ctx -> (prevArgs :+ getString.call(Seq(name)))
            }
            case "int" => ctx -> (prevArgs :+ "int".toIdent.call(Seq(name)))
            case _ => ctx -> (prevArgs :+ name)
          }
      }

      argsAndCtx.map { case (ctx, args) =>
        val call = "this".toIdent.sel(fwd.forwardFieldName).sel(targetSig.name).call(args)
        ctx.method.returnType match {
          case IType.VoidType => ctx -> Seq(call.toStmt)
          case _ => targetSig.retTypeName match {
            case Some("string") => ctx.importRuntimeQualifiedName("NewString").map { case (ctx, newString) =>
              ctx -> Seq(newString.call(Seq(call)).ret)
            }
            case Some("int") => ctx -> Seq("int32".toIdent.call(Seq(call)).ret)
            case _ => ctx -> Seq(call.ret)
          }
        }
      }
    }
  }

  case class FunctionSig(
    name: String,
    ambigName: String,
    paramTypeNames: Seq[String],
    retTypeName: Option[String]
  ) {
    def matchesMethod(mangler: Mangler, m: Method): Boolean =
      matchesMethodName(m) && matchesArgTypes(mangler, m) && matchesReturnType(mangler, m)

    def matchesArgTypes(mangler: Mangler, m: Method): Boolean =
      paramTypeNames.size == m.argTypes.size &&
        paramTypeNames.zip(m.argTypes).forall(Function.tupled(matchesType(mangler, _, _)))

    def matchesReturnType(mangler: Mangler, m: Method): Boolean = retTypeName match {
      case None => m.returnType == IType.VoidType
      case Some(t) => matchesType(mangler, t, m.returnType)
    }

    def matchesType(mangler: Mangler, typName: String, mTyp: IType): Boolean = {
      mTyp.goTypeName(mangler) == (typName match {
        case "string" => IType.getObjectType("java/lang/String").goTypeName(mangler)
        case "int" => IType.IntType.goTypeName(mangler)
        case other => other
      })
    }

    def matchesMethodName(m: Method): Boolean =
      m.name.compareToIgnoreCase(ambigName) == 0 ||
        (m.name == "<init>" && ambigName.compareToIgnoreCase("init") == 0) ||
        (m.name == "<clinit>" && ambigName.compareToIgnoreCase("init") == 0)
  }

  object FunctionSig {
    def apply(line: String): FunctionSig = {
      // We only allow two sets of parens, which means non anon funcs and no tuple returns
      require(line.count(_ == ')') == 2, "Forwarding functions cannot have any func types or multi-type returns")
      val firstEndParen = line.indexOf(')')
      val paramStartParen = line.indexOf('(', firstEndParen)
      val paramEndParen = line.indexOf(')', paramStartParen)
      val name = line.substring(firstEndParen + 1, paramStartParen).trim
      val ambigName = name.lastIndexOf('_') match {
        case -1 => name
        case index =>
          // If it's all digits, we assume it's an overload ident
          val end = name.substring(index + 1)
          if (end.nonEmpty && end.forall(Character.isDigit)) name.substring(0, index) else name
      }
      val paramStr = line.substring(paramStartParen + 1, paramEndParen)
      val params = if (paramStr.isEmpty) Nil else paramStr.split(',').map({ param =>
        // Just get the type part without qualifier
        val trimmed = param.trim
        val from = trimmed.lastIndexOf(' ').max(trimmed.lastIndexOf('.'))
        trimmed.substring(from + 1)
      }).toSeq
      val retStr = line.substring(paramEndParen + 1, line.lastIndexOf('{')).trim
      val ret = if (retStr.isEmpty) None else Some(retStr.substring(retStr.lastIndexOf('.') + 1))
      FunctionSig(name, ambigName, params, ret)
    }
  }

  case class ForwardComment(
    instance: Boolean,
    cls: Cls,
    memberName: Option[String],
    memberDesc: Option[String]
  )
  object ForwardComment {
    def apply(classPath: ClassPath, line: String): ForwardComment = {
      val (str, instance) =
        if (line.startsWith("//goahead:forward-instance ")) line.drop(27).trim() -> true
        else if (line.startsWith("//goahead:forward-static ")) line.drop(25).trim() -> false
        else sys.error(s"Unrecognized goahead comment: $line")
      val (cls, (nameOpt, descOpt)) = str.indexOf("::") match {
        case -1 =>
          classPath.getFirstClass(str.replace('.', '/')).cls -> (None -> None)
        case colonIndex =>
          val cls = classPath.getFirstClass(line.substring(0, colonIndex).replace('.', '/')).cls
          line.indexOf('(', colonIndex) match {
            case -1 =>
              cls -> (Some(line.substring(colonIndex + 2)) -> None)
            case parenIndex =>
              cls -> (Some(line.substring(colonIndex + 2, parenIndex)) -> Some(line.substring(parenIndex)))
          }
      }
      ForwardComment(instance, cls, nameOpt, descOpt)
    }
  }
}

