import java.io.File

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Expecting a source file to process" }
    val fileName = args[0]
    compileFile(File(fileName))
}

fun compileFile(filename: String) = compileFile(File(filename))

fun compileFile(file: File) {
    val content = file.readText()
    val parser = Parser(content)
    val ast = parser.parse(file.name)
    val ir = astToIr(ast)
    val serialized = mermaidSerialize(content, ast, ir)
    File("${file.name}.out.md").printWriter().use {
        it.println(serialized)
    }
}

private fun mermaidSerialize(content: String, ast: FileNode, ir: IrFunction) = buildString {
    appendLine("# File: ${ast.fileName}")
    appendLine("## Code:")
    appendLine("```")
    appendLine(content)
    appendLine("```")
    appendLine("## Ast:")
    appendLine("```mermaid")
    appendLine(serializeAst(ast))
    appendLine("```")
    appendLine("## IR:")
    appendLine("```mermaid")
    appendLine("flowchart TD")
    appendLine("B1[\"")
    append(serializeIr(ir))
    appendLine("\"]")
    appendLine("```")
}

