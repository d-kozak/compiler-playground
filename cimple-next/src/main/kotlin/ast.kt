interface Ast

typealias Identifier = String

class FileNode(
    val statement: MutableList<Statement>
) : Ast

sealed interface Statement : Ast

class Assignment(
    val target: Identifier,
    val expression: Expression
) : Statement

class Print(
    val source: Identifier
) : Statement


sealed interface Expression : Ast

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