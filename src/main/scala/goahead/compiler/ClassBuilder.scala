package goahead.compiler

import org.objectweb.asm._

class ClassBuilder(val classPath: ClassPath) extends ClassVisitor(Opcodes.ASM5) {
  override def visit(
    version: Int,
    access: Int,
    name: String,
    signature: String,
    superName: String,
    interfaces: Array[String]
  ): Unit = {
    ???
  }

  override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = {
    ???
  }

  override def visitAttribute(attr: Attribute): Unit = {
    ???
  }

  override def visitEnd(): Unit = {
    ???
  }

  override def visitField(access: Int, name: String, desc: String, signature: String, value: Any): FieldVisitor = {
    ???
  }

  override def visitInnerClass(name: String, outerName: String, innerName: String, access: Int): Unit = {
    ???
  }

  override def visitMethod(
    access: Int,
    name: String,
    desc: String,
    signature: String,
    exceptions: Array[String]
  ): MethodVisitor = {
    ???
  }

  override def visitOuterClass(owner: String, name: String, desc: String): Unit = {
    ???
  }

  override def visitSource(source: String, debug: String): Unit = {
    ???
  }

  override def visitTypeAnnotation(
    typeRef: Int,
    typePath: TypePath,
    desc: String,
    visible: Boolean
  ): AnnotationVisitor = {
    ???
  }
}

object ClassBuilder {
  def fromBytes(classPath: ClassPath, bytes: Array[Byte]): ClassBuilder = {
    val writer = new ClassBuilder(classPath)
    new ClassReader(bytes).accept(
      writer,
      ClassReader.SKIP_CODE & ClassReader.SKIP_DEBUG & ClassReader.SKIP_FRAMES
    )
    writer
  }
}