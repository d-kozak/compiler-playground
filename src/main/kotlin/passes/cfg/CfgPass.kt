package passes.cfg

import ControlFlowGraph

interface CfgPass {

    fun apply(cfg: ControlFlowGraph)
}
