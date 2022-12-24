fun dumpInstructions(func: IrFunction): String {
    val scope = PrintingScope()
    scope.process(func)
    return scope.toString()
}

fun dumpBasicBlock(basicBlock: BasicBlock): String {
    val scope = PrintingScope()
    scope.dumpInstructions(basicBlock.instructions.toTypedArray(), true)
    return scope.toString()
}

private class PrintingScope {

    val buffer = StringBuffer()

    fun process(func: IrFunction) {
        appendLabel(func.name)
        dumpInstructions(func.instructions, true)
    }

    fun dumpInstructions(instructions: Array<Instruction>, dumpLabels: Boolean) {
        for (inst in instructions) {
            val label = inst.label
            if (dumpLabels && label != null) {
                appendLabel(label)
            }
            when (inst) {
                is BinaryInstruction -> appendBinaryInstruction(inst)

                is Not -> appendNot(inst)
                is Noop -> appendNoop(inst)

                is Move -> appendMove(inst)

                is FunctionCall -> appendFunctionCall(inst)

                is Ret -> appendRet(inst)


                is DirectJump -> appendDirectJump(inst)
                is CondJump -> appendConditionalJump(inst)

                else -> internalError("Should never get here") // special method for non-exhaustive when?
            }
        }
    }

    private fun appendConditionalJump(inst: CondJump) {
        buffer.append('\t')
            .append("COND JUMP ")
            .append(inst.condition)
            .append(' ')
            .appendLine(inst.target.label!!.name)
    }

    private fun appendDirectJump(inst: DirectJump) {
        buffer.append('\t')
            .append("JUMP ")
            .appendLine(inst.target.label!!.name)
    }

    private fun appendRet(inst: Ret) {
        buffer.append('\t')
            .append("return ")
        val value = inst.value
        if (value != null) {
            buffer.append(value)
        }
        buffer.appendLine()
    }

    private fun appendFunctionCall(inst: FunctionCall) {
        appendTarget(inst.target)
            .append(inst.functionName)
            .append('(')
        for ((i, arg) in inst.args.withIndex()) {
            if (i > 0) buffer.append(", ")
            buffer.append(arg)
        }
        buffer.appendLine(')')
    }


    private fun appendMove(inst: Move) {
        appendTarget(inst.target)
            .appendLine(inst.source.toString())
    }


    private fun appendNoop(inst: Noop) {
        buffer.append('\t')
            .appendLine(inst.name)
    }

    private fun appendNot(inst: Not) {
        appendTarget(inst.target)
            .append('!')
            .appendLine(inst.source.toString())
    }

    private fun appendBinaryInstruction(inst: BinaryInstruction) {
        val op = when (inst) {
            is Add -> BinaryOperation.ADDITION
            is Sub -> BinaryOperation.SUBTRACTION
            is Mult -> BinaryOperation.MULTIPLICATION
            is Div -> BinaryOperation.DIVISION
            is Eq -> BinaryOperation.EQUALS
            is Neq -> BinaryOperation.NOT_EQUALS
            is Lt -> BinaryOperation.LESS_THAN
            is Le -> BinaryOperation.LESS_OR_EQUAL
            is Ge -> BinaryOperation.GREATER_EQUAL
            is Gt -> BinaryOperation.GREATER_THAN
            else -> internalError("bad op $inst")
        }
        appendTarget(inst.target)
            .append(inst.left)
            .append(' ')
            .append(op.symbol)
            .append(' ')
            .appendLine(inst.right.toString())
    }

    private fun appendTarget(identifier: Identifier): StringBuffer = buffer.append('\t')
        .append(identifier)
        .append(" = ")

    private fun appendLabel(id: Identifier) {
        buffer.append(id.name)
            .appendLine(':')

    }


    override fun toString(): String {
        return buffer.toString()
    }

}
