import java.util.*

fun lookupBuiltIn(id: Identifier) = builtIns[id]

interface BuildInt {
    operator fun invoke(vararg args: Value): Value?
}

private val builtIns = mutableMapOf(
    Identifier("input") to ReadInput,
    Identifier("print") to Print,
    Identifier("assert") to Assert,
    Identifier("IntArray") to ArrayConstructor
)

object ReadInput : BuildInt {
    override fun invoke(vararg args: Value): Value {
        semanticCheck(args.isEmpty()) { "Input should accept no arguments" }
        val line = readlnOrNull() ?: internalError("Failed to read input")
        return IntConstant(line.toIntOrNull() ?: runtimeError("Invalid value passed as integer: $line"))
    }

}

object Print : BuildInt {
    override fun invoke(vararg args: Value): Value? {
        semanticCheck(args.size <= 1) { "Print should be passed exactly at most one argument" }
        if (args.size == 1)
            println(args[0])
        else println()
        return null
    }
}


object Assert : BuildInt {
    override fun invoke(vararg args: Value): Value? {
        semanticCheck(args.size == 1) { "Assert expects one argument - value to check" }
        if (!asBoolean(args[0]))
            failAssert("Assertion failed")
        return null
    }
}


val rnd = Random()

object ArrayConstructor : BuildInt {
    override fun invoke(vararg args: Value): Value? {
        semanticCheck(args.size == 1) { "Array constructor expects a single argument - array size" }
        val size = args[0]
        semanticCheck(size is IntConstant) { "Array size should be an integer" }
        val sizeAsInt = (size as IntConstant).value
        return ArrayValue(IntArray(sizeAsInt) { rnd.nextInt(0, size.value) })
    }
}