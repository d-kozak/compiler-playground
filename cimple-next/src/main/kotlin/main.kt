import java.io.File

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Expecting a source file to process" }
    val content = File(args[0]).readText()
    val parser = Parser(content)
    val ast = parser.parse()
    println(ast)
}