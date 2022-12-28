package asm

import Add
import ArrayRead
import ArrayWrite
import CondJump
import DebugDump
import DirectJump
import FunctionCall
import Ge
import Identifier
import IdentifierOrValue
import Instruction
import IntConstant
import IrFunction
import Move
import Noop
import internalError

data class Reg(val name: String) {
    override fun toString(): String {
        return name
    }
}

private class Registers {
    var free = 19
    var LIM = 28

    val assigned = mutableMapOf<Identifier, Reg>()
    fun registerFor(id: Identifier): Reg {
        return assigned.computeIfAbsent(id, this::nextRegister)
    }

    private fun nextRegister(identifier: Identifier): Reg {
//        require(free <= LIM) { "Out of registers" }
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
        functionEpilogue()
    }

    private fun gen(inst: Instruction) {
        val label = inst.label
        if (label != null) genLabel("_${label.name}")
        when (inst) {
            is Add -> genAdd(inst)
            is ArrayWrite -> genArrayWrite(inst)
            is ArrayRead -> genArrayRead(inst)
            is Move -> genMove(inst)
            is FunctionCall -> genCall(inst)
            is Ge -> genCmp(inst)
            is CondJump -> genCondJump(inst)
            is DirectJump -> genDirectJump(inst)
            is Noop -> genNoop(inst)
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


    var prevCmp = null as Instruction?
    private fun genCondJump(inst: CondJump) {
        val prev = prevCmp ?: internalError("No test before condjump found")
        val mnemo = when (prev) {
            is Ge -> "b.ge"
            else -> TODO("Unsupported $prev")
        }
        genInstr("$mnemo _${inst.target.label!!.name}")
    }

    private fun genCmp(inst: Ge) {
        // todo assign value to target
        prevCmp = inst
        genInstr("cmp ${regOrValue(inst.left)}, ${regOrValue(inst.right)}")
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
            genInstr("mov x$i,${regOrValue(arg)}")
        }
        val name = if (inst.functionName.name == "IntArray") "alloc_arr" else inst.functionName.name
        genInstr("bl _$name")
        genInstr("mov ${registers.registerFor(inst.target)}, x0")
    }


    private fun genPrint(inst: FunctionCall) {
        // fetch the formatting string into x0
        genInstr("adrp x0, l_.str@PAGE")
        genInstr("add x0, x0, l_.str@PAGEOFF")

        val num = regOrValue(inst.args[0])
        genInstr("str $num, [sp]")
        genInstr("bl _printf")
    }

    private fun functionEpilogue() {
        genInstr("ldp x29, x30, [sp, #32]")
        genInstr("add sp, sp, #48")
        genInstr("ret")
    }

    private fun functionPrologue(f: IrFunction) {
        val name = "_${f.name}"
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
    .globl	_main                           ; -- Begin function main
    .p2align	2"""
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
    .asciz	"%d\n""""
        buffer.appendLine(txt)
    }
}