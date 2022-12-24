fun interpretIr(functions: List<IrFunction>) {
    // todo check for name collisions
    val lookup = functions.associateBy { it.name }
    val main = lookup[Identifier("main")] ?: semanticError("No main method found")
    val interpreter = IrInterpreter(lookup)
    interpreter.execute(main)
}

class Scope(var parent: Scope? = null) {

    private val elems = mutableMapOf<Identifier, Int>()


    fun lookup(id: IdentifierOrValue): Int = when (id) {
        is Identifier -> lookup(id)
        is IntConstant -> id.value
    }


    fun lookup(id: Identifier): Int {
        var curr = this as Scope?
        while (curr != null) {
            val res = elems[id]
            if (res != null) return res
            curr = curr.parent
        }
        semanticError("Trying to lookup undeclared variable '${id.name}'")
    }

    fun insert(id: Identifier, value: Int) {
        elems[id] = value
    }
}

class IrInterpreter(val functions: Map<Identifier, IrFunction>) {

    val topLevelScope = Scope()
    var currentScope = topLevelScope

    fun execute(entry: IrFunction) {
        call(entry)
    }

    private fun call(func: IrFunction, vararg args: Int): Int? {
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

    private fun execute(instructions: Array<Instruction>): Int? {
        var i = 0
        while (i < instructions.size) {
            when (val inst = instructions[i]) {
                is Add -> executeBinary(inst) { a, b -> a + b }
                is Sub -> executeBinary(inst) { a, b -> a - b }
                is Mult -> executeBinary(inst) { a, b -> a * b }
                is Div -> executeBinary(inst) { a, b -> a / b }

                is Eq -> executeBinary(inst) { a, b -> asInt(a == b) }
                is Neq -> executeBinary(inst) { a, b -> asInt(a != b) }
                is Lt -> executeBinary(inst) { a, b -> asInt(a < b) }
                is Le -> executeBinary(inst) { a, b -> asInt(a <= b) }
                is Gt -> executeBinary(inst) { a, b -> asInt(a > b) }
                is Ge -> executeBinary(inst) { a, b -> asInt(a >= b) }

                is Move -> executeMove(inst)
                is MoveConst -> executeMoveConst(inst)

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

    private fun executeCall(call: FunctionCall) {
        val args = call.args.map { currentScope.lookup(it) }.toIntArray()

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

    private fun executeMoveConst(inst: MoveConst) {
        currentScope.insert(inst.target, inst.constant.value)
    }

    private fun asInt(b: Boolean): Int {
        return if (b) 1 else 0
    }

    private fun asBoolean(x: Int): Boolean {
        return x != 0
    }


    private inline fun executeBinary(inst: BinaryInstruction, op: (Int, Int) -> Int) {
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
