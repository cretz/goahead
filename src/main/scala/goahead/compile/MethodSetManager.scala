package goahead.compile
import Helpers._

// TODO: slow...memoize please
trait MethodSetManager {
  import MethodSetManager._

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

  def dispatchInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet = {
    // Includes default forwarder methods
    val defaultForwarders = implDefaultForwarderMethods(classPath, cls)
    // All methods not in parent classes and not static
    val parentDispatchInterfaces = classPath.allSuperTypes(cls.name).foldLeft(MethodSet(classPath, Nil)) {
      case (set, clsDet) => set ++ dispatchInterfaceMethods(classPath, clsDet.cls)
    }
    (allMethods(classPath, cls).filterNot(_.access.isAccessStatic) ++ defaultForwarders) -- parentDispatchInterfaces
  }

  def dispatchForwardingMethods(classPath: ClassPath, cls: Cls): MethodSet = {
    // Only my new methods and default forwarders
    allMyMethods(classPath, cls).filterNot(_.access.isAccessStatic) ++ implDefaultForwarderMethods(classPath, cls)
  }

  def staticMethods(classPath: ClassPath, cls: Cls): MethodSet = {
    allMyMethods(classPath, cls).filter(_.access.isAccessStatic)
  }

  def instInterfaceMethods(classPath: ClassPath, cls: Cls): MethodSet = {
    // Non-private, non-init, non-static including parents
    allMethods(classPath, cls).filterNot(m => m.access.isAccessPrivate || m.name == "<init>" || m.access.isAccessStatic)
  }

  def implMethods(classPath: ClassPath, cls: Cls): MethodSet = {
    // Only mine, non-static
    allMyMethods(classPath, cls).filterNot(_.access.isAccessStatic)
  }

  def implDefaultForwarderMethods(classPath: ClassPath, cls: Cls): MethodSet = {
    // All interface defaults that my parent doesn't implement in any way
    val allInterfaceDefaults = MethodSet(classPath, classPath.allInterfaceTypes(cls.name).flatMap(_.cls.methods).
      filterNot(m => m.access.isAccessStatic || m.access.isAccessAbstract))
    allInterfaceDefaults -- allParentMethods(classPath, cls)
  }
}

object MethodSetManager extends MethodSetManager {

  case class MethodSet private(
    private val classPath: ClassPath,
    // Keyed by name then arg type, value is actual -> covariant dupes
    private val map: Map[(String, Seq[IType]), (Method, Iterable[Method])]
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
      // Non-interface preferred, then non abstract
      } else if (m1.access.isAccessInterface != m2.access.isAccessInterface) {
        if (m1.access.isAccessInterface) m2 else m1
      } else {
        if (m1.access.isAccessAbstract) m2 else m1
      }
    }
  }
}