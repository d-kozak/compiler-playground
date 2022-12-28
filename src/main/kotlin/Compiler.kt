import asm.Aarch64Assembler
import passes.*
import passes.basicblock.DirectConstantPropagationPass
import passes.cfg.PropagateVariablesWithSingleWrites
import java.io.File


val ALL_TOP_LEVEL_PASSES = listOf(
    SimplifyJumpConditions(),
    EvaluateConstantExpressions(),
    RemoveCompletelyUnusedAssignments(),
    RemoveNoopPass()
)
val ALL_BLOCK_LEVEL_PASSES = listOf(DirectConstantPropagationPass())
val ALL_CFG_PASSES = listOf(PropagateVariablesWithSingleWrites())

class Compiler(
    private val config: CompilerConfig,
) {

    private val debugDump = DebugDump(config)
    fun runAll() {
        val fileName = config.inputFile ?: "programs/source/selection_sort.prog"
        try {
            val root = parseFile(fileName)
            val irFunctions = lowerToIr(root)
            optimize(irFunctions)

            interpret(irFunctions)

            lowerToAsm(irFunctions, fileName)
        } finally {
            debugDump.onCompilationFinished()
        }
    }

    private fun lowerToAsm(irFunctions: MutableList<IrFunction>, fileName: String) {
        val assembler = Aarch64Assembler(irFunctions, debugDump)
        assembler.gen()
        println(assembler.buffer.toString())
        debugDump.asm(fileName, assembler)
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
            // todo create some kind of optimization plan/phase to properly group all the optimizations
            applyTopLevelPasses(irFunction)

            val cfg = computeCfg(irFunction)

            val str = dumpCfg(cfg)
            verbose(str)
            debugDump.dump(cfg, "After CFG creation")

            applyBasicBlockPasses(cfg)

            for (cfgPass in ALL_CFG_PASSES) {
                cfgPass.apply(cfg)
                debugDump.dump(cfg, cfgPass)
            }

            applyTopLevelPasses(irFunction)

            debugDump.dumpFinalCfg(cfg)
        }
    }

    private fun applyTopLevelPasses(irFunction: IrFunction) {
        for (pass in ALL_TOP_LEVEL_PASSES) {
            pass.apply(irFunction)
            debugDump.dump(irFunction, pass)
        }
    }

    private fun applyBasicBlockPasses(cfg: ControlFlowGraph) {
        for (pass in ALL_BLOCK_LEVEL_PASSES) {
            for (block in cfg.basicBlocks) {
                pass.apply(block)
            }
            debugDump.dump(cfg, pass)
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

