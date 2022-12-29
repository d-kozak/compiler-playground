package asm


const val FIRST_FREE = 19
const val LAST_FREE = 28

class AssignFirstFreeStrategy {


    private var free = FIRST_FREE

    private val registers = mutableMapOf<VirtualRegister, MachineRegister>()

    fun assign(vreg: VirtualRegister) = registers.computeIfAbsent(vreg) { nextFree() }

    private fun nextFree(): MachineRegister {
        require(free <= LAST_FREE)
        return MachineRegister("x${free++}")
    }

}

fun registerAllocation(from: InstructionOrLabel, to: InstructionOrLabel) {
    val strategy = AssignFirstFreeStrategy()

    var curr = from
    while (true) {
        if (curr is AsmInstruction) {
            for ((i, param) in curr.params.withIndex()) {
                if (param is VirtualRegister)
                    curr.params[i] = strategy.assign(param)
            }
        }
        if (curr === to)
            break
        curr = curr.next!!
    }
}