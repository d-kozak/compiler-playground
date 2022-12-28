package asm

import DebugDump
import FunctionCall
import Identifier
import Instruction
import IntConstant
import IrFunction
import Move
import internalError

data class Reg(val name: String) {
    override fun toString(): String {
        return name
    }
}

private class Registers {
    var free = 9
    var LIM = 15

    val assigned = mutableMapOf<Identifier, Reg>()
    fun registerFor(id: Identifier): Reg {
        return assigned.computeIfAbsent(id, this::nextRegister)
    }

    private fun nextRegister(identifier: Identifier): Reg {
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

        footer()
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
        when (inst) {
            is Move -> genMove(inst)
            is FunctionCall -> genCall(inst)
            else -> internalError("Unsupported instruction$inst")
        }
    }

    private fun genMove(inst: Move) {
        val target = registers.registerFor(inst.target)
        val source = if (inst.source is Identifier)
            registers.registerFor((inst.source as Identifier))
        else imm((inst.source as IntConstant).value)
        genInstr("mov $target, $source")
    }

    private fun imm(value: Int): Reg {
        return Reg("#$value")
    }

    private fun genCall(inst: FunctionCall) {
        require(inst.functionName.name == "print") { "Only supporting print for now" }
        genPrint(inst)
    }

    private fun genPrint(inst: FunctionCall) {
        // fetch the formatting string into x0
        genInstr("adrp x0, l_.str@PAGE")
        genInstr("add x0, x0, l_.str@PAGEOFF")

        val num = registers.registerFor(inst.args[0])
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