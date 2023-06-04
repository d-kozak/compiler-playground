import java.io.File

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Expecting a source file to process" }
    val fileName = args[0]
    val content = File(fileName).readText()
    val parser = Parser(content)
    val ast = parser.parse(fileName)
    val serialized = serialize(ast)
    println("Code:")
    println("```")
    println(content)
    println("```")
    println("Ast")
    println("```mermaid")
    println(serialized)
    println("```")
}