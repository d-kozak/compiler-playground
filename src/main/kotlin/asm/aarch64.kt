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

data class AsmInstruction(val mnemo: String, val params: MutableList<InstructionParameter>) : InstructionOrLabel() {

    override fun lower(): String = "\t$mnemo ${params.joinToString { it.asmName() }}"

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

    private val fixTarget = mutableListOf<AsmInstruction>()


    fun gen() {
        val main = irFunctions.find { it.name.name == "main" }
        require(main != null) { "Main not found" }
        header()

        for (f in irFunctions) {
            genFunction(f)
        }

        fixTargets()

        // todo builtins
        footer()
    }

    private fun fixTargets() {
        for (inst in fixTarget) {
            val prev = inst.params[0].asmName().substring(1)
            inst.params[0] = MachineRegister("w$prev")
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
            is Ge -> genCmp(inst, "ge")
            is Eq -> genCmp(inst, "eq")
            is Neq -> genCmp(inst, "ne")
            is Gt -> genCmp(inst, "gt")
            is Not -> genNot(inst)
            is CondJump -> genCondJump(inst)
            is DirectJump -> genDirectJump(inst)
            is Noop -> genNoop(inst)
            is Ret -> genRet(inst)
            else -> internalError("Unsupported instruction$inst")
        }
    }

    private fun genNoop(inst: Noop) {
        inst("nop")
    }

    private fun genDirectJump(inst: DirectJump) {
        inst("b", Id("_${inst.target.label!!.name}"))
    }

    private fun genArrayRead(inst: ArrayRead) {
        fixTarget.add(
            inst(
                "ldr",
                vreg(inst.target.name),
                lst(regOrValue(inst.arrayBase), regOrValue(inst.arrIndex), Id("lsl #2"))
            ) as AsmInstruction
        )
    }

    private fun genArrayWrite(inst: ArrayWrite) {
        fixTarget.add(
            inst(
                "str",
                regOrValue(inst.value),
                lst(regOrValue(inst.arr), regOrValue(inst.arrIndex), Id("lsl #2"))
            ) as AsmInstruction
        )
    }

    private fun genAdd(inst: Add) {
        inst("add", vreg(inst.target.name), regOrValue(inst.left), regOrValue(inst.right))
    }

    private fun genMod(inst: Mod) {
        val target = vreg(inst.target.name)
        val left = regOrValue(inst.left)
        val right = if (inst.right is IntConstant) {
            val nxt = vregs.next()
            inst("mov", nxt, int((inst.right as IntConstant).value))
            nxt
        } else regOrValue(inst.right)
        inst("sdiv", target, left, right)
        inst("mul", target, target, right)
        inst("subs", target, left, target)
    }

    var prevCmp = null as String?

    private fun genCondJump(inst: CondJump) {
        val label = inst.target.label!!.name
        val prev = prevCmp
        if (prev != null) {
            prevCmp = null
            inst("b.$prev", Id("_$label"))
        } else {
            // just check whether the value is non-zero
            inst("cbnz", vreg(inst.condition.name), Id("_$label"))
        }
    }

    private fun genCmp(inst: BinaryInstruction, cc: String) {
        prevCmp = cc
        inst("cmp", regOrValue(inst.left), regOrValue(inst.right))
        inst("cset", vreg(inst.target.name), Id(cc))
    }

    private fun genNot(inst: Not) {
        inst("cmp", vreg(inst.source.name), int(0))
        inst("cset", vreg(inst.target.name), Id("eq"))
    }

    private fun genMove(inst: Move) {
        inst("mov", vreg(inst.target.name), regOrValue(inst.source))
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
            inst("mov", reg("x$i"), regOrValue(arg))
        }
        val name = when (val n = inst.functionName.name) {
            "IntArray" -> "alloc_arr"
            "assert" -> "my_assert"
            else -> n
        }
        inst("bl", Id("_$name"))
        inst("mov", vreg(inst.target.name), reg("x0"))
    }

    private fun genRet(inst: Ret) {
        // todo jump to end
        val value = inst.value
        if (value != null) {
            inst("mov", reg("x0"), regOrValue(value))
        }
        functionEpilogue(null)
    }

    private fun genPrint(inst: FunctionCall) {
        if (inst.args.isEmpty()) {
            inst("adrp", reg("x0"), Id("l_.str.2@PAGE"))
            inst("add", reg("x0"), reg("x0"), Id("l_.str.2@PAGEOFF"))
            inst("bl", Id("_puts"))
            return
        }
        // fetch the formatting string into x0
        inst("adrp", reg("x0"), Id("l_.str@PAGE"))
        inst("add", reg("x0"), reg("x0"), Id("l_.str@PAGEOFF"))

        when (val arg = inst.args[0]) {
            is Identifier -> inst("str", vreg(arg.name), lst(reg("sp")))

            is IntConstant -> {
                val nxt = vregs.next()
                inst("mov", nxt, int(arg.value))
                inst("str", nxt, lst(reg("sp")))
            }
        }
        inst("bl", Id("_printf"))
    }


    private fun functionPrologue(f: IrFunction) {
        val name = "_${f.name}"
        inst(LinkerDirective(".globl $name"))
        inst(LinkerDirective(".p2align 2"))
        inst(Label(name))
        inst("sub", reg("sp"), reg("sp"), int(48))
        inst("stp", reg("x29"), reg("x30"), lst(reg("sp"), int(32)))
        newline()
    }

    private fun regOrValue(source: IdentifierOrValue): InstructionParameter = when (source) {
        is Identifier -> vreg(source.name)
        is IntConstant -> int(source.value)
    }

    private fun functionEpilogue(f: IrFunction?) {
        if (f != null && f.name.name == "main") {
            // todo probably not the best way to achieve this
            inst("mov", reg("x0"), int(0))
        }
        inst("ldp", reg("x29"), reg("x30"), lst(reg("sp"), int(32)))
        inst("add", reg("sp"), reg("sp"), int(48))
        inst("ret")
        newline()
    }

    private fun inst(mnemo: String, vararg params: InstructionParameter) =
        inst(AsmInstruction(mnemo, params.toMutableList()))

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