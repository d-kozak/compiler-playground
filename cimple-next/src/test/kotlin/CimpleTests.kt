import org.junit.jupiter.api.Test
import java.io.File

class CimpleTests {

    private fun compileAllIn(directory: File) {
        val sourceFiles = directory.listFiles { f -> f.endsWith(".ci") }
        for (file in sourceFiles) {
            compileFile(file)
        }

    }

    @Test
    fun v1Add() {
        compileFile("programs/v1/add.ci")
    }

    @Test
    fun v2Add() {
        compileFile("programs/v1/add2.ci")
    }
}