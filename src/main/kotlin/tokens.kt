enum class TokenType {
    KEYWORD_FUN,
    KEYWORD_RETURN,
    KEYWORD_IF,
    KEYWORD_WHILE,
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
    LT,
    LE,
    GT,
    GE,
    EQ,
    EQ_EQ,
    NEQ,
    COMMA
}

data class SourceLocation(val line: Int, val column: IntRange) {
    constructor(line: Int, column: Int) : this(line, column..column)
}

data class Token(val type: TokenType, val location: SourceLocation, val value: String)