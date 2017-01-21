
* Check if discriminator is needed on interfaces to prevent ambiguity or if the raw accessor is enough
* Use external testing libs to validate work. There are many out there, but the OpenJDK ones are good start
* Reduce exports for package private things and types
* Respect final fields by removing setters
* Use friendly param and local var names based on debug-level info if available
* Complete the internal reflection support
* Fix the node writer's unary/binary op precedent/spacing code to ensure exact go fmt match
* Documentation, including:
  * Guide on usage including full configuration documentation
  * Tutorial that walks through building a project
  * Guide for alt JVM langs that shows how to build their runtimes and run hello world
  * Internal, detailed guide on what code is generated and why we made decisions we did
* CLIs, in addition to the existing compile, for:
  * Building exe
  * Compiling main class
* Fill out the stdlib with needed forwarders and impls for all sorts of things, e.g.:
    * Multi-charset
    * Reflection
    * IO
    * Threading
    * Lambda factory (I don't know that we can do this)
    * Serialization
* Investigate approaches to trim the binary
* Investigate approaches to speed up Go compilation (it's really really slow)
* Investigate approaches to improve runtime performance (lowest priority)
* Gradle/sbt/maven plugin?
* Interop:
  * Support accessing opaque Go types and method calls and field access (but that's it)
  * Consider supporting the whole of Go's features as JVM libs
  * Stub generator that generates the Java code representing a Go package
* Prepare for friendly-name use by aliases in near future: https://github.com/golang/proposal/blob/2d94347db73662f48efdec005d4fe57d28f51485/design/18130-type-alias.md