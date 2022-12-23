package passes

import IrFunction

interface OptimizationPass {

    fun apply(f: IrFunction)
}