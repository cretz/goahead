package goahead.compile
package insn

import goahead.ast.Node
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.VarInsnNode

trait VarInsnCompiler {
  import AstDsl._
  import Helpers._
  import MethodCompiler._

  def compile(ctx: Context, insn: VarInsnNode): (Context, Seq[Node.Statement]) = {
    insn.byOpcode  {
      case Opcodes.ALOAD | Opcodes.DLOAD | Opcodes.FLOAD | Opcodes.ILOAD | Opcodes.LLOAD =>
        load(ctx, insn.`var`, insn.getOpcode)
      case Opcodes.ASTORE | Opcodes.DSTORE | Opcodes.FSTORE | Opcodes.ISTORE | Opcodes.LSTORE =>
        store(ctx, insn.`var`, insn.getOpcode)
      case Opcodes.RET =>
        // TODO: confirm deprecation
        sys.error("Opcode RET not supported")
    }
  }

  protected def load(ctx: Context, index: Int, opcode: Int): (Context, Seq[Node.Statement]) = {
    @inline
    def doLoad(typ: IType) = ctx.getLocalVar(index, typ, forWriting = false).map { case (ctx, local) =>
      ctx.stackPushed(local) -> Seq.empty[Node.Statement]
    }
    opcode match {
      case Opcodes.ALOAD => doLoad(ObjectType)
      case Opcodes.DLOAD => doLoad(IType.DoubleType)
      case Opcodes.FLOAD => doLoad(IType.FloatType)
      case Opcodes.ILOAD => doLoad(IType.IntType)
      case Opcodes.LLOAD => doLoad(IType.LongType)
    }
  }

  protected def store(ctx: Context, index: Int, opcode: Int): (Context, Seq[Node.Statement]) = {
    @inline
    def doStore(typ: IType) = ctx.stackPopped { case (ctx, value) =>
      // We need to use the value type if it's already a simple type, otherwise make our own
      val localVarTyp = if (value.typ.isInstanceOf[IType.Simple]) value.typ else typ
      ctx.getLocalVar(index, localVarTyp, forWriting = true).map { case (ctx, local) =>
        value.toExprNode(ctx, local.typ).map { case (ctx, value) =>
          ctx -> local.expr.assignExisting(value).singleSeq
        }
      }
    }
    opcode match {
      case Opcodes.ASTORE => doStore(ObjectType)
      case Opcodes.DSTORE => doStore(IType.DoubleType)
      case Opcodes.FSTORE => doStore(IType.FloatType)
      case Opcodes.ISTORE => doStore(IType.IntType)
      case Opcodes.LSTORE => doStore(IType.LongType)
    }
  }
}
