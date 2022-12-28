package passes

import IrFunction
import JumpInstruction

fun setInstructionIndexes(ir: IrFunction) {
    for ((i, inst) in ir.instructions.withIndex()) {
        inst.index = i
    }
    for (inst in ir.instructions) {
        if (inst is JumpInstruction) {
            inst.targetIndex = inst.target.index
        }
    }
}