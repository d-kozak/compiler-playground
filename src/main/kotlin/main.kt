import passes.RemoveNoopPass
import passes.SimplifyJumpConditions
import passes.basicblock.DirectConstantPropagationPass
import passes.setInstructionIndexes
import java.io.File
import java.io.FileWriter
import kotlin.io.path.Path

val TEMP_DIR = Path("/Users/dkozak-mac-uni/projects/compiler-playground/tmp")


inline fun dumpTo(fileName: String, block: FileWriter.() -> Unit) {
    val dumpDir = TEMP_DIR.resolve(fileName)
    FileWriter(dumpDir.toFile()).use(block)
}

val passes = listOf(SimplifyJumpConditions(), RemoveNoopPass())
val basicBlockPasses = listOf(DirectConstantPropagationPass())
fun main(args: Array<String>) {
    val fileName = if (args.size == 1) args[0] else "programs/source/fib.prog"
    val root = parseFile(fileName)
    val irFunctions = mutableListOf<IrFunction>()
    for (decl in root.declarations) {
        val ir = compile(decl as FunctionDeclarationNode)
        setInstructionIndexes(ir)
        var str = dumpInstructions(ir)
        println(str)
        dumpTo("${decl.name}.ir") { write(str) }
        irFunctions.add(ir)

        for (pass in passes) {
            pass.apply(ir)
            dumpTo("${decl.name}.ir_after_${pass.javaClass.name}") { write(dumpInstructions(ir)) }
        }

        println("After opts")
        str = dumpInstructions(ir)
        println(str)
    }


    for (irFunction in irFunctions) {
        val cfg = computeCfg(irFunction)


        val str = printCfg(cfg)
        println(str)
        dumpTo("${irFunction.name}.cfg") { write(str) }

        for (pass in basicBlockPasses) {
            for (block in cfg.basicBlocks) {
                pass.apply(block)
            }
            dumpTo("${irFunction.name}.cfg_after_${pass.javaClass.name}") { write(printCfg(cfg)) }
        }
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