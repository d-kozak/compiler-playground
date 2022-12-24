package passes.basicblock

import BasicBlock

interface BasicBlockPass {

    fun apply(block: BasicBlock)
}