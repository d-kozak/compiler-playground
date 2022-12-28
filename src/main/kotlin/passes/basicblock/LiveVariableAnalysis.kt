package passes.basicblock

import BasicBlock
import BinaryInstruction
import CondJump
import DirectJump
import FunctionCall
import Identifier
import Move
import Noop
import Not
import Ret
import internalError

// todo this has to be data flow propagating information between basic blocks
class LiveVariableAnalysis : BasicBlockPass {

    override fun apply(block: BasicBlock) {
        markAllDead(block)
        markLiveVariables(block)
        removeDeadInstructions(block)
    }

    private fun markAllDead(block: BasicBlock) {
        for (instruction in block.instructions) {
            instruction.isLive = false
        }
    }

    private fun markLiveVariables(block: BasicBlock) {
        val liveVariables = mutableSetOf<Identifier>()
        for (inst in block.instructions.asReversed()) {
            when {
                inst is BinaryInstruction -> {
                    if (inst.target in liveVariables) {
                        inst.isLive = true
                        val left = inst.left
                        if (left is Identifier)
                            liveVariables.add(left)
                        val right = inst.right
                        if (right is Identifier)
                            liveVariables.add(right)
                    }
                    liveVariables.remove(inst.target)
                }

                inst is Move -> {
                    if (inst.target in liveVariables) {
                        inst.isLive = true
                        val source = inst.source
                        if (source is Identifier)
                            liveVariables.add(source)
                    }
                    liveVariables.remove(inst.target)
                }

                inst is Not -> {
                    if (inst.target in liveVariables) {
                        inst.isLive = true
                        liveVariables.add(inst.source)
                    }
                    liveVariables.remove(inst.target)
                }

                inst is CondJump -> {
                    inst.isLive = true
                    liveVariables.add(inst.condition)
                }

                inst is Ret -> {
                    inst.isLive = true
                    val value = inst.value
                    if (value is Identifier)
                        liveVariables.add(value)
                }

                inst is FunctionCall -> {
                    inst.isLive = true
                    for (arg in inst.args) {
                        if (arg is Identifier)
                            liveVariables.add(arg)
                    }
                }

                inst is DirectJump || inst is Noop -> {
                    // nothing to do
                }


                else -> internalError("Should never be reached $inst")
            }
        }
    }

    private fun removeDeadInstructions(block: BasicBlock) {
        for ((i, inst) in block.instructions.withIndex()) {
            if (!inst.isLive)
                block.instructions[i] = Noop()
        }
    }
}