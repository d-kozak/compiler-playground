import kotlin.test.Test

class ParserTest {


    @Test
    fun `parse add`() {
        val root = parseFile(testFile(ADD))
        println(root)
    }

    @Test
    fun `multiple adds`() {
        val root = parseFile(testFile(MULTIPLE_ADDS))
        println(root)
    }

    @Test
    fun `math operations`() {
        val root = parseFile(testFile(MATH))
        println(root)
    }

    @Test
    fun `fib`() {
        val root = parseFile(testFile(FIB))
        println(root)
    }

}