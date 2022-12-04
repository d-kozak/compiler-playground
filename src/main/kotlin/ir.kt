data class IrFunction(val name: Identifier, val params: List<Identifier>, val instructions: List<Instruction>)

sealed class Instruction(var name: String) {
    var label: Identifier? = null
}

interface BinaryInstruction {
    val target: Identifier
    val left: Identifier
    val right: Identifier
}

data class MoveConst(val target: Identifier, val constant: IntConstant) : Instruction("MOVC")
data class Move(val target: Identifier, val source: Identifier) : Instruction("MOV")
data class Not(val target: Identifier, val source: Identifier) : Instruction("NOT")
data class Add(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("ADD"),
    BinaryInstruction

data class Sub(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("SUB"),
    BinaryInstruction

data class Mult(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("MULT"),
    BinaryInstruction

data class Div(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("DIV"), BinaryInstruction

data class Eq(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("EQ"),
    BinaryInstruction

data class Neq(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("NEQ"),
    BinaryInstruction

data class Lt(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("LT"), BinaryInstruction

data class Le(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("LE"),
    BinaryInstruction

data class Gt(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("GT"),
    BinaryInstruction

data class Ge(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("GE"),
    BinaryInstruction

abstract class JumpInstruction(var targetIndex: Int, name: String) : Instruction(name) {
    lateinit var target: Instruction
}

class DirectJump(targetIndex: Int = -1) : JumpInstruction(targetIndex, "JMP")

class CondJump(val condition: Identifier, targetIndex: Int = -1) : JumpInstruction(targetIndex, "CJMP")

data class FunctionCall(val target: Identifier, val functionName: Identifier, val args: List<Identifier>) :
    Instruction("CALL")

data class Ret(val value: Identifier?) : Instruction("RET")

class Noop : Instruction("NOP")