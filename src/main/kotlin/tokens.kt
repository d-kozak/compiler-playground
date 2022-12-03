enum class TokenType {
    KEYWORD_FUN,
    KEYWORD_RETURN,
    PAREN_LEFT,
    PAREN_RIGHT,
    BRACKET_LEFT,
    BRACKET_RIGHT,
    IDENTIFIER,
    INT_LITERAL,
    PLUS,
    EQ,
    COMMA
}

data class SourceLocation(val line: Int, val column: IntRange) {
    constructor(line: Int, column: Int) : this(line, column..column)
}

data class Token(val type: TokenType, val location: SourceLocation, val value: String)