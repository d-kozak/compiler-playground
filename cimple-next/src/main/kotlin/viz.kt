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

fun serialize(root: FileNode): String = AstViz().serialize(root)

private class AstViz {

    private var cnt = 0

    private val nodes = mutableListOf<AstVizInfo>()

    private fun nextNode(desc: String, children: List<AstVizInfo>) =
        AstVizInfo(++cnt, desc, children.toList()).also { nodes.add(it) }

    private fun nextNode(desc: String, vararg children: AstVizInfo) = nextNode(desc, children.toList())


    private fun buildVizTree(node: AstNode): AstVizInfo = when (node) {
        is FileNode -> nextNode("File: ${node.fileName}", node.statements.map { buildVizTree(it) })
        is Statement -> {
            when (node) {
                is Assignment -> nextNode("=", nextNode(node.target), buildVizTree(node.expression))

                is Print -> nextNode("print", nextNode(node.source))
            }
        }

        is Expression -> {
            when (node) {
                is Add -> nextNode("+", buildVizTree(node.left), buildVizTree(node.right))
                is LiteralExpression -> nextNode(node.value.toString())
                is VariableLoad -> nextNode(node.source)
            }
        }
    }

    fun serialize(fileNode: FileNode): String = buildString {
        buildVizTree(fileNode)
        appendLine("flowchart TD")
        for (node in nodes) {
            append("n")
            append(node.id)
            append("[(")
            append(node.desc)
            appendLine(")]")
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

