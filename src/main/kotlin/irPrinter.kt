fun printInstructions(func: IrFunction) {
    val scope = PrintingScope(func)
    scope.process()
    println(scope.toString())
}


private class PrintingScope(val func: IrFunction) {

    val buffer = StringBuffer()

    fun process() {
        appendLabel(func.name)
        for (inst in func.instructions) {
            val label = inst.label
            if (label != null) {
                appendLabel(label)
            }
            when (inst) {
                is Add -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Sub -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Mult -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Div -> appendInstruction(inst.name, inst.target, inst.left, inst.right)

                is Eq -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Neq -> appendInstruction(inst.name, inst.target, inst.left, inst.right)

                is Ge -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Gt -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Le -> appendInstruction(inst.name, inst.target, inst.left, inst.right)
                is Lt -> appendInstruction(inst.name, inst.target, inst.left, inst.right)

                is Not -> appendInstruction(inst.name, inst.target, inst.source)
                is Noop -> appendInstruction(inst.name)

                is Move -> appendInstruction(inst.name, inst.target, inst.source)
                is MoveConst -> appendInstruction(inst.name, inst.target, inst.constant.asId())

                is FunctionCall -> appendInstruction(
                    inst.name,
                    inst.target,
                    inst.functionName,
                    *inst.args.toTypedArray()
                )

                is Ret -> {
                    val value = inst.value
                    if (value != null) appendInstruction(inst.name, value) else appendInstruction(inst.name)
                }


                is DirectJump -> appendInstruction(inst.name, inst.target.label!!)
                is CondJump -> appendInstruction(inst.name, inst.condition, inst.target.label!!)

                is JumpInstruction -> internalError("Should never get here, covered by the jumps above")
            }
        }
    }

    private fun appendLabel(id: Identifier) {
        buffer.append(id.name)
            .appendLine(':')

    }

    private fun appendInstruction(name: String, vararg args: Identifier) {
        buffer.append('\t')
            .append(name)
        for (arg in args) {
            buffer.append(' ')
                .append(arg.name)
        }
        buffer.appendLine()
    }

    override fun toString(): String {
        return buffer.toString()
    }

}

private fun IntConstant.asId(): Identifier = Identifier(value.toString())