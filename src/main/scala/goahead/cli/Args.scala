package goahead.cli

import scala.annotation.tailrec
import scala.util.Try

object Args {

  def validated[T](args: Seq[String], f: (Builder) => T): Either[Failure, T] = {
    val builder = new Builder(args)
    val result = f(builder)
    
    // If -help is anywhere in there, we show the usage but no errors
    if (builder.args.contains("-help")) Left(Failure(Nil, builder.usageStartingWithArgs())) else {
      var errStrs = builder.errs.map(_.getMessage)
      if (builder.args.nonEmpty) {
        errStrs :+= "Unrecognized args: " + builder.args.mkString(", ")
        builder.args = Nil
      }
      if (errStrs.nonEmpty) Left(Failure(errStrs, builder.usageStartingWithArgs())) else Right(result)
    }
  }

  case class Failure(errs: Seq[String], usageStartingWithArgs: String)

  case class ArgError(arg: String, err: String, cause: Throwable = null) extends Exception(s"$arg: $err", cause)

  sealed trait Opt[T] {

    protected def argNames(name: String, aliases: Seq[String]) = (name +: aliases).map('-' + _)

    def name: String

    def aliases: Seq[String]

    def allNamesAndAliases = name +: aliases

    def required: Boolean

    def default: T

    def get(implicit builder: Builder): T = Try(validatedGet(builder)).recover({ case err =>
      err match {
        case e: ArgError => builder.errs :+= e
        case e => builder.errs :+= ArgError(name, e.getMessage, e)
      }
      default
    }).get

    def map[U](f: T => U)(implicit builder: Builder): U = f(get)

    protected def validatedGet(builder: Builder): T
  }

  object Opt {
    case class Flag(
      name: String,
      aliases: Seq[String] = Nil,
      desc: String = ""
    ) extends Opt[Boolean] {
      override def required = false
      override def default = false

      override def validatedGet(builder: Builder): Boolean = {
        val names = argNames(name, aliases)
        val (withNames, without) = builder.args.partition(names.contains)
        if (withNames.length > 1) throw ArgError(name, "Many of the same value found: " + withNames.mkString(", "))
        builder.args = without
        withNames.nonEmpty
      }
    }

    case class ValueOpt(
      name: String,
      aliases: Seq[String] = Nil,
      desc: String = "",
      required: Boolean = false,
      default: String = ""
    ) extends Opt[String] {
      override def validatedGet(builder: Builder): String = {
        val vals = builder.findOpt(argNames(name, aliases))
        if (vals.isEmpty && required) throw ArgError(name, "Required, not present")
        else if (vals.isEmpty) default
        else if (vals.length > 1) throw ArgError(name, "Only single value allowed")
        else vals.head
      }
    }

    case class MultiOpt(
      name: String,
      aliases: Seq[String] = Nil,
      desc: String = "",
      required: Boolean = false,
      default: Seq[String] = Nil
    ) extends Opt[Seq[String]] {
      override def validatedGet(builder: Builder): Seq[String] = {
        val vals = builder.findOpt(argNames(name, aliases))
        if (vals.isEmpty && required) throw ArgError(name, "Required, not present")
        else if (vals.isEmpty) default
        else vals
      }
    }

    case class Trailing(
      name: String,
      desc: String = "",
      required: Boolean = false,
      default: String = "",
      includePossibleFlags: Boolean = false
    ) extends Opt[String] {
      override def aliases = Nil
      override def validatedGet(builder: Builder): String = {
        val vals = builder.findTrailing(includePossibleFlags)
        if (vals.isEmpty && required) throw ArgError(name, "Required, not present")
        else if (vals.isEmpty) default
        else if (vals.length > 1) throw ArgError(name, "Only single value allowed")
        else vals.head
      }
    }

    case class MultiTrailing(
      name: String,
      desc: String = "",
      required: Boolean = false,
      default: Seq[String] = Nil,
      includePossibleFlags: Boolean = false
    ) extends Opt[Seq[String]] {
      override def aliases = Nil
      override def validatedGet(builder: Builder): Seq[String] = {
        val vals = builder.findTrailing(includePossibleFlags)
        if (vals.isEmpty && required) throw ArgError(name, "Required, not present")
        else if (vals.isEmpty) default
        else vals
      }
    }
  }

