class Parser(input: String) {
    private val lexer = Lexer(input)


    fun parse(fileName: String): FileNode {
        val statements = mutableListOf<StatementNode>()
        while (!lexer.isEmpty())
            statements.add(parseStatement())
        return FileNode(fileName, statements)
    }

    private fun parseStatement(): StatementNode {
        return when (val next = lexer.next()) {
            is IdentifierToken -> parseAssignment(next.name)
            is PrintToken -> parsePrint()
            else -> parseError("Bad top level token $next.")
        }
    }

    private fun parseAssignment(target: Identifier): AssignmentNode {
        eat("Expecting equals") { it is EqualsToken }
        val expr = parseExpression()
        return AssignmentNode(target, expr)
    }

    private fun parseExpression(): ExpressionNode {
        var left = parseLitOrVar()
        while (lexer.peek() is PlusToken) {
            assert(lexer.next() is PlusToken)
            val right = parseLitOrVar()
            left = AddNode(left, right)
        }
        return left
    }

    private fun parseLitOrVar(): ExpressionNode = when (val next = lexer.next()) {
        is IdentifierToken -> VariableLoadNode(next.name)
        is IntToken -> LiteralNode(Constant(next.value))
        else -> parseError("Expecting id or int.")
    }

    private fun parsePrint() = when (val variable = lexer.next()) {
        is IdentifierToken -> PrintNode(variable.name)
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