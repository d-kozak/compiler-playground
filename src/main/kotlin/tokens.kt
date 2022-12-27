enum class TokenType {
    KEYWORD_FUN,
    KEYWORD_RETURN,
    KEYWORD_IF,
    KEYWORD_ELSE,
    KEYWORD_WHILE,
    KEYWORD_FOR,
    PAREN_LEFT,
    PAREN_RIGHT,
    BRACKET_LEFT,
    BRACKET_RIGHT,
    SQUARE_BRACKET_LEFT,
    SQUARE_BRACKET_RIGHT,
    IDENTIFIER,
    INT_LITERAL,
    PLUS,
    MINUS,
    MULT,
    DIV,
    MOD,
    LT,
    LE,
    GT,
    GE,
    EQ,
    EQ_EQ,
    NEQ,
    COMMA,
    SEMICOLON
}

data class SourceLocation(val line: Int, val column: IntRange) {
    constructor(line: Int, column: Int) : this(line, column..column)
}

data class Token(val type: TokenType, val location: SourceLocation, val value: String)