package asm

import java.io.FileReader

class Aarch64BuiltIn(val name: String, val asmName: String) {
    fun loadContent(): String {
        val url = this.javaClass.getResource("/$asmName.s")
        return FileReader(url.file).use { it.readText() }
    }

    var isUsed: Boolean = false
}