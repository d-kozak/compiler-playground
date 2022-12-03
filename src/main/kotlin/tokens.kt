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
    MINUS,
    MULT,
    DIV,
    EQ_EQ,
    LT,
    LE,
    GT,
    GE,
    EQ,
    NEQ,
    COMMA
}

data class SourceLocation(val line: Int, val column: IntRange) {
    constructor(line: Int, column: Int) : this(line, column..column)
}

data class Token(val type: TokenType, val location: SourceLocation, val value: String)