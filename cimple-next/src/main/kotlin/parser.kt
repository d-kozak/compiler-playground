class Parser(input: String) {
    private val lexer = Lexer(input)


    fun parse(): FileNode {
        val statements = mutableListOf<Statement>()
        while (!lexer.isEmpty())
            statements.add(parseStatement())
        return FileNode(statements)
    }

    private fun parseStatement(): Statement {
        return when (val next = lexer.next()) {
            is IdentifierToken -> parseAssignment(next.name)
            is PrintToken -> parsePrint()
            else -> parseError("Bad top level token $next.")
        }
    }

    private fun parseAssignment(target: Identifier): Assignment {
        eat("Expecting equals") { it is EqualsToken }
        val expr = parseExpression()
        return Assignment(target, expr)
    }

    private fun parseExpression(): Expression {
        var left = parseLitOrVar()
        while (lexer.peek() is PlusToken) {
            assert(lexer.next() is PlusToken)
            val right = parseLitOrVar()
            left = Add(left, right)
        }
        return left
    }

    private fun parseLitOrVar(): Expression = when (val next = lexer.next()) {
        is IdentifierToken -> VariableLoad(next.name)
        is IntToken -> LiteralExpression(next.value)
        else -> parseError("Expecting id or int.")
    }

    private fun parsePrint() = when (val variable = lexer.next()) {
        is IdentifierToken -> Print(variable.name)
        else -> parseError("Bad print statement.")
    }


    private inline fun eat(msg: String, test: (Token) -> Boolean): Token {
        val next = lexer.next()
        if (next == null || !test(next))
            parseError(msg)
        return next
    }

    private fun parseError(msg: String): Nothing {
        TODO(msg)
    }
}