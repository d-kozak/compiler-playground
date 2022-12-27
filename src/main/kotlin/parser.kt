import java.io.File

class SyntaxError(msg: String, context: String, location: SourceLocation) :
    RuntimeException("$msg at $location\n $context") {
}

class Parser(val lexer: Lexer, val file: File) {


    fun parseFile(): FileContentNode {
        val declarations = mutableListOf<TopLevelDeclaration>()
        while (lexer.hasText()) {
            val token = lexer.next()
            when (token.type) {
                TokenType.KEYWORD_FUN -> {
                    declarations.add(parseFunction())
                }

                else -> lexer.syntaxError("Expected a top level statement, got $token", token)
            }
        }
        return FileContentNode(file, declarations)
    }

    private fun parseFunction(): FunctionDeclarationNode {
        val nameToken = lexer.consume(TokenType.IDENTIFIER)
        lexer.consume(TokenType.PAREN_LEFT)
        val parameters = parseParams()
        lexer.consume(TokenType.PAREN_RIGHT)
        val statements = parseBlock()
        return FunctionDeclarationNode(Identifier(nameToken.value), parameters, statements)
    }

    private fun parseParams(): List<Identifier> {
        val params = mutableListOf<Identifier>()
        while (!lexer.match(TokenType.PAREN_RIGHT)) {
            val param = lexer.consume(TokenType.IDENTIFIER)
            params.add(Identifier(param.value))
            if (!lexer.match(TokenType.PAREN_RIGHT))
                lexer.consume(TokenType.COMMA)
        }
        return params
    }

    private fun parseBlock(): List<StatementNode> {
        val statements = mutableListOf<StatementNode>()
        lexer.consume(TokenType.BRACKET_LEFT)

        while (!lexer.match(TokenType.BRACKET_RIGHT)) {
            statements.add(parseStatement())
        }

        lexer.consume(TokenType.BRACKET_RIGHT)
        return statements
    }

    private fun parseStatement(): StatementNode {
        val start = lexer.next()
        when (start.type) {
            TokenType.IDENTIFIER -> {
                val identifier = start
                val next = lexer.next()
                when (next.type) {
                    TokenType.EQ -> {
                        val expression = parseExpression()
                        return AssignmentNode(Identifier(identifier.value), expression)
                    }

                    TokenType.PAREN_LEFT -> {
                        // todo this could be  probably a fall-through as well - function call is an expression
                        // and this simplification could help when adding arrays for example (reduces duplicity)
                        return finishFuctionCall(identifier)
                    }

                    else -> {
                        lexer.returnToken(next)
                        lexer.returnToken(start)
                    }
                    // fallthrough, handle expression below lexer.syntaxError("Unexpected statement type, got $next", next)
                }
            }

            TokenType.KEYWORD_RETURN -> {
                val next = lexer.peek()
                var expression: ExpressionNode? = null
                if (next.type != TokenType.BRACKET_RIGHT && start.location.line == next.location.line) {
                    // the block is not closed and something is following the return on the same line
                    // -> assume this is an expression to return
                    // todo check whether this makes sense, see valueOnNextLineHowToEvaluate in multiple_adds.prog
                    expression = parseExpression()
                }
                return ReturnNode(expression)
            }

            TokenType.KEYWORD_IF -> {
                lexer.consume(TokenType.PAREN_LEFT)
                val expression = parseExpression()
                lexer.consume(TokenType.PAREN_RIGHT)
                val thenStatement = parseBlock()
                // todo if-else
                return IfNode(expression, thenStatement)
            }


            TokenType.KEYWORD_WHILE -> {
                lexer.consume(TokenType.PAREN_LEFT)
                val expression = parseExpression()
                lexer.consume(TokenType.PAREN_RIGHT)
                val block = parseBlock()
                return WhileNode(expression, block)
            }

            else -> lexer.returnToken(start)
            // fallthrough, handle expression below lexer.syntaxError("Unexpected statement type, got $start", start)
        }
        // if nothing specific matched, try to parse it as a simple expression
        return parseExpression()
    }

