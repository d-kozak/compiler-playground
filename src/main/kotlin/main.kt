import java.io.File


fun main(args: Array<String>) {
    val fileName = if (args.size == 1) args[0] else "programs/source/inc_input.prog"
    val root = parseFile(fileName)
    println(root)
}

fun parseFile(fileName: String): FileContentNode {
    val file = File(fileName)
    val input = file.readText()
    val lexer = Lexer(input)
    val parser = Parser(lexer, file)
    val root = parser.parseFile()
    return root
}