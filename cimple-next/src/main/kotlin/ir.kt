sealed interface IrInstruction

class Print(val source: Identifier) : IrInstruction
class Mov(val target: Identifier, val source: IdentifierOrConstant) : IrInstruction
class Add(val target: Identifier, val left: IdentifierOrConstant, val right: IdentifierOrConstant) : IrInstruction

data class IrFunction(val instructions: List<IrInstruction>)

class IdGen {
    private var cnt = 1

    fun next() = Identifier("\$${cnt++}")
}

fun astToIr(ast: FileNode): IrFunction {
    val instructions = mutableListOf<IrInstruction>()
    val idGen = IdGen()
    fun dfs(node: AstNode): IdentifierOrConstant = when (node) {
        is AddNode -> {
            val left = dfs(node.left)
            val right = dfs(node.right)
            val target = idGen.next()
            instructions.add(Add(target, left, right))
            target
        }

        is LiteralNode -> {
            node.value
        }

        is VariableLoadNode -> {
            node.source
        }

        is AssignmentNode -> {
            val expr = dfs(node.expression)
            instructions.add(Mov(node.target, expr))
            node.target
        }

        is PrintNode -> {
            instructions.add(Print(node.source))
            INVALID_ID
        }

        else -> TODO("should not reach here")
    }

    for (statement in ast.statements) {
        dfs(statement)
    }
    return IrFunction(instructions)
}