    /**
     * Assumes ID and PAREN_LEFT are already consumed
     */
    private fun finishFuctionCall(identifier: Token): FunctionCallNode {
        val arguments = parseArguments()
        lexer.consume(TokenType.PAREN_RIGHT)
        return FunctionCallNode(Identifier(identifier.value), arguments)
    }

    private fun parseArguments(): List<ExpressionNode> {
        val arguments = mutableListOf<ExpressionNode>()
        while (!lexer.match(TokenType.PAREN_RIGHT)) {
            arguments.add(parseExpression())
            if (!lexer.match(TokenType.PAREN_RIGHT))
                lexer.consume(TokenType.COMMA)
        }
        return arguments
    }

    private fun parseExpression(): ExpressionNode {
        return parseCmpExpression()
    }


    private fun parseCmpExpression(): ExpressionNode {
        val left = parseAddExpression()
        when {
            lexer.matchAndEat(TokenType.EQ_EQ) -> {
                val right = parseAddExpression()
                return BinaryOpNode(left, BinaryOperation.EQUALS, right)
            }

            lexer.matchAndEat(TokenType.NEQ) -> {
                val right = parseAddExpression()
                return BinaryOpNode(left, BinaryOperation.NOT_EQUALS, right)
            }

            lexer.matchAndEat(TokenType.LT) -> {
                val right = parseAddExpression()
                return BinaryOpNode(left, BinaryOperation.LESS_THAN, right)
            }

            lexer.matchAndEat(TokenType.LE) -> {
                val right = parseAddExpression()
                return BinaryOpNode(left, BinaryOperation.LESS_OR_EQUAL, right)
            }

            lexer.matchAndEat(TokenType.GT) -> {
                val right = parseAddExpression()
                return BinaryOpNode(left, BinaryOperation.GREATER_THAN, right)
            }

            lexer.matchAndEat(TokenType.GE) -> {
                val right = parseAddExpression()
                return BinaryOpNode(left, BinaryOperation.GREATER_EQUAL, right)
            }
        }
        return left
    }

    private fun parseAddExpression(): ExpressionNode {
        var res = parseMultExpression()
        while (lexer.matchAny(TokenType.PLUS, TokenType.MINUS)) {
            when {
                lexer.matchAndEat(TokenType.PLUS) -> {
                    val right = parseMultExpression()
                    res = BinaryOpNode(res, BinaryOperation.ADDITION, right)
                }

                lexer.matchAndEat(TokenType.MINUS) -> {
                    val right = parseMultExpression()
                    res = BinaryOpNode(res, BinaryOperation.SUBTRACTION, right)
                }
            }

        }
        return res
    }

    private fun parseMultExpression(): ExpressionNode {
        var res = parseInnerExpression()
        while (lexer.matchAny(TokenType.MULT, TokenType.DIV, TokenType.MOD)) {
            when {
                lexer.matchAndEat(TokenType.MULT) -> {
                    val right = parseInnerExpression()
                    res = BinaryOpNode(res, BinaryOperation.MULTIPLICATION, right)
                }

                lexer.matchAndEat(TokenType.DIV) -> {
                    val right = parseInnerExpression()
                    res = BinaryOpNode(res, BinaryOperation.DIVISION, right)
                }

                lexer.matchAndEat(TokenType.MOD) -> {
                    val right = parseInnerExpression()
                    res = BinaryOpNode(res, BinaryOperation.MODULO, right)
                }
            }

        }
        return res
    }


    private fun parseInnerExpression(): ExpressionNode {
        val left = lexer.next()
        when (left.type) {
            TokenType.INT_LITERAL -> return IntLiteralNode(left.value.toInt())
            TokenType.IDENTIFIER -> {
                if (lexer.match(TokenType.PAREN_LEFT)) {
                    lexer.consume(TokenType.PAREN_LEFT)
                    return finishFuctionCall(left)
                }
                return VariableReadNode(Identifier(left.value))
            }

            else -> lexer.syntaxError("Expected variable access or literal value, got $left", left)
        }
    }
}