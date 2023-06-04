class AstVizInfo(
    val id: Int,
    val desc: String,
    val children: List<AstVizInfo>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AstVizInfo

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}

fun serializeAst(root: FileNode): String = AstViz().serialize(root)

private class AstViz {

    private var cnt = 0

    private val nodes = mutableListOf<AstVizInfo>()

    private fun nextNode(desc: String, children: List<AstVizInfo>) =
        AstVizInfo(++cnt, desc, children.toList()).also { nodes.add(it) }

    private fun nextNode(desc: String, vararg children: AstVizInfo) = nextNode(desc, children.toList())


    private fun buildVizTree(node: AstNode): AstVizInfo = when (node) {
        is FileNode -> nextNode("File: ${node.fileName}", node.statements.map { buildVizTree(it) })
        is StatementNode -> {
            when (node) {
                is AssignmentNode -> nextNode("=", nextNode(node.target.name), buildVizTree(node.expression))

                is PrintNode -> nextNode("print", nextNode(node.source.name))
            }
        }

        is ExpressionNode -> {
            when (node) {
                is AddNode -> nextNode("+", buildVizTree(node.left), buildVizTree(node.right))
                is LiteralNode -> nextNode(node.value.toString())
                is VariableLoadNode -> nextNode(node.source.name)
            }
        }
    }

    fun serialize(fileNode: FileNode): String = buildString {
        buildVizTree(fileNode)
        appendLine("flowchart TD")
        for (node in nodes) {
            append("n")
            append(node.id)
            append("([")
            append(node.desc)
            appendLine("])")
        }
        for (node in nodes) {
            for (child in node.children) {
                append("n")
                append(node.id)
                append(" --> ")
                append("n")
                appendLine(child.id)
            }
        }
    }
}

fun serializeIr(ir: IrFunction): String = buildString {
    for (instruction in ir.instructions) {
        when (instruction) {
            is Add -> appendLine("add ${instruction.target}, ${instruction.left}, ${instruction.right}")
            is Mov -> appendLine("mov ${instruction.target}, ${instruction.source}")
            is Print -> appendLine("print ${instruction.source}")
        }
    }
}
