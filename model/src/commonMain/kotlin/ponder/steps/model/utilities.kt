package ponder.steps.model

fun String.obfuscate(): String {
    return this.map { it.code.xor('s'.code).toChar() }.joinToString("")
}

fun String.deobfuscate(): String {
    return this.map { it.code.xor('s'.code).toChar() }.joinToString("")
}