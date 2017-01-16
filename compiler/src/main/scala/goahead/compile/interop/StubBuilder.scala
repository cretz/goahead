package goahead.compile.interop

import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier

import com.squareup.javapoet._
import goahead.interop._

class StubBuilder {
  import StubBuilder._

  def build(importPath: String, targetPackageName: String, goPath: GoPath): Seq[JavaFile.Builder] = {
    val ctx = Context(
      pkg = goParser.loadPackage(importPath, goPath),
      goPath = goPath,
      targetPackageName = targetPackageName
    )
    build(ctx).map { case (_, files) => files }
  }

  protected def build(ctx: Context): (Context, Seq[JavaFile.Builder]) = {
    buildTopLevel(ctx).map { case (ctx, topLevel) =>
      ???
    }
  }

  protected def buildTopLevel(ctx: Context): (Context, TypeSpec.Builder) = {
    // All consts, vars, and funcs at a package level are in a Pkg class
    val bld = TypeSpec.classBuilder(ctx.targetPackageName + ".Pkg").
      addModifiers(Modifier.PUBLIC, Modifier.FINAL)

    ctx.pkg.decls.foldLeft(ctx -> bld) { case ((ctx, bld), decl) =>
      decl match {
        // Consts are static final whereas vars are just static
        case d: Package.ConstDecl =>
          buildField(ctx, "", d).map { case (ctx, field) =>
            ctx -> bld.addField(field.addModifiers(Modifier.STATIC, Modifier.FINAL).build())
          }
        case d: Package.VarDecl =>
          buildField(ctx, "", d).map { case (ctx, field) =>
            ctx -> bld.addField(field.addModifiers(Modifier.STATIC).build())
          }
        // Functions are just static native
        case d: Package.FuncDecl =>
          buildMethod(ctx, "", d).map { case (ctx, method) =>
            ctx -> bld.addMethod(method.addModifiers(Modifier.STATIC, Modifier.NATIVE).build())
          }
        case _ => ctx -> bld
      }
    }
  }

  protected def buildTypeDecls(ctx: Context): (Context, Seq[TypeSpec.Builder]) = {
    ctx.pkg.decls.foldLeft(ctx -> Seq.empty[TypeSpec.Builder]) { case ((ctx, blds), decl) =>
      decl match {
        case _: Package.StructDecl =>
          ???
        case _ => ctx -> blds
      }
    }
  }

  protected def buildStructDecl(ctx: Context, struct: Package.StructDecl): (Context, TypeSpec.Builder) = {
    // Structs have embeds as fields, fields as fields, and methods as methods
    ctx.safeClassName("", struct.name).map { case (ctx, structName) =>
      val bld = TypeSpec.classBuilder(s"${ctx.targetPackageName}.$structName")
      val ctxAndBld = struct.fields.foldLeft(ctx -> bld) { case ((ctx, bld), field) =>
        // Create var decl from field
        val decl = Package.VarDecl(
          name = field.name.getOrElse {
            field.typ match {
              case Package.QualifiedType(_, name) => name
              case _ => sys.error(s"In ${struct.name} expected qualified embedded type, got: ${field.typ}")
            }
          },
          typ = field.typ
        )
        buildField(ctx, struct.name, decl).map { case (ctx, fieldBld) =>
          ctx -> bld.addField(fieldBld.build())
        }
      }
      struct.methods.foldLeft(ctxAndBld) { case ((ctx, bld), method) =>
        buildMethod(ctx, struct.name, method).map { case (ctx, methodBld) =>
          ctx -> bld.addMethod(methodBld.addModifiers(Modifier.NATIVE).build())
        }
      }
    }
  }

  protected def buildInterfaceDecl(ctx: Context, iface: Package.IfaceDecl): (Context, TypeSpec.Builder) = {
    // Interfaces simply extend embeds and add methods they contain
    ctx.safeClassName("", iface.name).map { case (ctx, ifaceName) =>
      val bld = TypeSpec.interfaceBuilder(s"${ctx.targetPackageName}.$ifaceName")
      val ctxAndBld = iface.embedded.foldLeft(ctx -> bld) { case ((ctx, bld), embedded) =>
        ctx.typeRef(embedded).map { case (ctx, embeddedRef) =>
          ctx -> bld.addSuperinterface(embeddedRef)
        }
      }
      iface.methods.foldLeft(ctxAndBld) { case ((ctx, bld), method) =>
        buildMethod(ctx, iface.name, method).map { case (ctx, methodBld) =>
          ctx -> bld.addMethod(methodBld.build())
        }
      }
    }
  }

  protected def buildField(ctx: Context, parent: String, v: Package.VarMember): (Context, FieldSpec.Builder) = {
    ctx.safeMemberName(parent, v.name).map { case (ctx, safeName) =>
      ctx.typeRef(v.typ).map { case (ctx, typeRef) =>
        val field = FieldSpec.builder(typeRef, safeName, Modifier.PUBLIC)
        if (v.const) field.addModifiers(Modifier.FINAL)
        field.addAnnotation(AnnotationSpec.builder(classOf[GoField]).addMember("value", "$S", v.name).build())
        ctx -> field
      }
    }
  }

