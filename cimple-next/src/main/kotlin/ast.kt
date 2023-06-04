sealed interface AstNode

sealed interface IdentifierOrConstant
data class Identifier(val name: String) : IdentifierOrConstant {
    override fun toString(): String = name
}

data class Constant(val value: Long) : IdentifierOrConstant {
    override fun toString(): String = value.toString()
}

val INVALID_ID = Identifier("INVALID")


class FileNode(
    val fileName: String,
    val statements: MutableList<StatementNode>
) : AstNode

sealed interface StatementNode : AstNode

class AssignmentNode(
    val target: Identifier,
    val expression: ExpressionNode
) : StatementNode

class PrintNode(
    val source: Identifier
) : StatementNode


sealed interface ExpressionNode : AstNode

class LiteralNode(
    val value: Constant
) : ExpressionNode

class VariableLoadNode(
    val source: Identifier
) : ExpressionNode

class AddNode(
    val left: ExpressionNode,
    val right: ExpressionNode
) : ExpressionNode