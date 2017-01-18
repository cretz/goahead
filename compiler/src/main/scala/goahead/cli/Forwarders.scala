package goahead.cli

import java.nio.file.Paths

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

object Forwarders {
  import Helpers._

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
                forwarder.copy(methods = forwarder.methods + ForwarderMethod(methods.head, sig.name))
            }
            methods
        }
      }
    }

    // Read all files out of the target, looking for "//goahead-forwarder"
    Paths.get(path).toFile.listFiles().foreach { f =>
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

  case class ForwarderMethod(from: Method, targetName: String)

  case class FunctionSig(
    name: String,
    ambigName: String,
    paramTypeNames: Seq[String],
    retTypeName: Option[String]
  ) {
    def matchesMethod(mangler: Mangler, m: Method): Boolean = {
      if (!matchesMethodName(m.name)) false
      else if (paramTypeNames.size != m.argTypes.size) false
      else if (m.returnType == IType.VoidType && retTypeName.nonEmpty) false
      else if (m.returnType != IType.VoidType && retTypeName.isEmpty) false
      else if (m.argTypes.map(_.goTypeName(mangler)) != paramTypeNames) false
      else if (m.returnType != IType.VoidType &&
        !retTypeName.contains(m.returnType.goTypeName(mangler))) false
      else true
    }

    def matchesMethodName(methodName: String): Boolean = {
      methodName.compareToIgnoreCase(ambigName) == 0 ||
        (methodName == "<init>" && ambigName.compareToIgnoreCase("init") == 0) ||
        (methodName == "<clinit>" && ambigName.compareToIgnoreCase("init") == 0)
    }
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