  protected def buildMethod(ctx: Context, parent: String, v: Package.FuncMember): (Context, MethodSpec.Builder) = {
    ctx.safeMemberName(parent, v.name).map { case (ctx, safeName) =>
      ctx.returnTypeRef(v.results).map { case (ctx, returnRef) =>
        val method = MethodSpec.methodBuilder(safeName).
          addModifiers(Modifier.PUBLIC).
          addAnnotation(AnnotationSpec.builder(classOf[GoFunction]).addMember("value", "$S", v.name).build()).
          varargs(v.variadic).
          returns(returnRef)
        v.params.zipWithIndex.foldLeft(ctx -> method) { case ((ctx, method), (Package.NamedType(nameOpt, typ), idx)) =>
          ctx.typeRef(typ).map { case (ctx, typRef) =>
            val ctxAndName = nameOpt match {
              case None => ctx -> s"param$idx"
              case Some(name) => ctx.safeMemberName(s"$parent.${v.name}", name)
            }
            ctxAndName.map { case (ctx, name) =>
              val param = ParameterSpec.builder(typRef, name)
              ctx -> method.addParameter(param.build())
            }
          }
        }
      }
    }
  }

  protected def goParser: GoParser = GoParser
}

object StubBuilder {

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
    mangler: Mangler = Mangler.Default
  )

  implicit class RichContext(val ctx: Context) extends AnyVal {

    def safeName(parent: String, name: String, mangle: String => String): (Context, String) = {
      val myUsedNames = ctx.usedNames.getOrElse(parent, Set.empty)
      val ret = Iterator.from(1).
        map(i => mangle(name + (if (i == 1) i.toString else ""))).
        find(s => !myUsedNames.contains(s) && !SourceVersion.isKeyword(s)).get
      ctx.copy(
        usedNames = ctx.usedNames + (parent -> (myUsedNames + ret))
      ) -> ret
    }

    def safeClassName(parent: String, name: String): (Context, String) = {
      safeName(parent, name, ctx.mangler.javaClassName)
    }

    def safeMemberName(parent: String, name: String): (Context, String) = {
      safeName(parent, name, ctx.mangler.javaMemberName)
    }

    @inline
    def map[U](f: Context => U): U = f(ctx)

    def returnTypeRef(typ: Seq[Package.NamedType]): (Context, TypeName) = {
      typ match {
        // If there's only one, who cares what the name is, we just use a normal type
        case Seq(Package.NamedType(_, typ)) =>
          typeRef(typ)
        case tuple =>
          // TODO: live-decl tuple
          sys.error("Tuples not yet supported in return types")
      }
    }

    def typeRef(typ: Package.Type): (Context, TypeName) = {
      typ match {
        case Package.QualifiedType(None, name) =>
          val localName = ctx.localDeclNames.getOrElse(name, sys.error(s"Unable to find type $name"))
          ctx -> ClassName.get(ctx.targetPackageName, localName)
        case Package.QualifiedType(Some(otherPkg), name) =>
          ctx -> ctx.goPath.getPackage(otherPkg).javaClassName(name)
        case _: Package.FuncType =>
          // TODO: live-decl functional interface
          sys.error("Function types not yet supported")
        case t: Package.BasicType =>
          ctx -> ctx.goPath.builtInPackage.javaClassName(t.name)
        case Package.ArrayType(size, elemType) =>
          typeRef(elemType).map { case (ctx, elemTypeRef) =>
            val sizeAnn = AnnotationSpec.builder(classOf[ArraySize]).
              addMember("value", "$L", java.lang.Long.valueOf(size)).build()
            ctx -> ParameterizedTypeName.get(ClassName.get(classOf[builtin.Array[_]]), elemTypeRef.annotated(sizeAnn))
          }
        case Package.SliceType(elemType) =>
          typeRef(elemType).map { case (ctx, elemTypeRef) =>
            ctx -> ParameterizedTypeName.get(ClassName.get(classOf[builtin.Slice[_]]), elemTypeRef)
          }
        case _: Package.StructType =>
          // TODO: live-decl anon struct
          sys.error("Anonymous struct types not yet supported")
        case Package.PointerType(elemType) =>
          typeRef(elemType).map { case (ctx, elemTypeRef) =>
            ctx -> ParameterizedTypeName.get(ClassName.get(classOf[builtin.Pointer[_]]), elemTypeRef)
          }
        case _: Package.IfaceType =>
          // TODO: live-decl anon iface
          sys.error("Anonymous interface types not yet supported")
        case Package.MapType(keyType, valType) =>
          typeRef(keyType).map { case (ctx, keyTypeRef) =>
            typeRef(valType).map { case (ctx, valTypeRef) =>
              ctx -> ParameterizedTypeName.get(ClassName.get(classOf[builtin.Map[_, _]]), keyTypeRef, valTypeRef)
            }
          }
        case Package.ChanType(elemType, send, receive) =>
          typeRef(elemType).map { case (ctx, elemTypeRef) =>
            var dirs = Array.empty[ChanDir.Dir]
            if (send) dirs :+= ChanDir.Dir.SEND
            if (receive) dirs :+= ChanDir.Dir.RECEIVE
            val dirAnn = AnnotationSpec.builder(classOf[ChanDir]).addMember("value", "$L", dirs).build()
            ctx -> ParameterizedTypeName.get(ClassName.get(classOf[builtin.Channel[_]]), elemTypeRef.annotated(dirAnn))
          }
      }
    }
  }

  implicit class RichTuple[A, B](val tuple: (A, B)) extends AnyVal {
    @inline
    def map[C](f: (A, B) => C): C = f.tupled(tuple)
  }
}