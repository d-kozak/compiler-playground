import java.io.File
import kotlin.test.Test

class LexerTest {



    @Test
    fun `lex the whole multi adds file`() {
        val lexer = Lexer(readFile(testFile(MULTIPLE_ADDS)))
        while (lexer.hasText()) {
            println(lexer.next())
        }
    }

    @Test
    fun `lex the whole multi math file`() {
        val lexer = Lexer(readFile(testFile(MATH)))
        while (lexer.hasText()) {
            val token = lexer.next()
            println(token)
        }
    }

    private fun readFile(fileName: String) = File(fileName).readText()
}