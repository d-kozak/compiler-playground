package asm

import Add
import ArrayRead
import ArrayWrite
import BinaryInstruction
import CondJump
import DebugDump
import DirectJump
import Eq
import FunctionCall
import Ge
import Gt
import Identifier
import IdentifierOrValue
import Instruction
import IntConstant
import IrFunction
import Le
import Lt
import Mod
import Move
import Neq
import Noop
import Not
import Ret
import internalError

sealed interface InstructionParameter {
    fun asmName(): String
}

data class MachineRegister(val name: String) : InstructionParameter {
    override fun asmName() = name
}

data class Id(val name: String) : InstructionParameter {
    override fun asmName(): String = name
}

fun reg(name: String) = MachineRegister(name)

data class StackSlot(val offset: Int) : InstructionParameter {
    override fun asmName() = "[sp,-4]"
}

fun stk(offset: Int) = StackSlot(offset)

data class IntLiteral(val value: Int) : InstructionParameter {
    override fun asmName(): String = "#$value"

}

fun int(value: Int) = IntLiteral(value)

data class VirtualRegister(val name: String) : InstructionParameter {
    override fun asmName(): String = "v_$name"
}

fun vreg(name: String) = VirtualRegister(name)

data class ParamList(val list: MutableList<InstructionParameter>) : InstructionParameter {
    override fun asmName(): String = list.joinToString(prefix = "[", postfix = "]") { it.asmName() }
}

fun lst(vararg params: InstructionParameter) = ParamList(params.toMutableList())

sealed class InstructionOrLabel {
    abstract fun lower(): String

    var prev: InstructionOrLabel? = null
    var next: InstructionOrLabel? = null
}

sealed class ArmInstruction(val mnemo: String, val params: MutableList<InstructionParameter> = mutableListOf()) :
    InstructionOrLabel() {

    constructor(mnemo: String, vararg params: InstructionParameter) : this(mnemo, params.toMutableList())

    override fun lower(): String = "\t$mnemo ${params.joinToString { it.asmName() }}"


    class Nop : ArmInstruction("nop")

    class Branch(label: Id) : ArmInstruction("b", label) {
        var label: Id
            get() = params[0] as Id
            set(value) {
                params[0] = value
            }
    }

    class CondBranch(cc: String, label: Id) : ArmInstruction("b.$cc", label) {
        var condition: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }

        var label: Id
            get() = params[0] as Id
            set(value) {
                params[0] = value
            }
    }

    class Cbnz(condition: InstructionParameter, label: Id) : ArmInstruction("cbnz", condition, label) {
        var condition: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }

        var label: Id
            get() = params[0] as Id
            set(value) {
                params[0] = value
            }

    }

    class Cmp(left: InstructionParameter, right: InstructionParameter) : ArmInstruction("cmp", left, right) {
        var left: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }

        var right: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }
    }

    class Cset(target: InstructionParameter, cc: Id) : ArmInstruction("cset", target, cc) {
        var target: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }
    }

    class Ldr(target: InstructionParameter, source: InstructionParameter) : ArmInstruction("ldr", target, source) {
        var target: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }

        var source: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }

    }

    class Str(source: InstructionParameter, target: InstructionParameter) : ArmInstruction("str", source, target) {
        var source: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }
        var target: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }


    }

    class Binary(mnemo: String, target: InstructionParameter, left: InstructionParameter, right: InstructionParameter) :
        ArmInstruction(mnemo, target, left, right) {
        var target: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }

        var left: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }

        var right: InstructionParameter
            get() = params[2]
            set(value) {
                params[2] = value
            }


    }

    class Mov(target: InstructionParameter, source: InstructionParameter) : ArmInstruction("mov", target, source) {
        var target: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }

        var source: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }

    }

    class Bl(label: Id) : ArmInstruction("bl", label)

    class Adrp(target: InstructionParameter, label: Id) : ArmInstruction("adrp", target, label) {
        var target: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }
    }

    class Stp(regLeft: InstructionParameter, regRight: InstructionParameter, target: InstructionParameter) :
        ArmInstruction("stp", regLeft, regRight, target) {
        var regLeft: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }
        var regRight: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }

        var target: InstructionParameter
            get() = params[2]
            set(value) {
                params[2] = value
            }
    }

    class Ldp(regLeft: InstructionParameter, regRight: InstructionParameter, source: InstructionParameter) :
        ArmInstruction("ldp", regLeft, regRight, source) {
        var regLeft: InstructionParameter
            get() = params[0]
            set(value) {
                params[0] = value
            }
        var regRight: InstructionParameter
            get() = params[1]
            set(value) {
                params[1] = value
            }

        var source: InstructionParameter
            get() = params[2]
            set(value) {
                params[2] = value
            }
    }

    class Ret : ArmInstruction("ret")
}


data class Label(val name: String) : InstructionOrLabel() {
    override fun lower(): String = "$name:"
}

data class LinkerDirective(val content: String) : InstructionOrLabel() {
    override fun lower(): String = "\t$content"
}

