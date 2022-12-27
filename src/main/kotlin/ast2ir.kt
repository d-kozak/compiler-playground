import BinaryOperation.*

class NameGen(var prefix: String = "\$") {
    val sink = Identifier("${prefix}SINK")

    var curr = 'a'

    fun next(): Identifier {
        if (curr == 'z') {
            prefix += 'z'
            curr = 'a'
        }
        return Identifier("$prefix${curr++}")
    }

}


fun compile(file: FunctionDeclarationNode): IrFunction {
    val scope = CompilationScope(file)
    scope.doCompile()
    scope.fixJumpTargets()
    return IrFunction(file.name, file.parameters, scope.instructions.toTypedArray())
}

class CompilationScope(private val file: FunctionDeclarationNode) {

    private val nameGen = NameGen()

    val instructions = mutableListOf<Instruction>()

    fun doCompile() {
        compileStatements(file.statements)
    }

    private fun compileStatements(statements: List<StatementNode>) {
        for (statement in statements) {
            compileStatement(statement)
        }
    }

    private fun compileStatement(statement: StatementNode) {
        when (statement) {
            is AssignmentNode -> compileAssignment(statement)
            is BinaryOpNode -> compileBinaryOp(nameGen.sink, statement)
            is FunctionCallNode -> compileFunctionCall(nameGen.sink, statement)
            is IntLiteralNode -> compileIntLiteral(nameGen.sink, statement)
            is VariableReadNode -> compileVariableRead(nameGen.sink, statement)
            is ArrayReadNode -> compileArrayRead(nameGen.sink, statement)
            is ReturnNode -> compileReturn(statement)
            is IfNode -> compileIf(statement)
            is WhileNode -> compileWhile(statement)
            is ForNode -> compileFor(statement)
        }
    }

    private fun compileWhile(statement: WhileNode) {
        // todo remove redundancies between while, for and if
        val condStartIndex = instructions.size
        val cond = compileExpression(statement.condition)
        val negCont = nameGen.next()
        instructions.add(Not(negCont, cond))
        val condition = CondJump(negCont)
        instructions.add(condition)

        compileStatements(statement.body)
        instructions.add(DirectJump(condStartIndex))
        condition.targetIndex = instructions.size
    }

    private fun compileFor(statement: ForNode) {
        val initExpr = statement.initExpr
        if (initExpr != null) {
            compileAssignment(initExpr)
        }
        val condStartIndex = instructions.size
        val cond = compileExpression(statement.condition)
        val negCont = nameGen.next()
        instructions.add(Not(negCont, cond))
        val condition = CondJump(negCont)
        instructions.add(condition)

        compileStatements(statement.body)

        val increment = statement.increment
        if (increment != null)
            compileAssignment(increment)

        instructions.add(DirectJump(condStartIndex))
        condition.targetIndex = instructions.size
    }

    private fun compileIf(statement: IfNode) {
        val cond = compileExpression(statement.condition)
        val negCond = nameGen.next()
        instructions.add(Not(negCond, cond))
        val condition = CondJump(negCond)
        instructions.add(condition)

        compileStatements(statement.thenStatement)


        val elseStatement = statement.elseStatement
        if (elseStatement == null) {
            if (instructions.last() !is Ret)
                instructions.add(DirectJump(instructions.size + 1)) // add an explicit jump at the end of basic block
            condition.targetIndex = instructions.size
        } else {
            val fromThenToEndJump = DirectJump()
            instructions.add(fromThenToEndJump)
            condition.targetIndex = instructions.size

            compileStatements(elseStatement)

            if (instructions.last() !is Ret)
                instructions.add(DirectJump(instructions.size + 1)) // add an explicit jump at the end of th basic block
            fromThenToEndJump.targetIndex = instructions.size
        }

    }

    private fun compileReturn(statement: ReturnNode) {
        val identifier = statement.expression?.let { compileExpression(it) }
        instructions.add(Ret(identifier))
    }

    private fun compileAssignment(statement: AssignmentNode) {
        val res = compileExpression(statement.expression)
        instructions.add(Move(statement.variable, res))
    }

    private fun compileExpression(expression: ExpressionNode): Identifier {
        val target = nameGen.next()
        when (expression) {
            is BinaryOpNode -> compileBinaryOp(target, expression)
            is FunctionCallNode -> compileFunctionCall(target, expression)
            is IntLiteralNode -> compileIntLiteral(target, expression)
            is VariableReadNode -> compileVariableRead(target, expression)
            is ArrayReadNode -> compileArrayRead(target, expression)
        }
        return target
    }

    private fun compileVariableRead(target: Identifier, expression: VariableReadNode) {
        instructions.add(Move(target, expression.identifier))
    }

    private fun compileArrayRead(target: Identifier, statement: ArrayReadNode) {
        val arrayBase = compileExpression(statement.arrayExpr)
        val index = compileExpression(statement.indexExpr)
        instructions.add(ArrayRead(target, arrayBase, index))
    }

    private fun compileIntLiteral(target: Identifier, expression: IntLiteralNode) {
        instructions.add(
            Move(
                target,
                IntConstant(expression.value)
            )
        )
    }

    private fun compileFunctionCall(target: Identifier, expression: FunctionCallNode) {
        val args = expression.args.map { compileExpression(it) }
        instructions.add(FunctionCall(target, expression.name, args))
    }

    private fun compileBinaryOp(target: Identifier, binaryOpNode: BinaryOpNode) {
        val left = compileExpression(binaryOpNode.left)
        val right = compileExpression(binaryOpNode.right)
        val instr = when (binaryOpNode.operation) {
            ADDITION -> Add(target, left, right)
            SUBTRACTION -> Sub(target, left, right)
            MULTIPLICATION -> Mult(target, left, right)
            DIVISION -> Div(target, left, right)
            MODULO -> Mod(target, left, right)
            EQUALS -> Eq(target, left, right)
            NOT_EQUALS -> Neq(target, left, right)
            LESS_THAN -> Lt(target, left, right)
            LESS_OR_EQUAL -> Le(target, left, right)
            GREATER_THAN -> Gt(target, left, right)
            GREATER_EQUAL -> Ge(target, left, right)
        }
        instructions.add(instr)
    }

    fun fixJumpTargets() {
        val labelGen = NameGen("label_")
        for (instruction in instructions.toList()) {
            if (instruction is JumpInstruction) {
                // if the jumps lead to end of functions, there is no corresponding instruction to jump to
                // for now, insert noop there as a jump target
                while (instruction.targetIndex >= instructions.size) {
                    instructions.add(Noop())
                }
                instructions[instruction.targetIndex].jumpedFrom.add(instruction)
                instruction.target = instructions[instruction.targetIndex]
                if (instruction.target.label == null)
                    instruction.target.label = labelGen.next()
            }
        }
    }
}


