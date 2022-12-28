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

data class Reg(val name: String) {
    override fun toString(): String {
        return name
    }
}

// todo this should be function-specific
private class Registers {
    var free = 19
    var LIM = 28

    val assigned = mutableMapOf<Identifier, Reg>()
    fun registerFor(id: Identifier): Reg {
        return assigned.computeIfAbsent(id) { nextRegister() }
    }

    fun nextRegister(): Reg {
        require(free <= LIM) { "Out of registers" }
        return Reg("x${free++}")
    }

}

class Aarch64Assembler(
    val irFunctions: MutableList<IrFunction>,
    val debugDump: DebugDump,
) {

    private val registers = Registers()
    val buffer = StringBuilder()
    fun gen() {
        val main = irFunctions.find { it.name.name == "main" }
        require(main != null) { "Main not found" }
        header()

        for (f in irFunctions)
            genFunction(f)

        buildIns()
        footer()
    }

    private fun buildIns() {
        allocArray()
        assertion()
    }

    private fun assertion() {
        val code = """
    .globl _my_assert
    .p2align 2
    
_my_assert:
    sub sp, sp, #32
    stp x29, x30, [sp, #16]
    add x29, sp, #16
    cbnz w0, _succ
    
    adrp x0, l_.str.1@PAGE
    add x0,x0, l_.str.1@PAGEOFF
    bl _printf
    mov x0, #1
    bl _exit

_succ:
    ldp x29, x30, [sp, #16]
    add sp, sp, #32
    ret
        """
        buffer.append(code)
    }

    private fun allocArray() {
        val code = """
    .globl	_alloc_arr                      ; -- Begin function alloc_arr
	.p2align	2

_alloc_arr:                             ; @alloc_arr
; %bb.0:
	sub	sp, sp, #32                     ; =32
	stp	x29, x30, [sp, #16]             ; 16-byte Folded Spill
	add	x29, sp, #16                    ; =16

	lsl	x0, x0, #2
	bl	_malloc

	ldp	x29, x30, [sp, #16]             ; 16-byte Folded Reload
	add	sp, sp, #32                     ; =32
	ret
"""
        buffer.append(code)
    }

    private fun genFunction(f: IrFunction) {
        functionPrologue(f)
        for (inst in f.instructions) {
            gen(inst)
        }
        newline()
        functionEpilogue(f)
    }

    private fun gen(inst: Instruction) {
        val label = inst.label
        if (label != null) genLabel("_${label.name}")
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
        genInstr("nop")
    }

    private fun genDirectJump(inst: DirectJump) {
        genInstr("b _${inst.target.label!!.name}")
    }

    private fun genArrayRead(inst: ArrayRead) {
        val arr = regOrValue(inst.arrayBase)
        val index = regOrValue(inst.arrIndex)
        // todo fix this mess
        val target = 'w' + registers.registerFor(inst.target).name.substring(1)
        genInstr("ldr $target, [$arr, $index, lsl #2]")
    }

    private fun genArrayWrite(inst: ArrayWrite) {
        val arr = regOrValue(inst.arr)
        val index = regOrValue(inst.arrIndex)
        // todo fix this mess
        val value = 'w' + regOrValue(inst.value).name.substring(1)
        genInstr("str $value, [$arr, $index, lsl #2]")
    }

    private fun genAdd(inst: Add) {
        genInstr("add ${registers.registerFor(inst.target)}, ${regOrValue(inst.left)}, ${regOrValue(inst.right)}")
    }

    private fun genMod(inst: Mod) {
        val target = registers.registerFor(inst.target)
        val left = regOrValue(inst.left)
        val right = if (inst.right is IntConstant) {
            val nxt = registers.nextRegister()
            genInstr("mov $nxt, ${imm((inst.right as IntConstant).value)}")
            nxt
        } else regOrValue(inst.right)
        genInstr("sdiv $target, $left, $right")
        genInstr("mul $target, $target, $right")
        genInstr("subs $target, $left, $target")
    }

    var prevCmp = null as String?

    private fun genCondJump(inst: CondJump) {
        val label = inst.target.label!!.name
        val prev = prevCmp
        if (prev != null) {
            prevCmp = null
            genInstr("b.$prev _$label")
        } else {
            // just check whether the value is non-zero
            genInstr("cbnz ${registers.registerFor(inst.condition)}, _$label")
        }
    }

    private fun genCmp(inst: BinaryInstruction, cc: String) {
        prevCmp = cc
        genInstr("cmp ${regOrValue(inst.left)}, ${regOrValue(inst.right)}")
        genInstr("cset ${regOrValue(inst.target)}, $cc")
    }

    private fun genNot(inst: Not) {
        val source = registers.registerFor(inst.source)
        val target = registers.registerFor(inst.target)
        genInstr("cmp $source, #0")
        genInstr("cset $target, eq")
    }

    private fun regOrValue(source: IdentifierOrValue): Reg = when (source) {
        is Identifier -> registers.registerFor(source)
        is IntConstant -> imm(source.value)
    }


    private fun genMove(inst: Move) {
        val target = registers.registerFor(inst.target)
        val source = regOrValue(inst.source)
        genInstr("mov $target, $source")
    }

    private fun imm(value: Int): Reg {
        return Reg("#$value")
    }

    private fun genCall(inst: FunctionCall) {
        if (inst.functionName.name == "print") {
            genPrint(inst)
            return
        }
        for ((i, arg) in inst.args.withIndex()) {
            genInstr("mov x$i, ${regOrValue(arg)}")
        }
        val name = when (val n = inst.functionName.name) {
            "IntArray" -> "alloc_arr"
            "assert" -> "my_assert"
            else -> n
        }
        genInstr("bl _$name")
        genInstr("mov ${registers.registerFor(inst.target)}, x0")
    }

    private fun genRet(inst: Ret) {
        // todo jump to end
        val value = inst.value
        if (value != null) {
            genInstr("mov x0, ${regOrValue(value)}")
        }
        functionEpilogue(null)
        genInstr("ret")
    }


    private fun genPrint(inst: FunctionCall) {
        if (inst.args.isEmpty()) {
            genInstr("adrp x0, l_.str.2@PAGE")
            genInstr("add x0, x0, l_.str.2@PAGEOFF")
            genInstr("bl _puts")
            return
        }
        // fetch the formatting string into x0
        genInstr("adrp x0, l_.str@PAGE")
        genInstr("add x0, x0, l_.str@PAGEOFF")

        val arg = inst.args[0]
        when (arg) {
            is Identifier -> genInstr("str ${registers.registerFor(arg)}, [sp]")
            is IntConstant -> {
                val nxt = registers.nextRegister()
                genInstr("mov $nxt, ${imm(arg.value)}")
                genInstr("str $nxt, [sp]")
            }
        }
        genInstr("bl _printf")
    }

    private fun functionEpilogue(f: IrFunction?) {
        if (f != null && f.name.name == "main") {
            // todo probably not the best way to achieve this
            genInstr("mov x0, #0")
        }
        genInstr("ldp x29, x30, [sp, #32]")
        genInstr("add sp, sp, #48")
        genInstr("ret")
        newline()
    }

    private fun functionPrologue(f: IrFunction) {
        val name = "_${f.name}"
        genInstr(".globl $name")
        genInstr(".p2align\t2")
        genLabel(name)
        genInstr("sub sp, sp, #48")
        genInstr("stp x29, x30, [sp, #32]")
        newline()
    }

    private fun genLabel(name: String) {
        append("$name:")
    }

    private fun genInstr(txt: String) {
        buffer.appendLine("\t$txt")
    }

    private fun append(str: String) {
        buffer.appendLine(str)
    }

    private fun header() {
        val txt = """  
    .section	__TEXT,__text,regular,pure_instructions
    .build_version macos, 11, 0	sdk_version 11, 3
 """
        buffer.appendLine(txt)
        newline()
    }

    private fun newline() {
        buffer.appendLine()
    }

    private fun footer() {
        val txt = """
    .section	__TEXT,__cstring,cstring_literals
l_.str:                                 ; @.str
    .asciz	"%d\n"
l_.str.1:
	.asciz	"Assertion failed"
l_.str.2:
    .asciz ""
    """
        buffer.appendLine(txt)
    }
}