  class Builder(private[Args] var args: Seq[String]) {
    private[Args] var opts = Seq.empty[Opt[_]]
    private[Args] var errs = Seq.empty[ArgError]

    private[Args] def addOpt[T <: Opt[_]](opt: T): T = {
      opts :+= opt
      opt
    }

    private[Args] def findOpt(names: Seq[String]): Seq[String] = {
      @tailrec
      def nextOpt(names: Seq[String], acc: Seq[String]): Seq[String] = {
        args.indexWhere(names.contains) match {
          case -1 => acc
          case index =>
            require(index < args.length - 1, "No value for opt")
            val v = args(index + 1)
            args = args.take(index) ++ args.drop(index + 2)
            nextOpt(names, acc :+ v)
        }
      }
      nextOpt(names, Nil)
    }

    private[Args] def findTrailing(includePossibleFlags: Boolean): Seq[String] = {
      val (found, leftover, _) = args.foldLeft((Seq.empty[String], Seq.empty[String], false)) {
        case ((found, leftover, ignoreNext), arg) =>
          if (ignoreNext) (found, leftover :+ arg, false)
          else if (!includePossibleFlags && arg.startsWith("-")) (found, leftover :+ arg, true)
          else (found :+ arg, leftover, false)
      }
      args = leftover
      found
    }

    private[Args] def usageStartingWithArgs(): String = {
      val optsTrailingLast = opts.zipWithIndex.sortBy({
        case (_: Opt.Trailing, index) => (index + 1) * 1000
        case (_: Opt.MultiTrailing, index) => (index + 1) * 1000
        case (_, index) => index
      }).map(_._1)
      val strPieces = optsTrailingLast.map { opt =>
        @inline
        def req(v: String) = if (!opt.required) s"[$v]" else v
        @inline
        def optName = opt.allNamesAndAliases.mkString("|")
        opt match {
          case o: Opt.Flag => (req(s"-$optName"), s"-$optName", o.desc)
          case o: Opt.ValueOpt => (req(s"-$optName <value>"), s"-$optName", o.desc)
          case o: Opt.MultiOpt => (req(s"-$optName <value> [...]"), s"-$optName", o.desc)
          case o: Opt.Trailing => (req(s"<$optName>"), s"<$optName>", o.desc)
          case o: Opt.MultiTrailing => (req(s"<$optName>..."), s"<$optName>", o.desc)
        }
      }

      def wordWrap(str: String, maxLineLength: Int): Seq[String] = {
        str.split(' ').foldLeft(Seq("")) { case (lines, word) =>
          if (lines.last.length + word.length + 1 > maxLineLength) lines :+ word
          else lines.init :+ (lines.last + " " + word).trim
        }
      }


      strPieces.map(_._1).mkString(" ") + "\n\n" + strPieces.map({ case (_, name, desc) =>
        name + "\n  " + wordWrap(desc, 78).mkString("\n  ")
      }).mkString("\n\n")
    }

    def flag(
      name: String,
      aliases: Seq[String] = Nil,
      desc: String = ""
    ) = addOpt(Opt.Flag(name, aliases, desc))

    def opt(
      name: String,
      aliases: Seq[String] = Nil,
      desc: String = "",
      required: Boolean = false,
      default: String = ""
    ) = addOpt(Opt.ValueOpt(name, aliases, desc, required, default))

    def opts(
      name: String,
      aliases: Seq[String] = Nil,
      desc: String = "",
      required: Boolean = false,
      default: Seq[String] = Nil
    ) = addOpt(Opt.MultiOpt(name, aliases, desc, required, default))

    def trailingOpt(
      name: String,
      desc: String = "",
      required: Boolean = false,
      default: String = ""
    ) = addOpt(Opt.Trailing(name, desc, required, default))

    def trailingOpts(
      name: String,
      desc: String = "",
      required: Boolean = false,
      default: Seq[String] = Nil
    ) = addOpt(Opt.MultiTrailing(name, desc, required, default))
  }
}
