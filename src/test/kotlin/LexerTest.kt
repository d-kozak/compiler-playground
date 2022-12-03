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

    @Test
    fun `consume first few tokens`() {
        val lexer = Lexer(readFile(testFile(INC_INPUT)))
        lexer.consume(TokenType.KEYWORD_FUN)
        lexer.consume(TokenType.IDENTIFIER)
        lexer.consume(TokenType.PAREN_LEFT)
        lexer.consume(TokenType.PAREN_RIGHT)
    }

    @Test
    fun `match first few tokens`() {
        val lexer = Lexer(readFile(testFile(INC_INPUT)))
        check(lexer.match(TokenType.KEYWORD_FUN))
        lexer.consume(TokenType.KEYWORD_FUN)
        check(lexer.match(TokenType.IDENTIFIER))
        lexer.consume(TokenType.IDENTIFIER)
        check(lexer.match(TokenType.PAREN_LEFT))
        lexer.consume(TokenType.PAREN_LEFT)
        check(lexer.match(TokenType.PAREN_RIGHT))
    }

    private fun readFile(fileName: String) = File(fileName).readText()
}