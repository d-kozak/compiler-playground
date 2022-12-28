package passes

import Add
import BinaryInstruction
import Div
import Eq
import Ge
import Gt
import IntConstant
import IrFunction
import Le
import Lt
import Mod
import Move
import Mult
import Neq
import Sub
import asInt

class EvaluateConstantExpressions : OptimizationPass {

    override fun apply(f: IrFunction) {
        for ((i, inst) in f.instructions.withIndex()) {
            if (inst is BinaryInstruction) {
                val left = inst.left
                val right = inst.right
                if (left is IntConstant && right is IntConstant) {
                    val res = evaluate(inst, left.value, right.value)
                    val newInst = Move(inst.target, IntConstant(res))
                    newInst.label = inst.label
                    newInst.jumpedFrom = inst.jumpedFrom
                    for (jumpInstruction in newInst.jumpedFrom) {
                        jumpInstruction.target = newInst
                    }
                    f.instructions[i] = newInst
                }
            }
        }
    }

    private fun evaluate(inst: BinaryInstruction, left: Int, right: Int): Int = when (inst) {
        is Add -> left + right
        is Sub -> left - right
        is Mult -> left * right
        is Div -> left / right
        is Mod -> left % right
        is Eq -> asInt(left == right).value
        is Neq -> asInt(left != right).value
        is Ge -> asInt(left >= right).value
        is Gt -> asInt(left > right).value
        is Le -> asInt(left <= right).value
        is Lt -> asInt(left < right).value
    }

}