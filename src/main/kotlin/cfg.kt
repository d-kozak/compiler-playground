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

    for ((from, successors) in cfg.successors.entries) {
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

// todo id
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
            if (instruction.label != null && curr.isNotEmpty()) {
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
        require(curr.isNotEmpty())
        val block = BasicBlock(curr.toMutableList())
        basicBlocks.add(block)
        for (instruction in curr) {
            instructionToBlock[instruction] = block
        }
    }

    fun findJumpTargets() {
        for ((i, instruction) in function.instructions.withIndex()) {
            val sourceBlock = instructionToBlock[instruction]!!
            if (i + 1 < function.instructions.size) {
                val next = instructionToBlock[function.instructions[i + 1]]!!
                if (sourceBlock != next && instruction !is JumpInstruction) {
                    val l = successors[sourceBlock] ?: mutableListOf()
                    l.add(next)
                    successors[sourceBlock] = l
                }
            }
            if (instruction is JumpInstruction) {
                val targetBlock = instructionToBlock[instruction.target]!!
                val l = successors[sourceBlock] ?: mutableListOf()
                l.add(targetBlock)
                successors[sourceBlock] = l

                if (instruction is CondJump) {
                    val nextBlock = instructionToBlock[function.instructions[i + 1]]!!
                    l.add(nextBlock)
                }
            }
        }
    }
}

