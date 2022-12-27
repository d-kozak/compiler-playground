inline fun semanticCheck(condition: Boolean, lazyMsg: () -> String) {
    if (!condition)
        semanticError(lazyMsg())
}

fun semanticError(msg: String): Nothing = throw SemanticError(msg)
class SemanticError(msg: String) : RuntimeException(msg)


fun internalError(msg: String): Nothing = throw InternalError(msg)
class InternalError(msg: String) : RuntimeException(msg)


fun runtimeError(msg: String): Nothing = throw RuntimeExecutionError(msg)

class RuntimeExecutionError(msg: String) : RuntimeException(msg)


fun failAssert(msg: String): Nothing = throw ProgAssertError(msg)

class ProgAssertError(msg: String) : RuntimeException(msg)