fun computeCfg(function: IrFunction): ControlFlowGraph {
    val cfg = ControlFlowGraph(function)
    cfg.computeBasicBlocks()
    cfg.findJumpTargets()
    return cfg
}

fun printCfg(cfg: ControlFlowGraph): String = buildString {
    append("digraph ")
    append(cfg.function.name)
    append(" {")
    appendLine()
    appendLine()

    var curr = 'a'
    val blockNames = mutableMapOf<BasicBlock, String>()

    for (basicBlock in cfg.basicBlocks) {
        val name = curr.toString()
        blockNames[basicBlock] = name
        curr++
        append(name)
        append("[label=\"")
        appendLine()
        append(dumpBasicBlock(basicBlock))
        append("\"];")
        appendLine()
    }

    appendLine()

    for ((from,successors) in cfg.successors.entries) {
        for (successor in successors) {
            append(blockNames[from])
            append(" -> ")
            append(blockNames[successor])
            appendLine()
        }
    }

    append("}")
    appendLine()
}

data class BasicBlock(
    val instructions: MutableList<Instruction> = mutableListOf()
)

data class ControlFlowGraph(
    val function: IrFunction,
    val basicBlocks: MutableList<BasicBlock> = mutableListOf(),
    val instructionToBlock: MutableMap<Instruction, BasicBlock> = mutableMapOf(),
    val successors: MutableMap<BasicBlock, MutableList<BasicBlock>> = mutableMapOf()
) {
    fun computeBasicBlocks() {
        val curr = mutableListOf<Instruction>()
        for (instruction in function.instructions) {
            var processed = false
            if (instruction.label != null) {
                newBasicBlock(curr)
                curr.clear()
                curr.add(instruction)
                processed = true
            }
            if (instruction is JumpInstruction || instruction is Ret) {
                curr.add(instruction)
                newBasicBlock(curr)
                curr.clear()
                processed = true
            }
            if (!processed) {
                curr.add(instruction)
            }
        }
        if (curr.isNotEmpty()) {
            newBasicBlock(curr)
        }
    }

    private fun newBasicBlock(curr: MutableList<Instruction>) {
        val block = BasicBlock(curr.toMutableList())
        basicBlocks.add(block)
        for (instruction in curr) {
            instructionToBlock[instruction] = block
        }
    }

    fun findJumpTargets() {
        for (instruction in function.instructions) {
            if (instruction is JumpInstruction) {
                val sourceBlock = instructionToBlock[instruction]!!
                val targetBlock = instructionToBlock[instruction.target]!!
                val l = successors[sourceBlock] ?: mutableListOf()
                l.add(targetBlock)
                successors[sourceBlock] = l
            }
        }
    }
}

