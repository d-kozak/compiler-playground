fun computeCfg(function: IrFunction): ControlFlowGraph {
    val cfg = ControlFlowGraph(function)
    cfg.computeBasicBlocks()
    cfg.findJumpTargets()
    cfg.computePostOrder()
    return cfg
}

fun dumpCfg(cfg: ControlFlowGraph): String = buildString {
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

class BasicBlock(
    val id: Int,
    val instructions: MutableList<Instruction> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BasicBlock

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}

data class ControlFlowGraph(
    val function: IrFunction,
    val basicBlocks: MutableList<BasicBlock> = mutableListOf(),
    val instructionToBlock: MutableMap<Instruction, BasicBlock> = mutableMapOf(),
    val successors: MutableMap<BasicBlock, MutableList<BasicBlock>> = mutableMapOf()
) {


    lateinit var postOrder: MutableList<BasicBlock>

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


    private var nextBlockId = 1

    private fun newBasicBlock(curr: MutableList<Instruction>) {
        require(curr.isNotEmpty())
        val block = BasicBlock(nextBlockId++, curr.toMutableList())
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

    fun computePostOrder() {
        if (basicBlocks.isEmpty()) {
            this.postOrder = mutableListOf()
            return
        }
        val postOrder = mutableListOf<BasicBlock>()
        val start = basicBlocks.minBy { it.id }

        val seen = mutableSetOf<BasicBlock>()

        fun dfs(block: BasicBlock) {
            if (!seen.add(block))
                return
            val successors = successors[block]
            if (successors != null) {
                for (next in successors)
                    dfs(next)
            }
            postOrder.add(block)
        }

        dfs(start)

        this.postOrder = postOrder
    }

    fun reversedPostOrder() = postOrder.asReversed()
}

