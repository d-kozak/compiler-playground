data class IrFunction(val name: Identifier, val instructions: List<Instruction>)

sealed class Instruction(var name: String) {
    var label: Identifier? = null
}

data class MoveConst(val target: Identifier, val constant: IntConstant) : Instruction("MOVC")
data class Move(val target: Identifier, val source: Identifier) : Instruction("MOV")
data class Not(val target: Identifier, val source: Identifier) : Instruction("NOT")
data class Add(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("ADD")
data class Sub(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("SUB")
data class Mult(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("MULT")
data class Div(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("DIV")
data class Eq(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("EQ")
data class Neq(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("NEQ")
data class Lt(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("LT")
data class Le(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("LE")
data class Gt(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("GT")
data class Ge(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction("GE")

abstract class JumpInstruction(var targetIndex: Int, name: String) : Instruction(name) {
    lateinit var target: Instruction
}

class DirectJump(targetIndex: Int = -1) : JumpInstruction(targetIndex, "JMP")

class CondJump(val condition: Identifier, targetIndex: Int = -1) : JumpInstruction(targetIndex, "CJMP")

data class FunctionCall(val target: Identifier, val functionName: Identifier, val args: List<Identifier>) :
    Instruction("CALL")

data class Ret(val value: Identifier?) : Instruction("RET")

class Noop : Instruction("NOP")