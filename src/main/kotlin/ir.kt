data class IrFunction(val name: Identifier, val params: List<Identifier>, val instructions: Array<Instruction>)

sealed class Instruction(var name: String) {
    fun copyLabel(source: Instruction) {
        this.label = source.label
        this.jumpedFrom = source.jumpedFrom ?: return
        this.jumpedFrom!!.target = this
    }

    var jumpedFrom: JumpInstruction? = null
        set(value) {
            require(this.jumpedFrom == null) { "assuming only a single jump target (for now)" }
            field = value
        }

    var label: Identifier? = null
}

interface BinaryInstruction {
    val target: Identifier
    val left: Identifier
    val right: Identifier

    fun canNegate(): Boolean

    fun negate(): Instruction
}

class MoveConst(val target: Identifier, val constant: IntConstant) : Instruction("MOVC")
class Move(val target: Identifier, val source: Identifier) : Instruction("MOV")
class Not(val target: Identifier, val source: Identifier) : Instruction("NOT")
class Add(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("ADD"),
    BinaryInstruction {
    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")
}

class Sub(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("SUB"),
    BinaryInstruction {

    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")

}

class Mult(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("MULT"),
    BinaryInstruction {

    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")
}

class Div(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("DIV"), BinaryInstruction {

    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")
}

class Eq(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("EQ"),
    BinaryInstruction {

    override fun canNegate(): Boolean = true

    override fun negate() = Neq(target, left, right).also { it.copyLabel(this) }

}

class Neq(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("NEQ"),
    BinaryInstruction {

    override fun canNegate(): Boolean = true

    override fun negate() = Eq(target, left, right).also { it.copyLabel(this) }
}

class Lt(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("LT"), BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Ge(target, left, right).also { it.copyLabel(this) }
}

class Le(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("LE"),
    BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Gt(target, left, right).also { it.copyLabel(this) }
}

class Gt(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("GT"),
    BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Le(target, left, right).also { it.copyLabel(this) }
}

class Ge(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("GE"),
    BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Lt(target, left, right).also { it.copyLabel(this) }
}

abstract class JumpInstruction(var targetIndex: Int, name: String) : Instruction(name) {
    lateinit var target: Instruction
}

class DirectJump(targetIndex: Int = -1) : JumpInstruction(targetIndex, "JMP")

class CondJump(var condition: Identifier, targetIndex: Int = -1) : JumpInstruction(targetIndex, "CJMP")

class FunctionCall(val target: Identifier, val functionName: Identifier, val args: List<Identifier>) :
    Instruction("CALL")

class Ret(val value: Identifier?) : Instruction("RET")

class Noop : Instruction("NOP")