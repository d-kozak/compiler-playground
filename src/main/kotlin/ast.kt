import java.io.File

// TODO source locations - how to nicely insert them?

data class FileContentNode(val file: File, val declarations: List<TopLevelDeclaration>)

data class Identifier(val name: String)


sealed class TopLevelDeclaration
data class FunctionDeclarationNode(
    val name: Identifier,
    val parameters: List<Identifier>,
    val statements: List<StatementNode>
) : TopLevelDeclaration()

enum class BinaryOperation {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    EQUALS,
    NOT_EQUALS,
    LESS_THAN,
    LESS_OR_EQUAL,
    GREATER_THAN,
    GREATER_EQUAL
}

sealed class StatementNode
data class AssignmentNode(val variable: Identifier, val expression: ExpressionNode) : StatementNode()
data class ReturnNode(val expression: ExpressionNode?) : StatementNode()

data class IfNode(val condition: ExpressionNode, val thenStatement: List<StatementNode>) : StatementNode()
data class WhileNode(val condition: ExpressionNode, val body: List<StatementNode>) : StatementNode()

sealed class ExpressionNode : StatementNode()
data class BinaryOpNode(val left: ExpressionNode, val operation: BinaryOperation, val right: ExpressionNode) :
    ExpressionNode()

data class FunctionCallNode(val name: Identifier, val args: List<ExpressionNode>) : ExpressionNode()
data class IntLiteralNode(val value: Int) : ExpressionNode()
data class VariableReadNode(val identifier: Identifier) : ExpressionNode()