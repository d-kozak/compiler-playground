import java.io.File


fun main(args: Array<String>) {
    val fileName = if (args.size == 1) args[0] else "programs/source/fib.prog"
    val root = parseFile(fileName)
    val irFunctions = mutableListOf<IrFunction>()
    for (decl in root.declarations) {
        val ir = compile(decl as FunctionDeclarationNode)
        printInstructions(ir)
        irFunctions.add(ir)
    }
    for (irFunction in irFunctions) {
        val cfg = computeCfg(irFunction)
        val str = printCfg(cfg)
        println(str)
    }
    interpretIr(irFunctions)
}

fun parseFile(fileName: String): FileContentNode {
    val file = File(fileName)
    val input = file.readText()
    val lexer = Lexer(input)
    val parser = Parser(lexer, file)
    val root = parser.parseFile()
    return root
}