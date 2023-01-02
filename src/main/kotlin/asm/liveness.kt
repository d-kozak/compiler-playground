package asm

import internalError

class Liveness(val program: List<InstructionOrLabel>) {

    val inputs = mutableMapOf<InstructionOrLabel, MutableSet<InstructionOrLabel>>()
    val outputs = mutableMapOf<InstructionOrLabel, MutableSet<InstructionOrLabel>>()

    val gen = mutableMapOf<InstructionOrLabel, MutableSet<VirtualRegister>>()
    val kill = mutableMapOf<InstructionOrLabel, MutableSet<VirtualRegister>>()


    val liveIn = mutableMapOf<InstructionOrLabel, MutableSet<VirtualRegister>>()
    val liveOut = mutableMapOf<InstructionOrLabel, MutableSet<VirtualRegister>>()

    val labels = mutableMapOf<String, Label>()

    val sinks = mutableSetOf<InstructionOrLabel>()

    init {
        for (curr in program) {
            if (curr is Label) {
                require(curr.name !in labels) { "Label ${curr.name} already assigned" }
                labels[curr.name] = curr
            }
        }

        var prev = null as InstructionOrLabel?
        for (curr in program) {
            when (curr) {
                is ArmInstruction -> {
                    when (curr) {
                        is ArmInstruction.Adrp -> target(curr, curr.target)
                        is ArmInstruction.Binary -> {
                            target(curr, curr.target)
                            source(curr, curr.left, curr.right)
                        }

                        is ArmInstruction.Bl -> {
                            // nothing to do
                        }

                        is ArmInstruction.Branch -> {
                            branchTarget(curr, curr.label)
                        }

                        is ArmInstruction.Cbnz -> {
                            source(curr, curr.condition)
                            branchTarget(curr, curr.label)
                        }

                        is ArmInstruction.Cmp -> {
                            source(curr, curr.left)
                            source(curr, curr.right)
                        }

                        is ArmInstruction.CondBranch -> {
                            source(curr, curr.condition)
                            branchTarget(curr, curr.label)
                        }

                        is ArmInstruction.Cset -> {
                            target(curr, curr.target)
                        }

                        is ArmInstruction.Ldp -> {
                            source(curr, curr.source)
                            target(curr, curr.regLeft, curr.regRight)
                        }

                        is ArmInstruction.Stp -> {
                            source(curr, curr.regLeft, curr.regRight)
                            target(curr, curr.target)
                        }

                        is ArmInstruction.Ldr -> {
                            source(curr, curr.source)
                            target(curr, curr.target)
                        }

                        is ArmInstruction.Str -> {
                            source(curr, curr.source)
                            target(curr, curr.target)
                        }

                        is ArmInstruction.Mov -> {
                            source(curr, curr.source)
                            target(curr, curr.target)
                        }

                        is ArmInstruction.Nop -> {
                            // nothing to do
                        }

                        is ArmInstruction.Ret -> {
                            sinks.add(curr)
                        }

                    }
                }

                is LinkerDirective, is Label -> {
                    // skip
                }

                is BuiltInMacroInstruction, is Empty -> internalError("These should never occur within a function")
            }
            if (prev != null && prev !is ArmInstruction.Branch && prev !is ArmInstruction.Ret) {
                addInput(curr, prev)
                addOutput(prev, curr)
            }
            prev = curr

            if (curr.next == null) {
                sinks.add(curr)
            }
        }
    }

    private fun branchTarget(curr: ArmInstruction, label: Id) {
        val target = labels[label.name]!!
        addInput(target, curr)
        addOutput(curr, target)
    }

    private fun addInput(node: InstructionOrLabel, input: InstructionOrLabel) {
        inputs.computeIfAbsent(node) { mutableSetOf() }.add(input)
    }

    private fun addOutput(node: InstructionOrLabel, output: InstructionOrLabel) {
        outputs.computeIfAbsent(node) { mutableSetOf() }.add(output)
    }

    private fun source(instr: ArmInstruction, vararg args: InstructionParameter) {
        val set = mutableSetOf<VirtualRegister>()
        for (arg in args) {
            if (arg is VirtualRegister)
                set.add(arg)
        }
        gen[instr] = set
    }

    private fun target(instr: ArmInstruction, vararg args: InstructionParameter) {
        val set = mutableSetOf<VirtualRegister>()
        for (arg in args) {
            if (arg is VirtualRegister)
                set.add(arg)
        }
        kill[instr] = set
    }


    fun run() {
        val queue = ArrayDeque(sinks)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()

            val newLiveOut = mutableSetOf<VirtualRegister>()

            val outputs = outputs[curr]
            if (outputs != null) {
                for (out in outputs) {
                    val elems = liveIn[out]
                    if (elems != null) {
                        newLiveOut.addAll(elems)
                    }
                }
            }

            val prevLiveOut = liveOut[curr]
            if (prevLiveOut == newLiveOut) {
                // nothing has changed
                continue
            }
            liveOut[curr] = newLiveOut

            val newLiveIn = newLiveOut.toMutableSet()
            newLiveIn.removeAll(kill[curr]!!)
            newLiveIn.addAll(gen[curr]!!)

            val oldLiveIn = liveIn[curr]
            if (oldLiveIn == newLiveIn) {
                // nothing to do
                continue
            }

            liveIn[curr] = newLiveIn

            val inputs = inputs[curr]
            if (inputs != null) {
                queue.addAll(inputs)
            }
        }
    }

}