package passes.basicblock

import ArrayRead
import ArrayWrite
import BasicBlock
import BinaryInstruction
import FunctionCall
import Identifier
import IdentifierOrValue
import Move
import Ret

class DirectConstantPropagationPass : BasicBlockPass {
    override fun apply(block: BasicBlock) {
        val values = mutableMapOf<Identifier, IdentifierOrValue>()
        for (instruction in block.instructions) {
            when {
                instruction is Move -> {
                    val nSource = values[instruction.source]
                    if (nSource != null)
                        instruction.source = nSource
                    values[instruction.target] = nSource ?: instruction.source
                }

                instruction is FunctionCall -> {
                    for ((i, arg) in instruction.args.withIndex()) {
                        val narg = values[arg]
                        if (narg != null)
                            instruction.args[i] = narg
                    }
                }

                instruction is BinaryInstruction -> {
                    val nLeft = values[instruction.left]
                    if (nLeft != null)
                        instruction.left = nLeft
                    val nRight = values[instruction.right]
                    if (nRight != null) {
                        instruction.right = nRight
                    }
                }

                instruction is Ret -> {
                    val x = instruction.value ?: continue
                    val nx = values[x]
                    if (nx != null)
                        instruction.value = nx
                }

                instruction is ArrayRead -> {
                    val nArr = values[instruction.arrayBase]
                    if (nArr != null) instruction.arrayBase = nArr as Identifier
                    val nIndex = values[instruction.arrIndex]
                    if (nIndex != null) instruction.arrIndex = nIndex
                }

                instruction is ArrayWrite -> {
                    val nArr = values[instruction.arr]
                    if (nArr != null) instruction.arr = nArr as Identifier
                    val nIndex = values[instruction.arrIndex]
                    if (nIndex != null) instruction.arrIndex = nIndex
                    val nValue = values[instruction.value]
                    if (nValue != null) instruction.value = nValue
                }
            }
        }
    }
}