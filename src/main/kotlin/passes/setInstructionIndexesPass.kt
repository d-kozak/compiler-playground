package passes

import IrFunction

fun setInstructionIndexes(ir: IrFunction) {
    for ((i, inst) in ir.instructions.withIndex()) {
        inst.index = i
    }
}