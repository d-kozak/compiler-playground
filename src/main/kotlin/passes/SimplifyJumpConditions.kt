package passes

import BinaryInstruction
import CondJump
import IrFunction
import Noop
import Not

class SimplifyJumpConditions : OptimizationPass {

    override fun apply(f: IrFunction) {
        for ((i, inst) in f.instructions.withIndex()) {
            if (inst is CondJump && i - 2 >= 0) {
                val prev = f.instructions[i - 1]
                val prevPrev = f.instructions[i - 2]
                if (prev is Not
                    && inst.condition == prev.target
                    && prevPrev is BinaryInstruction
                    && prevPrev.canNegate()
                    && prev.source == prevPrev.target
                ) {
                    f.instructions[i - 1] = Noop()
                    f.instructions[i - 2] = prevPrev.negate()
                    inst.condition = prevPrev.target
                }
            }
        }
    }
}