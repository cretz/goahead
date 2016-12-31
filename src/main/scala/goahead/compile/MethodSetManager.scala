package goahead.compile
import Helpers._

// TODO: slow...memoize please
trait MethodSetManager {
  import MethodSetManager._

  def dispatchInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet
  def dispatchForwardingMethods(classPath: ClassPath, cls: Cls): MethodSet
  def staticMethods(classPath: ClassPath, cls: Cls): MethodSet
  def instInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet
  def instInterfaceDefaultMethods(classPath: ClassPath, cls: Cls): MethodSet
  def implMethods(classPath: ClassPath, cls: Cls): MethodSet
  def implDefaultForwarderMethods(classPath: ClassPath, cls: Cls): MethodSet

  // None if not a functional interface
  def functionalInterfaceMethod(classPath: ClassPath, cls: Cls): Option[Method]
  def functionalInterfaceMethodWithDupes(classPath: ClassPath, cls: Cls): Option[(Method, Seq[Method])]
}

object MethodSetManager {

  object Default extends Default
  trait Default extends MethodSetManager {
    // All super interfaces and types
    protected def allSuperMethods(classPath: ClassPath, classes: Cls*): MethodSet = {
      val supers = classes.flatMap(c => classPath.allSuperAndImplementingTypes(c.name).map(_.cls))
      MethodSet(classPath, supers.flatMap(_.methods))
    }

    protected def allMyMethods(classPath: ClassPath, classes: Cls*): MethodSet = {
      MethodSet(classPath, classes.flatMap(_.methods))
    }

    // All super interfaces and types starting with the direct parent
    protected def allParentMethods(classPath: ClassPath, classes: Cls*): MethodSet = {
      allMethods(classPath, classes.flatMap(_.parent.map(classPath.getFirstClass(_).cls)):_*)
    }

    protected def allMethods(classPath: ClassPath, classes: Cls*): MethodSet = {
      allMyMethods(classPath, classes:_*) ++ allSuperMethods(classPath, classes:_*)
    }

    protected def dispatchMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      // Includes default forwarder methods
      val defaultForwarders = defaultForwarderMethods(classPath, cls)
      // All methods not in parent classes and not static and not private
      val parentDispatchInterfaces = classPath.allSuperTypes(cls.name).foldLeft(MethodSet(classPath, Nil)) {
        case (set, clsDet) => set ++ dispatchMethods(classPath, clsDet.cls)
      }
      (allMethods(classPath, cls).filterNot(m => m.access.isAccessStatic || m.access.isAccessPrivate) ++
        defaultForwarders) -- parentDispatchInterfaces
    }

