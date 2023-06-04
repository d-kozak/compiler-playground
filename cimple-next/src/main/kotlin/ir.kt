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


fun irInterpreter(irFunction: IrFunction) {
    val scope = mutableMapOf<Identifier, Long>()

    fun lookup(x: IdentifierOrConstant): Long = when (x) {
        is Constant -> x.value
        is Identifier -> scope[x] ?: error("Unknown id $x")
    }

    for (instruction in irFunction.instructions) {
        when (instruction) {
            is Add -> {
                val left = lookup(instruction.left)
                val right = lookup(instruction.right)
                scope[instruction.target] = left + right
            }

            is Mov -> {
                scope[instruction.target] = lookup(instruction.source)
            }

            is Print -> {
                println(scope[instruction.source])
            }
        }
    }
}