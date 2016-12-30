package goahead.cli

import java.util.jar.JarFile

import com.google.common.io.ByteStreams
import goahead.Logger
import goahead.cli.FindOpcode.MethodsWithOpcodeClassVisitor
import goahead.compile.ClassPath
import goahead.compile.Helpers._
import org.objectweb.asm._
import org.objectweb.asm.util.Printer

class FindOpcode extends Command with Logger {
  override val name = "find-opcode"

  case class Conf(
    max: Int = 1,
    staticOnly: Boolean = false,
    packageStart: String = "",
    jar: String = ClassPath.Entry.javaRuntimeJarPath.toString,
    opcodes: Seq[String]
  )

  override def argParser = { implicit builder =>
    Conf(
      max = builder.opt(
        name = "max",
        aliases = Seq("m"),
        desc = "The maximum number of opcodes find",
        default = "1"
      ).get.toInt,
      staticOnly = builder.flag(
        name = "static",
        desc = "Only get static"
      ).get,
      packageStart = builder.opt(
        name = "package-start",
        aliases = Seq("p"),
        desc = "Require the package to start with the given string"
      ).get,
      jar = builder.opt(
        name = "jar",
        aliases = Seq("j"),
        desc = "What JAR to use, defaults to the rt.jar on the classpath",
        default = ClassPath.Entry.javaRuntimeJarPath.toString
      ).get,
      opcodes = builder.trailingOpts(
        name = "opcode",
        required = true,
        desc = "The opcodes to search, case insensitive"
      ).get
    )
  }

  override def confLoader = pureconfig.loadConfig[Conf]

  override def run(conf: Conf): Unit = {
    val opcodes = conf.opcodes.map { opcodeStr =>
      val opcode = Printer.OPCODES.indexOf(opcodeStr.toUpperCase)
      require(opcode != -1, s"Unable to find opcode $opcodeStr")
      opcode
    }
    val visitor = new MethodsWithOpcodeClassVisitor(opcodes.toSet, conf.staticOnly)

    val jar = new JarFile(conf.jar)
    try {
      val packageStart = conf.packageStart.replace('.', '/')

      val classEntries = {
        import scala.collection.JavaConverters._
        jar.entries().asScala.filter { e =>
          !e.isDirectory && e.getName.endsWith(".class") && e.getName.startsWith(packageStart)
        }
      }

      // Lazily find the methods
      val foundMethods = classEntries.toTraversable.view.flatMap { entry =>
        val bytes = {
          val is = jar.getInputStream(entry)
          try ByteStreams.toByteArray(is) finally swallowException(is.close())
        }
        new ClassReader(bytes).accept(visitor, 0)
        visitor.matchingMethods
      }

      foundMethods.take(conf.max).foreach(f => println(s"Method: $f"))

    } finally swallowException(jar.close())
  }
}

object FindOpcode extends FindOpcode {
  class MethodsWithOpcodeClassVisitor(opcodes: Set[Int], staticOnly: Boolean) extends ClassVisitor(Opcodes.ASM6) {
    var className = ""
    var currMethod = ""
    var matchingMethods = Set.empty[String]

    val methodVisitor = new MethodVisitor(Opcodes.ASM6) {
      var foundMethod = false

      override def visitEnd() = {
        if (foundMethod) matchingMethods += className + "."+ currMethod
        foundMethod = false
      }

      def checkOpcode(opcode: Int): Unit =
        if (!foundMethod && opcodes.contains(opcode)) foundMethod = true

      override def visitInsn(opcode: Int) =
        checkOpcode(opcode)
      override def visitIntInsn(opcode: Int, operand: Int) =
        checkOpcode(opcode)
      override def visitVarInsn(opcode: Int, `var`: Int) =
        checkOpcode(opcode)
      override def visitTypeInsn(opcode: Int, `type`: String) =
        checkOpcode(opcode)
      override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) =
        checkOpcode(opcode)
      override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) =
        checkOpcode(opcode)
      override def visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, bsmArgs: AnyRef*) =
        checkOpcode(Opcodes.INVOKEDYNAMIC)
      override def visitJumpInsn(opcode: Int, label: Label) =
        checkOpcode(opcode)
      override def visitLdcInsn(cst: scala.Any) =
        checkOpcode(Opcodes.LDC)
      override def visitIincInsn(`var`: Int, increment: Int) =
        checkOpcode(Opcodes.IINC)
      override def visitTableSwitchInsn(min: Int, max: Int, dflt: Label, labels: Label*) =
        checkOpcode(Opcodes.TABLESWITCH)
      override def visitLookupSwitchInsn(dflt: Label, keys: Array[Int], labels: Array[Label]) =
        checkOpcode(Opcodes.LOOKUPSWITCH)
    }

    override def visit(
      version: Int,
      access: Int,
      name: String,
      signature: String,
      superName: String,
      interfaces: Array[String]
    ) = {
      className = name
      matchingMethods = Set.empty
    }

    override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]) = {
      if (staticOnly && !access.isAccessStatic) null else {
        currMethod = name
        if (access.isAccessStatic) currMethod += " (static)"
        methodVisitor
      }
    }
  }
}
