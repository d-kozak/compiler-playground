data class SourceInfo(
    val lineFrom: Int,
    val lineTo: Int,
    val colFrom: Int,
    val colTo: Int
)

sealed class Token

data class IdentifierToken(val name: Identifier) : Token()
class IntToken(val value: Long) : Token()
class EqualsToken : Token()
class PlusToken : Token()
class PrintToken : Token()

class Lexer(val input: String) {
    var pos = 0
    val N = input.length

    private var buff: Token? = null

    fun peek(): Token? {
        if (buff != null) return buff
        buff = next()
        return buff
    }

    fun next(): Token? {
        val prev = buff
        if (prev != null) {
            buff = null
            return prev
        }
        eatSpace()
        while (pos < N) {
            val c = input[pos]
            return when {
                c.isDigit() -> lexInt()
                c == '+' -> {
                    pos++
                    PlusToken()
                }

                c == '=' -> {
                    pos++
                    EqualsToken()
                }

                else -> lexIdOrKeyword()
            }
        }
        return null
    }

    private fun eatSpace() {
        while (pos < N && input[pos].isWhitespace())
            pos++
    }

    private fun lexIdOrKeyword(): Token {
        var curr = pos
        while (curr < N && (input[curr] == '_' || input[curr].isLetter()))
            curr++
        val str = input.substring(pos, curr)
        pos = curr
        return when (str) {
            "print" -> PrintToken()
            else -> IdentifierToken(str)
        }
    }

    private fun lexInt(): Token {
        var curr = pos
        while (curr < N && input[curr].isDigit())
            curr++
        val x = input.substring(pos, curr).toLong()
        pos = curr
        return IntToken(x)
    }

    fun isEmpty() = pos >= N
}