    override def dispatchInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      dispatchMethods(classPath, cls)
    }

    override def dispatchForwardingMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      dispatchMethods(classPath, cls)
    }

    override def staticMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      allMyMethods(classPath, cls).filter(_.access.isAccessStatic)
    }

    override def instInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      // Non-private, non-init, non-static including parents
      allMethods(classPath, cls).
        filterNot(m => m.access.isAccessPrivate || m.name == "<init>" || m.access.isAccessStatic)
    }

    override def instInterfaceDefaultMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      MethodSet(classPath, cls.methods.filter(m => !m.access.isAccessStatic && !m.access.isAccessAbstract))
    }

    override def implMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      // All of mine, non-static + all non-default-forwarder abstracts
      allMyMethods(classPath, cls).filterNot(_.access.isAccessStatic) ++
        (allMethods(classPath, cls).filterIncludingReturnDuplicates(_.access.isAccessAbstract) --
          defaultForwarderMethods(classPath, cls))
    }

    protected def defaultForwarderMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      // All interface defaults that neither I nor my parent doesn't implement in any way
      val allInterfaces = classPath.allSuperAndImplementingTypes(cls.name).filter(_.cls.access.isAccessInterface)
      val allInterfaceDefaults = MethodSet(classPath, allInterfaces.flatMap(_.cls.methods).
        filterNot(m => m.access.isAccessStatic || m.access.isAccessAbstract))
      allInterfaceDefaults -- (allMyMethods(classPath, cls) ++ allParentMethods(classPath, cls))
    }

    override def implDefaultForwarderMethods(classPath: ClassPath, cls: Cls): MethodSet = {
      defaultForwarderMethods(classPath, cls)
    }

    override def functionalInterfaceMethod(classPath: ClassPath, cls: Cls): Option[Method] = {
      functionalInterfaceMethodWithDupes(classPath, cls).map(_._1)
    }

    override def functionalInterfaceMethodWithDupes(classPath: ClassPath, cls: Cls): Option[(Method, Seq[Method])] = {
      // Can only have one that is abstract and not part of object
      if (!cls.access.isAccessInterface) None else {
        // All methods not in the object class
        val all = allMethods(classPath, cls) -- allMethods(classPath, classPath.getFirstClass("java/lang/Object").cls)
        val methodPossibles = all.map.toSeq.collect {
          case (_, (meth, dupes)) if meth.access.isAccessAbstract => meth -> dupes.toSeq
        }
        if (methodPossibles.size != 1) None else methodPossibles.headOption
      }
    }
  }

  abstract class Filtered(underlying: MethodSetManager = Default) extends MethodSetManager {

    def filter(method: Method, forImpl: Boolean): Boolean

    def dispatchInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.dispatchInterfaceMethods(classPath, cls).filter(filter(_, forImpl = false))
    def dispatchForwardingMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.dispatchForwardingMethods(classPath, cls).filter(filter(_, forImpl = false))
    def staticMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.staticMethods(classPath, cls).filter(filter(_, forImpl = true))
    def instInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.instInterfaceMethods(classPath, cls).filter(filter(_, forImpl = false))
    def instInterfaceDefaultMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.instInterfaceDefaultMethods(classPath, cls).filter(filter(_, forImpl = false))
    def implMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.implMethods(classPath, cls).filter(filter(_, forImpl = true))
    def implDefaultForwarderMethods(classPath: ClassPath, cls: Cls): MethodSet =
      underlying.implDefaultForwarderMethods(classPath, cls).filter(filter(_, forImpl = false))

    override def functionalInterfaceMethod(classPath: ClassPath, cls: Cls) =
      underlying.functionalInterfaceMethod(classPath, cls).filter(filter(_, forImpl = false))
    override def functionalInterfaceMethodWithDupes(classPath: ClassPath, cls: Cls) =
      underlying.functionalInterfaceMethodWithDupes(classPath, cls).filter(v => filter(v._1, forImpl = false))
  }

  case class MethodSet private(
    private val classPath: ClassPath,
    // Keyed by name then arg type, value is actual -> covariant dupes
    private[MethodSetManager] val map: Map[(String, Seq[IType]), (Method, Iterable[Method])]
  ) {
    def methods = map.values.map(_._1).toSeq.sortBy(m => m.name -> m.desc)

    def covariantReturnDuplicates = {
      // Sort inside and out
      map.values.toSeq.sortBy(m => m._1.name -> m._1.desc).map { case (m, dupes) =>
        m -> dupes.toSeq.sortBy(m => m.name -> m.desc)
      }
    }

    def methodsWithCovariantReturnDuplicates = allMethods.toSeq.sortBy(m => m.name -> m.desc)

    private def allMethods = map.values.flatMap(v => Iterable(v._1) ++ v._2)

    def `++`(other: MethodSet) = MethodSet(classPath, allMethods ++ other.allMethods)

    def `--`(other: MethodSet) = {
      val otherMethods = other.allMethods.map(m => m.name -> m.desc).toSet
      MethodSet(classPath, allMethods.filterNot(m => otherMethods.contains(m.name -> m.desc)))
    }

    def filter(fn: Method => Boolean) = MethodSet(classPath, allMethods.filter(fn))
    def filterNot(fn: Method => Boolean) = MethodSet(classPath, allMethods.filterNot(fn))

    // Filters primary method and non-matches are excluded along with their dupes
    def filterIncludingReturnDuplicates(fn: Method => Boolean) = copy(
      map = map.filter { case (_, (m, _)) => fn(m) }
    )

    def prettyLines: Seq[String] = {
      covariantReturnDuplicates.flatMap { case (m, dupes) =>
        s"Method: ${m.cls.name}::${m.name}${m.desc}" +: dupes.map { m =>
          s"  Dupe: ${m.cls.name}::${m.name}${m.desc}"
        }
      }
    }
  }

  object MethodSet {

    def apply(classPath: ClassPath, methods: Iterable[Method]): MethodSet = {
      val byNameAndArgs = methods.groupBy(m => m.name -> m.argTypes)
      MethodSet(classPath, byNameAndArgs.mapValues { dupes =>
        // We have to get the most specific by return type too here
        val distinctDupes = dupes.groupBy(_.returnType).values.map(_.reduce(mostSpecific(classPath, _, _)))
        // Now, of all of the dupes, which is the most specific?
        val mostSpec = distinctDupes.reduce(mostSpecific(classPath, _, _))
        // All others are just dupes
        mostSpec -> distinctDupes.filterNot(_.returnType == mostSpec.returnType).toSeq
      })
    }

    def mostSpecific(classPath: ClassPath, m1: Method, m2: Method): Method = {
      // Most specific covariant if return types are not the same
      if (m1.returnType != m2.returnType) {
        if (m1.returnType.isAssignableFrom(classPath, m2.returnType)) m2 else m1
      // Non-interface preferred, then just prefer the first (so, yes, order of input matters)
      } else if (m1.access.isAccessInterface != m2.access.isAccessInterface) {
        if (m1.access.isAccessInterface) m2 else m1
      } else {
        m1
      }
    }
  }
}