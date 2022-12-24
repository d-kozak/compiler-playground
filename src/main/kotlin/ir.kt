data class IrFunction(val name: Identifier, val params: List<Identifier>, var instructions: Array<Instruction>)


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


    /**
     * Index into the the instruction array
     * @see setInstructionIndexes
     */
    var index: Int = -1
}

interface BinaryInstruction {
    val target: Identifier
    var left: IdentifierOrValue
    var right: IdentifierOrValue

    fun canNegate(): Boolean

    fun negate(): Instruction
}
class Move(val target: Identifier, var source: IdentifierOrValue) : Instruction("MOV")
class Not(val target: Identifier, val source: Identifier) : Instruction("NOT")
class Add(
    override val target: Identifier,
    override var left: IdentifierOrValue,
    override var right: IdentifierOrValue
) :
    Instruction("ADD"),
    BinaryInstruction {
    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")
}

class Sub(
    override val target: Identifier,
    override var left: IdentifierOrValue,
    override var right: IdentifierOrValue
) :
    Instruction("SUB"),
    BinaryInstruction {

    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")

}

class Mult(
    override val target: Identifier,
    override var left: IdentifierOrValue,
    override var right: IdentifierOrValue
) :
    Instruction("MULT"),
    BinaryInstruction {

    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")
}

class Div(
    override val target: Identifier,
    override var left: IdentifierOrValue,
    override var right: IdentifierOrValue
) :
    Instruction("DIV"), BinaryInstruction {

    override fun canNegate(): Boolean = false

    override fun negate() = internalError("cannot negate")
}

class Eq(override val target: Identifier, override var left: IdentifierOrValue, override var right: IdentifierOrValue) :
    Instruction("EQ"),
    BinaryInstruction {

    override fun canNegate(): Boolean = true

    override fun negate() = Neq(target, left, right).also { it.copyLabel(this) }

}

class Neq(
    override val target: Identifier,
    override var left: IdentifierOrValue,
    override var right: IdentifierOrValue
) :
    Instruction("NEQ"),
    BinaryInstruction {

    override fun canNegate(): Boolean = true

    override fun negate() = Eq(target, left, right).also { it.copyLabel(this) }
}

class Lt(override val target: Identifier, override var left: IdentifierOrValue, override var right: IdentifierOrValue) :
    Instruction("LT"), BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Ge(target, left, right).also { it.copyLabel(this) }
}

class Le(override val target: Identifier, override var left: IdentifierOrValue, override var right: IdentifierOrValue) :
    Instruction("LE"),
    BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Gt(target, left, right).also { it.copyLabel(this) }
}

class Gt(override val target: Identifier, override var left: IdentifierOrValue, override var right: IdentifierOrValue) :
    Instruction("GT"),
    BinaryInstruction {
    override fun canNegate(): Boolean = true

    override fun negate() = Le(target, left, right).also { it.copyLabel(this) }
}

class Ge(override val target: Identifier, override var left: IdentifierOrValue, override var right: IdentifierOrValue) :
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

class Ret(var value: IdentifierOrValue?) : Instruction("RET")

class Noop : Instruction("NOP")