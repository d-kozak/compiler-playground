package passes

import IrFunction
import Noop

class RemoveNoopPass : OptimizationPass {

    override fun apply(f: IrFunction) {
        f.instructions = f.instructions.filter { it !is Noop || it.jumpedFrom != null }.toTypedArray()
        setInstructionIndexes(f)
    }
}