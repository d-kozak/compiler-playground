sealed class Instruction

data class IrFunction(val name: Identifier, val instructions: List<Instruction>)

data class MoveConst(val target: Identifier, val constant: IntConstant) : Instruction()
data class Move(val target: Identifier, val source: Identifier) : Instruction()
data class Not(val target: Identifier, val source: Identifier) : Instruction()
data class Add(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Sub(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Mult(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Div(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Eq(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Neq(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Lt(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Le(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Gt(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Ge(val target: Identifier, val left: Identifier, val right: Identifier) : Instruction()
data class Jump(var targetIndex: Int = 0) : Instruction() {
    var target: Instruction = NullInstruction
}

data class CondJump(val condition: Identifier, var targetIndex: Int = 0) : Instruction() {
    var target: Instruction = NullInstruction
}

data class FunctionCall(val target: Identifier, val functionName: Identifier, val args: List<Identifier>) :
    Instruction()

data class Ret(val value: Identifier?) : Instruction()

// instruction to be patched
object NullInstruction : Instruction()
