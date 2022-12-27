fun main(args: Array<String>) {
    val config = parseConfig(args)
    GlobalState.config = config
    val compiler = Compiler(config)
    compiler.runAll()
}

