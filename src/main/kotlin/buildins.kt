fun lookupBuiltIn(id: Identifier) = builtIns[id]

interface BuildInt {
    operator fun invoke(vararg args: Int): Int?
}

private val builtIns = mutableMapOf(
    Identifier("input") to ReadInput,
    Identifier("print") to Print
)

object ReadInput : BuildInt {
    override fun invoke(vararg args: Int): Int {
        semanticCheck(args.isEmpty()) { "Input should accept no arguments" }
        val line = readlnOrNull() ?: internalError("Failed to read input")
        return line.toIntOrNull() ?: runtimeError("Invalid value passed as integer: $line")
    }

}

object Print : BuildInt {
    override fun invoke(vararg args: Int): Int? {
        semanticCheck(args.size == 1) { "Print should be passed exactly one argument" }
        println(args[0])
        return null
    }
}