enum class TokenType {
    KEYWORD_FUN,
    PAREN_LEFT,
    PAREN_RIGHT,
    BRACKET_LEFT,
    BRACKET_RIGHT,
    IDENTIFIER,
    INT_LITERAL,
    PLUS,
    EQ
}

data class SourceLocation(val line: IntRange, val column: IntRange) {
    constructor(line: Int, column: Int) : this(line..line, column..column)
    constructor(line: Int, column: IntRange) : this(line..line, column)
}

data class Token(val type: TokenType, val location: SourceLocation, val value: String)