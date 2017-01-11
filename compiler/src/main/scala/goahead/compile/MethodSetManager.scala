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
      // Can't include static, private, or private to another package
      val allMyMethods = allMethods(classPath, cls).filterNot { m =>
        m.access.isAccessStatic || m.access.isAccessPrivate ||
          (m.access.isAccessPackagePrivate && m.cls.packageName != cls.packageName)
      }
      val allMyPackagePrivateVisibilityIncreases = allMyMethods.myPackagePrivateVisibilityIncreases(cls).map(_._1)
      // All methods not in parent classes
      val parentDispatchInterfaces = classPath.allSuperTypes(cls.name).foldLeft(MethodSet(classPath, Nil)) {
        case (set, clsDet) => set ++ dispatchMethods(classPath, clsDet.cls)
      }
      // Exclude package private ones from the parent because we don't want to exclude them
      val toExclude = parentDispatchInterfaces.filterNot { m =>
        m.name != "<init>" && m.access.isAccessPackagePrivate && m.cls.packageName != cls.packageName
      }
      ((allMyMethods ++ defaultForwarders) -- toExclude) ++ MethodSet(classPath, allMyPackagePrivateVisibilityIncreases)
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
      val allMethodSet = allMethods(classPath, cls)
      // All of mine, non-static + all non-default-forwarder abstracts + all parent package priv increase
      allMyMethods(classPath, cls).filterNot(_.access.isAccessStatic) ++
        (allMethodSet.filterIncludingReturnDuplicates(_.access.isAccessAbstract) --
          defaultForwarderMethods(classPath, cls)) ++
        MethodSet(classPath, allMethodSet.myPackagePrivateVisibilityIncreases(cls).map(_._2))
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
          case (_, (dupes)) if dupes.primary.access.isAccessAbstract =>
            dupes.primary -> dupes.covariantReturnTypes
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
    private[MethodSetManager] val map: Map[(String, Seq[IType]), MethodDupes]
  ) {
    def methods = map.values.map(_.primary).toSeq.sortBy(m => m.name -> m.desc)

    def covariantReturnDuplicates = {
      // Sort inside and out
      map.values.toSeq.sortBy(m => m.primary.name -> m.primary.desc).map { case (dupes) =>
        dupes.primary -> dupes.covariantReturnTypes.sortBy(m => m.name -> m.desc)
      }
    }

    def methodsWithCovariantReturnDuplicates = {
      map.values.flatMap(m => m.primary +: m.covariantReturnTypes).toSeq.sortBy(m => m.name -> m.desc)
    }

    def methodsWithDupes = map.values

    def packagePrivateVisibilityIncreases = map.values.flatMap(_.packagePrivateVisibilityIncreases)

    def myPackagePrivateVisibilityIncreases(cls: Cls) = map.values.flatMap(_.myPackagePrivateVisibilityIncreases(cls))

    def allMethods = map.values.flatMap(m => m.primary +: (m.covariantReturnTypes ++ m.regularDupes))

    def `++`(other: MethodSet) = MethodSet(classPath, allMethods ++ other.allMethods)

    def `--`(other: MethodSet) = {
      val otherMethods = other.allMethods.map(m => m.name -> m.desc).toSet
      MethodSet(classPath, allMethods.filterNot(m => otherMethods.contains(m.name -> m.desc)))
    }

    def filter(fn: Method => Boolean) = MethodSet(classPath, allMethods.filter(fn))
    def filterNot(fn: Method => Boolean) = MethodSet(classPath, allMethods.filterNot(fn))

    // Filters primary method and non-matches are excluded along with their dupes
    def filterIncludingReturnDuplicates(fn: Method => Boolean) = copy(
      map = map.filter { case (_, (m)) => fn(m.primary) }
    )

    def prettyLines: Seq[String] = {
      @inline def methodStr(m: Method) = s"${m.cls.name}::${m.name}${m.desc}"
      map.values.flatMap({ m =>
        s"Method: ${methodStr(m.primary)}" +: (
          m.covariantReturnTypes.map(m => s"  Covariant Return Dupe: ${methodStr(m)}") ++
          m.regularDupes.map(m => s"  Regular Dupe: ${methodStr(m)}")
        )
      }).toSeq
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
        val differentReturnType = distinctDupes.filterNot(_.returnType == mostSpec.returnType).toSeq
        val sameReturnType = dupes.toSeq.diff(mostSpec +: differentReturnType)
        MethodDupes(
          primary = mostSpec,
          differentReturnType,
          sameReturnType
        )
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

  case class MethodDupes(
    primary: Method,
    covariantReturnTypes: Seq[Method],
    regularDupes: Seq[Method]
  ) {


    def myPackagePrivateVisibilityIncreases(cls: Cls): Option[(Method, Method)] = {
      packagePrivateVisibilityIncreases.filter(_._1.cls.name == cls.name)
    }

    def packagePrivateVisibilityIncreases: Option[(Method, Method)] = {
      val primaryNotInit = !primary.access.isAccessStatic && primary.name != "<init>"
      val mineIsVisible = primary.access.isAccessPublic || primary.access.isAccessProtected
      val noParentsAreVisible = !regularDupes.exists(m => m.access.isAccessPublic || m.access.isAccessProtected)
      val parentPackagePrivate = regularDupes.find(m =>
        m.access.isAccessPackagePrivate && m.cls.packageName == primary.cls.packageName
      )
      parentPackagePrivate match {
        case Some(p) if primaryNotInit && mineIsVisible && noParentsAreVisible => Some(primary -> p)
        case _ => None
      }
    }
  }
}