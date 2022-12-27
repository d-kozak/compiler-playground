import java.io.FileWriter
import kotlin.io.path.Path

val DEFAULT_DUMP_DIR = Path("/Users/dkozak-mac-uni/projects/compiler-playground/tmp")

class DebugDump(val config: CompilerConfig) {

    inline operator fun invoke(fileName: String, block: FileWriter.() -> Unit) {
        val dumpDir = DEFAULT_DUMP_DIR.resolve(fileName)
        FileWriter(dumpDir.toFile()).use(block)
    }
}