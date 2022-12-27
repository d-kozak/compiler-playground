import java.io.File

// TODO source locations - how to nicely insert them?

data class FileContentNode(val file: File, val declarations: List<TopLevelDeclaration>)


sealed interface IdentifierOrValue

data class Identifier(val name: String) : IdentifierOrValue {
    override fun toString(): String = name
}

data class IntConstant(val value: Int) : IdentifierOrValue, Value {
    override fun toString(): String = value.toString()

    override fun plus(other: Value): Value = IntConstant(value + unwrap(other))

    override fun minus(other: Value): Value = IntConstant(value - unwrap(other))

    override fun times(other: Value): Value = IntConstant(value * unwrap(other))

    override fun div(other: Value): Value = IntConstant(value / unwrap(other))

    override fun rem(other: Value): Value = IntConstant(value % unwrap(other))

    override fun compareTo(other: Value): Int = value.compareTo(unwrap(other))

    private fun unwrap(other: Value) = (other as IntConstant).value
}


sealed class TopLevelDeclaration
data class FunctionDeclarationNode(
    val name: Identifier,
    val parameters: List<Identifier>,
    val statements: List<StatementNode>
) : TopLevelDeclaration()

enum class BinaryOperation(val symbol: String) {
    ADDITION("+"),
    SUBTRACTION("-"),
    MULTIPLICATION("*"),
    DIVISION("/"),
    MODULO("%"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_EQUAL(">=")
}

sealed class StatementNode
data class AssignmentNode(val variable: Identifier, val expression: ExpressionNode) : StatementNode()
data class ReturnNode(val expression: ExpressionNode?) : StatementNode()

data class IfNode(
    val condition: ExpressionNode,
    val thenStatement: List<StatementNode>,
    val elseStatement: List<StatementNode>? = null
) : StatementNode()

data class WhileNode(val condition: ExpressionNode, val body: List<StatementNode>) : StatementNode()

data class ForNode(
    // todo forcing init and inc to be assignments is in a way too restrictive
    val initExpr: AssignmentNode?,
    val condition: ExpressionNode,
    val increment: AssignmentNode?,
    val body: List<StatementNode>
) :
    StatementNode()

sealed class ExpressionNode : StatementNode()
data class BinaryOpNode(val left: ExpressionNode, val operation: BinaryOperation, val right: ExpressionNode) :
    ExpressionNode()

data class FunctionCallNode(val name: Identifier, val args: List<ExpressionNode>) : ExpressionNode()
data class IntLiteralNode(val value: Int) : ExpressionNode()
data class VariableReadNode(val identifier: Identifier) : ExpressionNode()

data class ArrayReadNode(
    val arrayExpr: ExpressionNode,
    val indexExpr: ExpressionNode
) : ExpressionNode()