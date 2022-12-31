package asm


const val FIRST_FREE = 19
const val LAST_FREE = 28

class AssignFirstFreeStrategy {


    private var free = FIRST_FREE

    private val registers = mutableMapOf<VirtualRegister, MachineRegister>()

    fun assign(vreg: VirtualRegister) = registers.computeIfAbsent(vreg) { nextFree() }

    private fun nextFree(): MachineRegister {
        require(free <= LAST_FREE) { "out of registers" }
        return MachineRegister("x${free++}")
    }

}

fun registerAllocation(from: InstructionOrLabel, to: InstructionOrLabel) {
    val strategy = AssignFirstFreeStrategy()

    var curr = from
    while (true) {
        if (curr is ArmInstruction) {
            processParams(curr.params, strategy)
        }
        if (curr === to)
            break
        curr = curr.next!!
    }
}

fun processParams(params: MutableList<InstructionParameter>, strategy: AssignFirstFreeStrategy) {
    for ((i, param) in params.withIndex()) {
        if (param is VirtualRegister)
            params[i] = strategy.assign(param)
        else if (param is ParamList)
            processParams(param.list, strategy)
    }
}
