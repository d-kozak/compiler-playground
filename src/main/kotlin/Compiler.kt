import passes.RemoveNoopPass
import passes.SimplifyJumpConditions
import passes.basicblock.DirectConstantPropagationPass
import passes.setInstructionIndexes
import java.io.File


val ALL_TOP_LEVEL_PASSES = listOf(SimplifyJumpConditions(), RemoveNoopPass())
val ALL_BLOCK_LEVEL_PASSES = listOf(DirectConstantPropagationPass())


class Compiler(
    private val config: CompilerConfig,
) {

    private val debugDump = DebugDump(config)
    fun runAll() {
        val fileName = config.inputFile ?: "programs/source/fib.prog"
        val root = parseFile(fileName)
        val irFunctions = lowerToIr(root)
        optimize(irFunctions)
        interpret(irFunctions)
    }

    private fun optimize(irFunctions: MutableList<IrFunction>) {
        for (irFunction in irFunctions) {
            val cfg = computeCfg(irFunction)

            val str = printCfg(cfg)
            verbose(str)
            debugDump("${irFunction.name}.cfg") { write(str) }

            for (pass in ALL_BLOCK_LEVEL_PASSES) {
                for (block in cfg.basicBlocks) {
                    pass.apply(block)
                }
                debugDump("${irFunction.name}.cfg_after_${pass.javaClass.name}") { write(printCfg(cfg)) }
            }
        }
    }

    private fun lowerToIr(root: FileContentNode): MutableList<IrFunction> {
        val irFunctions = mutableListOf<IrFunction>()
        for (decl in root.declarations) {
            val ir = compile(decl as FunctionDeclarationNode)
            setInstructionIndexes(ir)
            var str = dumpInstructions(ir)
            verbose(str)
            debugDump("${decl.name}.ir") { write(str) }
            irFunctions.add(ir)

            for (pass in ALL_TOP_LEVEL_PASSES) {
                pass.apply(ir)
                debugDump("${decl.name}.ir_after_${pass.javaClass.name}") { write(dumpInstructions(ir)) }
            }

            verbose("After opts")
            str = dumpInstructions(ir)
            verbose(str)
        }
        return irFunctions
    }
}

fun parseFile(fileName: String): FileContentNode {
    val file = File(fileName)
    val input = file.readText()
    val lexer = Lexer(input)
    val parser = Parser(lexer, file)
    return parser.parseFile()
}

