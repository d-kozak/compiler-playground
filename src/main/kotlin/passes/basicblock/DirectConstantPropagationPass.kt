package passes.basicblock

import BasicBlock
import BinaryInstruction
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
            }
        }
    }
}