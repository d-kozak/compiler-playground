private val blanks = setOf(' ', '\n')

private val keywords = mapOf("fun" to TokenType.KEYWORD_FUN)

class Lexer(val input: String) {

    private var pos = 0
    private val n = input.length
    private var curr: Token? = null
    private var line = 1
    private var lineStart = 0
    fun next(): Token {
        val tkn = curr
        if (tkn != null) {
            curr = null
            return tkn
        }
        return consume()
    }

    fun consume(type: TokenType): Token {
        var tkn = curr
        if (tkn == null) {
            tkn = consume()
        }
        check(tkn.type == type) { "Expected $type, got ${tkn}" }
        curr = null
        return tkn
    }

    fun match(type: TokenType): Boolean {
        if (curr == null) {
            curr = consume()
        }
        return curr!!.type == type
    }


    private fun consume(): Token {
        while (hasText()) {
            when (input[pos]) {
                '(' -> return produceToken(TokenType.PAREN_LEFT)
                ')' -> return produceToken(TokenType.PAREN_RIGHT)
                '{' -> return produceToken(TokenType.BRACKET_LEFT)
                '}' -> return produceToken(TokenType.BRACKET_RIGHT)
                '+' -> return produceToken(TokenType.PLUS)
                '=' -> return produceToken(TokenType.EQ)
                in '1'..'9' -> return lexInt()
                !in blanks -> return lexIdOrKeyword()
            }
            if (input[pos++] == '\n') {
                line++
                lineStart = pos
            }
        }
        error("Failed to produce token")
    }

    fun hasText(): Boolean {
        while (pos < n && input[pos] in blanks) {
            if (input[pos++] == '\n') {
                line++
                lineStart = pos
            }
        }
        return pos < n
    }

    private fun produceToken(type: TokenType, nextPos: Int = pos + 1, value: String = ""): Token {
        val loc = SourceLocation(line, cols(nextPos))
        pos = nextPos
        return Token(type, loc, value)
    }

    private fun cols(nextPos: Int) = (pos - lineStart + 1) until (nextPos - lineStart + 1)

    private fun lexInt(): Token {
        var curr = pos
        while (curr < n && input[curr].isDigit())
            curr++
        return produceToken(TokenType.INT_LITERAL, curr, input.substring(pos, curr))
    }

    private fun lexIdOrKeyword(): Token {
        var curr = pos
        while (curr < n && input[curr].isJavaIdentifierPart())
            curr++
        val value = input.substring(pos, curr)
        val type = keywords[value] ?: TokenType.IDENTIFIER
        return produceToken(type, curr, value)
    }

}