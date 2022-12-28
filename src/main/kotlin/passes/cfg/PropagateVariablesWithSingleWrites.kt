package passes.cfg

import BinaryInstruction
import ControlFlowGraph
import Identifier
import IdentifierOrValue
import Move

class PropagateVariablesWithSingleWrites : CfgPass {

    sealed class PropagatedValue
    object Top : PropagatedValue()
    data class Constant(val value: IdentifierOrValue) : PropagatedValue()

    override fun apply(cfg: ControlFlowGraph) {
        val assignments = mutableMapOf<Identifier, PropagatedValue>()

        for (basicBlock in cfg.basicBlocks) {
            for (instruction in basicBlock.instructions) {
                when {
                    instruction is Move -> {
                        if (assignments.contains(instruction.target))
                            assignments[instruction.target] = Top
                        else assignments[instruction.target] = Constant(instruction.source)
                    }
                }
            }
        }


        for (block in cfg.reversedPostOrder()) {
            for (instruction in block.instructions) {
                when {
                    instruction is Move -> {
                        val improved = tryImprove(assignments, instruction.source)
                        if (improved != null) instruction.source = improved
                    }

                    instruction is BinaryInstruction -> {
                        var improved = tryImprove(assignments, instruction.left)
                        if (improved != null) instruction.left = improved
                        improved = tryImprove(assignments, instruction.right)
                        if (improved != null) instruction.right = improved
                    }
                }
            }
        }

    }

    private fun tryImprove(
        assignments: MutableMap<Identifier, PropagatedValue>,
        source: IdentifierOrValue
    ): IdentifierOrValue? {
        if (source is Identifier) {
            val nSource = assignments[source]
            if (nSource != null && nSource is Constant && nSource != source)
                return nSource.value
        }
        return null
    }
}