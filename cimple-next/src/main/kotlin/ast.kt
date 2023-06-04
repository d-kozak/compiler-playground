sealed interface AstNode

typealias Identifier = String

class FileNode(
    val fileName: String,
    val statements: MutableList<Statement>
) : AstNode

sealed interface Statement : AstNode

class Assignment(
    val target: Identifier,
    val expression: Expression
) : Statement

class Print(
    val source: Identifier
) : Statement


sealed interface Expression : AstNode

class LiteralExpression(
    val value: Long
) : Expression

class VariableLoad(
    val source: Identifier
) : Expression

class Add(
    val left: Expression,
    val right: Expression
) : Expression