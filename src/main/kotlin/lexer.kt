private val blanks = setOf(' ', '\n')

private val keywords = mapOf(
    "fun" to TokenType.KEYWORD_FUN,
    "return" to TokenType.KEYWORD_RETURN,
    "if" to TokenType.KEYWORD_IF,
    "else" to TokenType.KEYWORD_ELSE,
    "while" to TokenType.KEYWORD_WHILE,
    "for" to TokenType.KEYWORD_FOR
)

class Lexer(val input: String) {

    private var pos = 0
    private val n = input.length
    private val buffer = mutableListOf<Token>()
    private var line = 1
    private var lineStart = 0

    fun peek(): Token {
        if (buffer.isEmpty()) {
            buffer.add(consume())
        }
        return buffer.last()
    }

    fun next(): Token {
        if (buffer.isNotEmpty()) {
            return buffer.removeLast()
        }
        return consume()
    }

    fun consume(type: TokenType): Token {
        val tkn = if (buffer.isNotEmpty()) buffer.removeLast() else consume()
        syntaxCheck(tkn.type == type, tkn) { "Expected $type, got $tkn" }
        return tkn
    }

    fun returnToken(next: Token) {
        buffer.add(next)
    }

    fun match(type: TokenType): Boolean {
        return peek().type == type
    }

    fun matchAndEat(type: TokenType): Boolean {
        if (match(type)) {
            next()
            return true
        }
        return false
    }

    fun matchAny(vararg types: TokenType): Boolean {
        val current = peek().type
        return types.any { it == current }
    }


    private fun consume(): Token {
        while (hasText()) {
            when (input[pos]) {
                '(' -> return produceToken(TokenType.PAREN_LEFT)
                ')' -> return produceToken(TokenType.PAREN_RIGHT)
                '{' -> return produceToken(TokenType.BRACKET_LEFT)
                '}' -> return produceToken(TokenType.BRACKET_RIGHT)
                '+' -> return produceToken(TokenType.PLUS)
                '-' -> return produceToken(TokenType.MINUS)
                '*' -> return produceToken(TokenType.MULT)
                '/' -> return produceToken(TokenType.DIV)
                '%' -> return produceToken(TokenType.MOD)
                '=' -> {
                    return if (pos + 1 < n && input[pos + 1] == '=')
                        produceToken(TokenType.EQ_EQ, nextPos = pos + 2)
                    else
                        produceToken(TokenType.EQ)
                }

                ',' -> return produceToken(TokenType.COMMA)
                ';' -> return produceToken(TokenType.SEMICOLON)
                '!' -> {
                    if (pos + 1 < n && input[pos + 1] == '=')
                        return produceToken(TokenType.NEQ, nextPos = pos + 2)
                }

                '<' -> {
                    return if (pos + 1 < n && input[pos + 1] == '=')
                        produceToken(TokenType.LE, nextPos = pos + 2)
                    else produceToken(TokenType.LT)
                }

                '>' -> {
                    return if (pos + 1 < n && input[pos + 1] == '=')
                        produceToken(TokenType.GE, nextPos = pos + 2)
                    else produceToken(TokenType.GT)
                }

                in '0'..'9' -> return lexInt()
            }

            if (input[pos].isLetter()) return lexIdOrKeyword()

            if (!(input[pos] in blanks))
                syntaxError("Unrecognized token ${input[pos]}")

            if (input[pos++] == '\n') {
                line++
                lineStart = pos
            }
        }
        syntaxError("Failed to produce token")
    }

    private fun syntaxCheck(condition: Boolean, tkn: Token, lazyMsg: () -> String) {
        if (!condition)
            syntaxError(lazyMsg(), tkn)
    }

    fun syntaxError(msg: String, token: Token? = null): Nothing = throw SyntaxError(msg, context(token), currPos())

    private fun context(token: Token?): String {
        val loc = token?.location ?: currPos()
        val lines = input.split('\n')
        return buildString {
            if (loc.line - 2 >= 0) appendLine(lines[loc.line - 2])
            val wrongLine = lines[loc.line - 1]
            appendLine(wrongLine)
            val highlighter =
                wrongLine.indices.map { if (it + 1 in loc.column) '^' else ' ' }.joinToString(separator = "")
            appendLine(highlighter)
            if (loc.line < lines.size) appendLine(lines[loc.line])
        }
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


    fun currPos() = SourceLocation(line, pos - lineStart + 1)

}