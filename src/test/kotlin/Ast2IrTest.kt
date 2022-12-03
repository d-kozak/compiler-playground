import kotlin.test.Test

class Ast2IrTest {

    @Test
    fun fib() {
        val fileNode = parseFile(testFile(FIB))
        for (declaration in fileNode.declarations) {
            val irFunc = compile(declaration as FunctionDeclarationNode)
            println(irFunc)
        }
    }
}