class Empty : InstructionOrLabel() {
    override fun lower(): String = ""
}

data class BuiltInMacroInstruction(val content: String) : InstructionOrLabel() {
    override fun lower(): String = content
}

private class VRegGen {
    private var cnt = 0

    fun next() = vreg("g${cnt++}")
}

class Aarch64Assembler(
    val irFunctions: MutableList<IrFunction>,
    val debugDump: DebugDump,
) {

    private val vregs = VRegGen()


    lateinit var head: InstructionOrLabel
    lateinit var last: InstructionOrLabel

    private val fixTarget = mutableListOf<ArmInstruction>()

    private val builtIns = mutableListOf(
        Aarch64BuiltIn("IntArray", "alloc_arr")
    )


    fun gen() {
        val main = irFunctions.find { it.name.name == "main" }
        require(main != null) { "Main not found" }
        header()

        for (f in irFunctions) {
            genFunction(f)
        }

        fixTargets()

        includeBuiltIns()
        footer()
    }

    private fun fixTargets() {
        for (inst in fixTarget) {
            val prev = inst.params[0].asmName().substring(1)
            inst.params[0] = MachineRegister("w$prev")
        }
    }

    private fun includeBuiltIns() {
        for (builtIn in builtIns) {
            inst(BuiltInMacroInstruction(builtIn.loadContent()))
        }
    }

    private fun genFunction(f: IrFunction) {
        val from = last
        functionPrologue(f)
        // get ptr to last prologue inst
        for (inst in f.instructions) {
            gen(inst)
        }
        newline()
        // append movs to stack at the beginning
        // append movs from stack to the end
        functionEpilogue(f)
        val to = last

        registerAllocation(from, to)
    }

    private fun asList(from: InstructionOrLabel, to: InstructionOrLabel): List<InstructionOrLabel> {
        val res = mutableListOf<InstructionOrLabel>()

        var curr = from
        while (true) {
            res.add(curr)

            if (curr === to) break
            curr = curr.next!!
        }

        return res
    }

    private fun gen(inst: Instruction) {
        val label = inst.label
        if (label != null) inst(Label("_${label.name}"))
        when (inst) {
            is Add -> genAdd(inst)
            is Mod -> genMod(inst)
            is ArrayWrite -> genArrayWrite(inst)
            is ArrayRead -> genArrayRead(inst)
            is Move -> genMove(inst)
            is FunctionCall -> genCall(inst)
            is Eq -> genCmp(inst, "eq")
            is Neq -> genCmp(inst, "ne")
            is Lt -> genCmp(inst, "lt")
            is Le -> genCmp(inst, "le")
            is Gt -> genCmp(inst, "gt")
            is Ge -> genCmp(inst, "ge")
            is Not -> genNot(inst)
            is CondJump -> genCondJump(inst)
            is DirectJump -> genDirectJump(inst)
            is Noop -> genNoop(inst)
            is Ret -> genRet(inst)
            else -> internalError("Unsupported instruction$inst")
        }
    }

    private fun genNoop(inst: Noop) {
        inst(ArmInstruction.Nop())
    }

    private fun genDirectJump(inst: DirectJump) {
        inst(ArmInstruction.Branch(Id("_${inst.target.label!!.name}")))
    }

    private fun genArrayRead(inst: ArrayRead) {
        fixTarget.add(
            inst(
                ArmInstruction.Ldr(
                    vreg(inst.target.name),
                    lst(regOrValue(inst.arrayBase), regOrValue(inst.arrIndex), Id("lsl #2"))
                )
            ) as ArmInstruction
        )
    }

    private fun genArrayWrite(inst: ArrayWrite) {
        fixTarget.add(
            inst(
                ArmInstruction.Str(
                    regOrValue(inst.value),
                    lst(regOrValue(inst.arr), regOrValue(inst.arrIndex), Id("lsl #2"))
                )
            ) as ArmInstruction
        )
    }

    private fun genAdd(inst: Add) {
        inst(ArmInstruction.Binary("add", vreg(inst.target.name), regOrValue(inst.left), regOrValue(inst.right)))
    }

    private fun genMod(inst: Mod) {
        val target = vreg(inst.target.name)
        val left = regOrValue(inst.left)
        val right = if (inst.right is IntConstant) {
            val nxt = vregs.next()
            inst(ArmInstruction.Mov(nxt, int((inst.right as IntConstant).value)))
            nxt
        } else regOrValue(inst.right)
        inst(ArmInstruction.Binary("sdiv", target, left, right))
        inst(ArmInstruction.Binary("mul", target, target, right))
        inst(ArmInstruction.Binary("subs", target, left, target))
    }

    var prevCmp = null as String?

    private fun genCondJump(inst: CondJump) {
        val label = inst.target.label!!.name
        val prev = prevCmp
        if (prev != null) {
            prevCmp = null
            inst(ArmInstruction.CondBranch(prev, Id("_$label")))
        } else {
            // just check whether the value is non-zero
            inst(ArmInstruction.Cbnz(vreg(inst.condition.name), Id("_$label")))
        }
    }

    private fun genCmp(inst: BinaryInstruction, cc: String) {
        prevCmp = cc
        inst(ArmInstruction.Cmp(regOrValue(inst.left), regOrValue(inst.right)))
        inst(ArmInstruction.Cset(vreg(inst.target.name), Id(cc)))
    }

    private fun genNot(inst: Not) {
        inst(ArmInstruction.Cmp(vreg(inst.source.name), int(0)))
        inst(ArmInstruction.Cset(vreg(inst.target.name), Id("eq")))
    }

    private fun genMove(inst: Move) {
        inst(ArmInstruction.Mov(vreg(inst.target.name), regOrValue(inst.source)))
    }


    private fun imm(value: Int): MachineRegister {
        return MachineRegister("#$value")
    }

    private fun genCall(inst: FunctionCall) {
        if (inst.functionName.name == "print") {
            genPrint(inst)
            return
        }
        for ((i, arg) in inst.args.withIndex()) {
            inst(ArmInstruction.Mov(reg("x$i"), regOrValue(arg)))
        }
        var name = inst.functionName.name
        for (builtIn in builtIns) {
            if (builtIn.name == name) {
                name = builtIn.asmName
                builtIn.isUsed = true
                break
            }
        }

        inst(ArmInstruction.Bl(Id("_$name")))
        inst(ArmInstruction.Mov(vreg(inst.target.name), reg("x0")))
    }

    private fun genRet(inst: Ret) {
        // todo jump to end
        val value = inst.value
        if (value != null) {
            inst(ArmInstruction.Mov(reg("x0"), regOrValue(value)))
        }
        functionEpilogue(null)
    }

    private fun genPrint(inst: FunctionCall) {
        if (inst.args.isEmpty()) {
            inst(ArmInstruction.Adrp(reg("x0"), Id("l_.str.2@PAGE")))
            inst(ArmInstruction.Binary("add", reg("x0"), reg("x0"), Id("l_.str.2@PAGEOFF")))
            inst(ArmInstruction.Bl(Id("_puts")))
            return
        }
        // fetch the formatting string into x0
        inst(ArmInstruction.Adrp(reg("x0"), Id("l_.str@PAGE")))
        inst(ArmInstruction.Binary("add", reg("x0"), reg("x0"), Id("l_.str@PAGEOFF")))

        when (val arg = inst.args[0]) {
            is Identifier ->
                inst(ArmInstruction.Str(vreg(arg.name), lst(reg("sp"))))

            is IntConstant -> {
                val nxt = vregs.next()
                inst(ArmInstruction.Mov(nxt, int(arg.value)))
                inst(ArmInstruction.Str(nxt, lst(reg("sp"))))
            }
        }
        inst(ArmInstruction.Bl(Id("_printf")))
    }


    private fun functionPrologue(f: IrFunction) {
        val name = "_${f.name}"
        inst(LinkerDirective(".globl $name"))
        inst(LinkerDirective(".p2align 2"))
        inst(Label(name))
        inst(ArmInstruction.Binary("sub", reg("sp"), reg("sp"), int(48)))
        inst(ArmInstruction.Stp(reg("x29"), reg("x30"), lst(reg("sp"), int(32))))
        newline()
    }

    private fun regOrValue(source: IdentifierOrValue): InstructionParameter = when (source) {
        is Identifier -> vreg(source.name)
        is IntConstant -> int(source.value)
    }

    private fun functionEpilogue(f: IrFunction?) {
        if (f != null && f.name.name == "main") {
            // todo probably not the best way to achieve this
            inst(ArmInstruction.Mov(reg("x0"), int(0)))
        }
        inst(ArmInstruction.Ldp(reg("x29"), reg("x30"), lst(reg("sp"), int(32))))
        inst(ArmInstruction.Binary("add", reg("sp"), reg("sp"), int(48)))
        inst(ArmInstruction.Ret())
        newline()
    }

    private fun inst(i: InstructionOrLabel): InstructionOrLabel {
        last.next = i
        i.prev = last
        last = i
        return i
    }

    private fun header() {
        // insert the first instr manually
        val fst = LinkerDirective(".section	__TEXT,__text,regular,pure_instructions")
        head = fst
        last = fst
        inst(LinkerDirective(".build_version macos, 11, 0\tsdk_version 11, 3"))
        newline()
    }

    private fun newline() {
        inst(Empty())
    }

    private fun footer() {
        inst(LinkerDirective(".section	__TEXT,__cstring,cstring_literals"))
        inst(Label("l_.str"))
        inst(LinkerDirective(""".asciz "%d\n" """))
        inst(Label("l_.str.1"))
        inst(LinkerDirective(""".asciz "Assertion failed" """))
        inst(Label("l_.str.2"))
        inst(LinkerDirective(""".asciz "" """))
    }

    fun dumpAsm() = buildString {
        var curr = head as InstructionOrLabel?
        while (curr != null) {
            appendLine(curr.lower())
            curr = curr.next
        }
    }
}