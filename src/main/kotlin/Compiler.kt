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
        debugDump.onCompilationFinished()
        interpret(irFunctions)
    }

    private fun lowerToIr(root: FileContentNode): MutableList<IrFunction> {
        val irFunctions = mutableListOf<IrFunction>()
        for (decl in root.declarations) {
            val ir = compile(decl as FunctionDeclarationNode)
            setInstructionIndexes(ir)
            irFunctions.add(ir)
            val str = dumpInstructions(ir)
            verbose(str)
            debugDump.dump(ir, "After lowering")
        }
        return irFunctions
    }

    private fun optimize(irFunctions: MutableList<IrFunction>) {
        for (irFunction in irFunctions) {

            for (pass in ALL_TOP_LEVEL_PASSES) {
                pass.apply(irFunction)
                debugDump.dump(irFunction, pass)
            }


            val cfg = computeCfg(irFunction)

            val str = dumpCfg(cfg)
            verbose(str)
            debugDump.dump(cfg, "After CFG creation")

            for (pass in ALL_BLOCK_LEVEL_PASSES) {
                for (block in cfg.basicBlocks) {
                    pass.apply(block)
                }
                debugDump.dump(cfg, pass)
            }
        }
    }
}

fun parseFile(fileName: String): FileContentNode {
    val file = File(fileName)
    val input = file.readText()
    val lexer = Lexer(input)
    val parser = Parser(lexer, file)
    return parser.parseFile()
}
