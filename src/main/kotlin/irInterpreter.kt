fun interpret(functions: List<IrFunction>) {
    // todo check for name collisions
    val lookup = functions.associateBy { it.name }
    val main = lookup[Identifier("main")] ?: semanticError("No main method found")
    val interpreter = IrInterpreter(lookup)
    interpreter.execute(main)
}


sealed interface Value {
    operator fun plus(other: Value): Value
    operator fun minus(other: Value): Value

    operator fun times(other: Value): Value

    operator fun div(other: Value): Value
    operator fun rem(other: Value): Value

    operator fun compareTo(other: Value): Int

}

data class ArrayValue(var arr: IntArray) : Value {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrayValue

        if (!arr.contentEquals(other.arr)) return false

        return true
    }

    override fun hashCode(): Int {
        return arr.contentHashCode()
    }

    override fun toString(): String {
        return arr.contentToString()
    }

    override fun plus(other: Value): Value {
        cannotPerform()
    }

    override fun minus(other: Value): Value {
        cannotPerform()
    }

    override fun times(other: Value): Value {
        cannotPerform()
    }

    override fun div(other: Value): Value {
        cannotPerform()
    }

    override fun rem(other: Value): Value {
        cannotPerform()
    }

    override fun compareTo(other: Value): Int {
        cannotPerform()
    }

    private fun cannotPerform(): Nothing {
        semanticError("Cannot perform arithmetic on arrays")
    }
}

class Scope(var parent: Scope? = null) {

    private val elems = mutableMapOf<Identifier, Value>()


    fun lookup(id: IdentifierOrValue): Value = when (id) {
        is Identifier -> lookup(id)
        is IntConstant -> id
    }


    fun lookup(id: Identifier): Value {
        var curr = this as Scope?
        while (curr != null) {
            val res = elems[id]
            if (res != null) return res
            curr = curr.parent
        }
        semanticError("Trying to lookup undeclared variable '${id.name}'")
    }

    fun insert(id: Identifier, value: Value) {
        elems[id] = value
    }
}

class IrInterpreter(val functions: Map<Identifier, IrFunction>) {

    val topLevelScope = Scope()
    var currentScope = topLevelScope

    fun execute(entry: IrFunction) {
        call(entry)
    }

    private fun call(func: IrFunction, vararg args: Value): Value? {
        pushScope()
        semanticCheck(func.params.size == args.size) {
            "The actual number of arguments does not correspond to the expected count of params of ${func.params.size} for ${func.name}, received: ${args.toList()}"
        }
        for ((i, param) in func.params.withIndex()) {
            currentScope.insert(param, args[i])
        }
        val res = execute(func.instructions)
        popScope()
        return res
    }

    private fun execute(instructions: Array<Instruction>): Value? {
        var i = 0
        while (i < instructions.size) {
            when (val inst = instructions[i]) {
                is Add -> executeBinary(inst) { a, b -> a + b }
                is Sub -> executeBinary(inst) { a, b -> a - b }
                is Mult -> executeBinary(inst) { a, b -> a * b }
                is Div -> executeBinary(inst) { a, b -> a / b }
                is Mod -> executeBinary(inst) { a, b -> a % b }

                is Eq -> executeBinary(inst) { a, b -> asInt(a == b) }
                is Neq -> executeBinary(inst) { a, b -> asInt(a != b) }
                is Lt -> executeBinary(inst) { a, b -> asInt(a < b) }
                is Le -> executeBinary(inst) { a, b -> asInt(a <= b) }
                is Gt -> executeBinary(inst) { a, b -> asInt(a > b) }
                is Ge -> executeBinary(inst) { a, b -> asInt(a >= b) }

                is Move -> executeMove(inst)

                is ArrayRead -> executeArrayRead(inst)

                is Noop -> noop()
                is Not -> executeNot(inst)

                is FunctionCall -> executeCall(inst)
                is Ret -> {
                    return inst.value?.let { currentScope.lookup(it) }// just jumping out of execute should be enough
                }

                is DirectJump -> {
                    i = inst.target.index
                    continue
                }

                is CondJump -> {
                    val cond = currentScope.lookup(inst.condition)
                    if (asBoolean(cond)) {
                        i = inst.target.index
                        continue
                    }
                }

                is JumpInstruction -> internalError("Should never get here, covered by the jumps above")
            }
            i++
        }

        return null
    }

    private fun executeArrayRead(inst: ArrayRead) {
        val base = currentScope.lookup(inst.arrayBase) as ArrayValue
        val index = currentScope.lookup(inst.arrIndex) as IntConstant
        val unwraped = base.arr[index.value]
        currentScope.insert(inst.target, IntConstant(unwraped))
    }

    private fun executeCall(call: FunctionCall) {
        val args = call.args.map { currentScope.lookup(it) }.toTypedArray()

        val builtIn = lookupBuiltIn(call.functionName)
        if (builtIn != null) {
            val res = builtIn(*args)
            if (res != null)
                currentScope.insert(call.target, res)
            return
        }

        val functionToCall = functions[call.functionName]
        if (functionToCall != null) {
            val res = call(functionToCall, *args)
            if (res != null) {
                // todo handle nulls - now instead of some null error, the lookup of the variable fails later on
                // because it is not inserted into the scope
                currentScope.insert(call.target, res)
            }
            return
        }
        semanticError("Could not find function ${call.functionName}")
    }

    private fun executeNot(inst: Not) {
        val source = asBoolean(currentScope.lookup(inst.source))
        currentScope.insert(inst.target, asInt(!source))
    }

    private fun noop() {
        // nothing to do
    }

    private fun executeMove(inst: Move) {
        val source = currentScope.lookup(inst.source)
        currentScope.insert(inst.target, source)
    }


    private inline fun executeBinary(inst: BinaryInstruction, op: (Value, Value) -> Value) {
        try {
            val left = currentScope.lookup(inst.left)
            val right = currentScope.lookup(inst.right)
            val res = op(left, right)
            currentScope.insert(inst.target, res)
        } catch (err: SemanticError) {
            semanticError("When executing $inst: ${err.message}")
        }
    }

    private fun pushScope() {
        currentScope = Scope(currentScope)
    }

    private fun popScope() {
        currentScope = currentScope.parent ?: internalError("Trying to pop the top-level scope")
    }

}

fun asInt(b: Boolean): IntConstant {
    return IntConstant(if (b) 1 else 0)
}

fun asBoolean(x: Value): Boolean = when (x) {
    is IntConstant -> x.value != 0
    is ArrayValue -> semanticError("Cannot convert an array to a boolean.")
}

fun asBoolean(x: Int): Boolean {
    return x != 0
}

