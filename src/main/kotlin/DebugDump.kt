import asm.Aarch64Assembler
import java.io.FileWriter
import kotlin.io.path.Path
import kotlin.io.path.name

val DEFAULT_DUMP_DIR = Path("/Users/dkozak-mac-uni/projects/compiler-playground/tmp")

class DebugDump(val config: CompilerConfig) {

    private val contexts = mutableMapOf<Identifier, StringBuilder>()

    inline operator fun invoke(fileName: String, block: FileWriter.() -> Unit) {
        val dumpDir = DEFAULT_DUMP_DIR.resolve(fileName)
        FileWriter(dumpDir.toFile()).use(block)
    }

    fun dump(cfg: ControlFlowGraph, pass: Any) = dump(cfg, "After ${pass.javaClass.name}")

    fun dump(cfg: ControlFlowGraph, header: String) = withContext(cfg.function.name) {
        header(header)
        append(dumpCfg(cfg))
        footer()
    }

    private fun StringBuilder.header(header: String) {
        appendLine("===$header===")
    }

    private fun StringBuilder.footer() {
        appendLine("======")
    }

    fun dump(f: IrFunction, pass: Any) = dump(f, "After ${pass.javaClass.name}")
    fun dump(f: IrFunction, header: String) = withContext(f.name) {
        header(header)
        append(dumpInstructions(f))
        footer()
    }

    private fun withContext(identifier: Identifier, block: StringBuilder.() -> Unit) {
        val ctx = contexts.computeIfAbsent(identifier) { StringBuilder() }
        block(ctx)
    }

    fun onCompilationFinished() {
        for ((id, value) in contexts) {
            val targetFile = DEFAULT_DUMP_DIR.resolve(id.name)
            FileWriter(targetFile.toFile()).use { it.write(value.toString()) }
        }
    }

    fun dumpFinalCfg(cfg: ControlFlowGraph) {
        val targetFile = DEFAULT_DUMP_DIR.resolve("${cfg.function.name}.dot")
        FileWriter(targetFile.toFile()).use { it.write(dumpCfg(cfg)) }
    }

    fun asm(fileName: String, assembler: Aarch64Assembler) {
        val path = Path(fileName)
        val targetFile = DEFAULT_DUMP_DIR.resolve("${path.name}.s")
        FileWriter(targetFile.toFile()).use { it.write(assembler.dumpAsm()) }
    }
}