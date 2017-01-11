package goahead.compile.interop

import javax.lang.model.element.Modifier

import com.squareup.javapoet._
import goahead.interop.{GoField, GoFunction}

class StubBuilder {
  import StubBuilder._

  def build(importPath: String, targetPackageName: String, goPath: GoPath): Seq[JavaFile.Builder] = {
    val pkg = goParser.loadPackage(importPath, goPath)
    // TODO: this
    ???
  }

  protected def build(ctx: Context): (Context, Seq[JavaFile.Builder]) = {
    ???
  }

  protected def buildPackageIface(origCtx: Context, pkg: Package): (Context, TypeSpec.Builder) = {
    // All consts, vars, and funcs at a package level are in a Pkg class
    val bld = TypeSpec.classBuilder(origCtx.targetPackageName + ".Pkg").
      addModifiers(Modifier.PUBLIC)

    // Must have a private do nothing constructor to avoid instantiation
    bld.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())

    pkg.decls.foldLeft(origCtx -> bld) { case ((ctx, bld), decl) =>
      decl match {
        // Consts are static final whereas vars are just static
        case d: Package.ConstDecl =>
          buildField(ctx, "", d).map { case (ctx, field) =>
            ctx -> bld.addField(field.build())
          }
        case d: Package.VarDecl =>
          buildField(ctx, "", d).map { case (ctx, field) =>
            ctx -> bld.addField(field.build())
          }
        // Functions are just static
        case d: Package.FuncDecl =>
          buildMethod(ctx, "", d, static = true).map { case (ctx, method) =>
            ctx -> bld.addMethod(method.build())
          }
        case _ => ctx -> bld
      }
    }
  }

  protected def buildField(ctx: Context, parent: String, v: Package.VarMember): (Context, FieldSpec.Builder) = {
    ctx.safeMemberName(parent, v.name).map { case (ctx, safeName) =>
      ctx.typeRef(v.typ).map { case (ctx, typeRef) =>
        val field = FieldSpec.builder(typeRef, safeName, Modifier.PUBLIC, Modifier.STATIC)
        if (v.const) field.addModifiers(Modifier.FINAL)
        field.addAnnotation(AnnotationSpec.builder(classOf[GoField]).addMember("value", "$S", v.name).build())
        ctx -> field
      }
    }
  }

  protected def buildMethod(
    ctx: Context,
    parent: String,
    v: Package.FuncMember,
    static: Boolean
  ): (Context, MethodSpec.Builder) = {
    ctx.safeMemberName(parent, v.name).map { case (ctx, safeName) =>
      ctx.returnTypeRef(v.results).map { case (ctx, returnRef) =>
        val method = MethodSpec.methodBuilder(safeName).
          addModifiers(Modifier.PUBLIC, Modifier.NATIVE).
          addAnnotation(AnnotationSpec.builder(classOf[GoFunction]).addMember("value", "$S", v.name).build()).
          varargs(v.variadic).
          returns(returnRef)
        if (static) method.addModifiers(Modifier.STATIC)
        v.params.zipWithIndex.foldLeft(ctx -> method) { case ((ctx, method), (Package.NamedType(nameOpt, typ), idx)) =>
          ctx.typeRef(typ).map { case (ctx, typRef) =>
            val ctxAndName = nameOpt match {
              case None => ctx -> s"param$idx"
              case Some(name) => ctx.safeMemberName(s"$parent.${v.name}", name)
            }
            ctxAndName.map { case (ctx, name) =>
              ctx -> method.addParameter(typRef, name)
            }
          }
        }
      }
    }
  }

  protected def goParser: GoParser = GoParser
}

object StubBuilder {
  val keywordFixMap = Map(

  )

  case class Context(
    pkg: Package,
    goPath: GoPath,
    targetPackageName: String,
    // Keyed by declarator (which is an empty string for package level)
    usedNames: Map[String, Set[String]] = Map.empty,
    // Key is declarator + "." + go name, value is Java name
    //localNames: Map[String, String] = Map.empty,
    // Key is go name, value is java name
    localDeclNames: Map[String, String] = Map.empty,
    mangler: Mangler
  )

  implicit class RichContext(val ctx: Context) extends AnyVal {
    def safeMemberName(parent: String, name: String): (Context, String) = {
      val myUsedNames = ctx.usedNames.getOrElse(parent, Set.empty)
      val ret = Iterator.from(1).
        map(i => ctx.mangler.javaMemberName(name + (if (i == 1) i.toString else ""))).
        find(s => !myUsedNames.contains(s)).get
      ctx.copy(
        usedNames = ctx.usedNames + (parent -> (myUsedNames + ret))
      ) -> ret
    }

    @inline
    def map[U](f: Context => U): U = f(ctx)

    def returnTypeRef(typ: Seq[Package.NamedType]): (Context, TypeName) = {
      ???
    }

    def typeRef(typ: Package.Type): (Context, TypeName) = {
      typ match {
        case Package.QualifiedType(None, name) =>
          val localName = ctx.localDeclNames.getOrElse(name, sys.error(s"Unable to find type $name"))
          ctx -> ClassName.get(ctx.targetPackageName, localName)
        case Package.QualifiedType(Some(otherPkg), name) =>
          ctx -> ctx.goPath.getPackage(otherPkg).javaTypeName(name)
        case _: Package.FuncType =>
          sys.error("Function types not yet supported")
        case _ =>
          ???
      }
      ???
    }
  }

  implicit class RichTuple[A, B](val tuple: (A, B)) extends AnyVal {
    @inline
    def map[C](f: (A, B) => C): C = f.tupled(tuple)
  }
}