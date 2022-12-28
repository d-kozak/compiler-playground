package passes

import ArrayRead
import ArrayWrite
import BinaryInstruction
import CondJump
import FunctionCall
import Identifier
import IdentifierOrValue
import IrFunction
import Move
import Noop
import Ret

class RemoveCompletelyUnusedAssignments : OptimizationPass {

    override fun apply(f: IrFunction) {
        val used = mutableSetOf<Identifier>()

        fun maybeAdd(source: IdentifierOrValue) {
            if (source is Identifier)
                used.add(source)
        }

        for (inst in f.instructions) {
            when {
                inst is Move -> maybeAdd(inst.source)
                inst is BinaryInstruction -> {
                    maybeAdd(inst.left)
                    maybeAdd(inst.right)
                }

                inst is FunctionCall -> {
                    for (arg in inst.args) {
                        maybeAdd(arg)
                    }
                }

                inst is ArrayRead -> {
                    maybeAdd(inst.arrayBase)
                    maybeAdd(inst.arrIndex)
                }

                inst is ArrayWrite -> {
                    maybeAdd(inst.arr)
                    maybeAdd(inst.arrIndex)
                    maybeAdd(inst.value)
                }

                inst is CondJump -> {
                    maybeAdd(inst.condition)
                }

                inst is Ret -> {
                    maybeAdd(inst.value ?: continue)
                }
            }
        }

        for ((i, inst) in f.instructions.withIndex()) {
            when {
                inst is Move -> {
                    if (inst.target !in used) {
                        f.instructions[i] = Noop().also {
                            it.jumpedFrom = inst.jumpedFrom
                            it.label = inst.label
                            for (jumpInstruction in it.jumpedFrom) {
                                jumpInstruction.target = it
                            }
                        }

                    }
                }
            }
        }

    }
}