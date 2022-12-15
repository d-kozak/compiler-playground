data class IrFunction(val name: Identifier, val params: List<Identifier>, val instructions: List<Instruction>)

sealed class Instruction(var name: String) {
    var label: Identifier? = null
}

interface BinaryInstruction {
    val target: Identifier
    val left: Identifier
    val right: Identifier
}

class MoveConst(val target: Identifier, val constant: IntConstant) : Instruction("MOVC")
class Move(val target: Identifier, val source: Identifier) : Instruction("MOV")
class Not(val target: Identifier, val source: Identifier) : Instruction("NOT")
class Add(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("ADD"),
    BinaryInstruction

class Sub(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("SUB"),
    BinaryInstruction

class Mult(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("MULT"),
    BinaryInstruction

class Div(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("DIV"), BinaryInstruction

class Eq(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("EQ"),
    BinaryInstruction

class Neq(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("NEQ"),
    BinaryInstruction

class Lt(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("LT"), BinaryInstruction

class Le(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("LE"),
    BinaryInstruction

class Gt(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("GT"),
    BinaryInstruction

class Ge(override val target: Identifier, override val left: Identifier, override val right: Identifier) :
    Instruction("GE"),
    BinaryInstruction

abstract class JumpInstruction(var targetIndex: Int, name: String) : Instruction(name) {
    lateinit var target: Instruction
}

class DirectJump(targetIndex: Int = -1) : JumpInstruction(targetIndex, "JMP")

class CondJump(val condition: Identifier, targetIndex: Int = -1) : JumpInstruction(targetIndex, "CJMP")

class FunctionCall(val target: Identifier, val functionName: Identifier, val args: List<Identifier>) :
    Instruction("CALL")

class Ret(val value: Identifier?) : Instruction("RET")

class Noop : Instruction("NOP")