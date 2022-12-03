import java.io.File


fun main(args: Array<String>) {
    val file = if (args.size == 1) args[0] else "programs/source/inc_input.prog"
    val input = File(file).readText()
    val lexer = Lexer(input)
    while (lexer.hasText()) {
        println(lexer.next())
    }
}