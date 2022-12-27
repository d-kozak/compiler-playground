import java.lang.reflect.Field
import kotlin.reflect.KClass

annotation class CliParam(
    val short: String,
    val long: String = "",
    val needsValue: Boolean = false,
    val extractor: KClass<out ValueExtractor> = ValueExtractor.Identity::class
)

data class CompilerConfig(
    @field:CliParam("-v")
    var verbose: Boolean = false,
    var inputFile: String? = null
)

interface ValueExtractor {
    fun extract(value: String): Any


    class Identity : ValueExtractor {
        override fun extract(value: String) = value
    }
}


fun selectExtractor(annotation: CliParam): ValueExtractor {
    return annotation.extractor.objectInstance!!
}

fun configMap(): MutableMap<String, Pair<Field, CliParam>> {
    val res = mutableMapOf<String, Pair<Field, CliParam>>()
    for (field in CompilerConfig::class.java.declaredFields) {
        field.trySetAccessible()
        val annotation =
            field.getAnnotation(CliParam::class.java)
        if (annotation == null) {
            require(field.name == "inputFile") { "Only inputFile field can be without annotation, because it is positional" }
            continue
        }

        res[annotation.short] = field to annotation
        if (annotation.long.isNotEmpty())
            res[annotation.long] = field to annotation
    }

    return res
}

fun parseConfig(args: Array<String>): CompilerConfig {
    val config = CompilerConfig()

    val configMap = configMap()

    var i = 0
    val n = args.size

    while (i < n) {
        val arg = args[i]
        val lookup = configMap[arg]
        if (lookup == null) {
            require(config.inputFile == null) { "Input file already specified" }
            config.inputFile = arg
            i++
            continue
        }
        val (field, annotation) = lookup
        if (annotation.needsValue) {
            val extractor = selectExtractor(annotation)
            require(i + 1 < n) { "No value set for $arg" }
            field.set(config, extractor.extract(args[i + 1]))
            i += 2
        } else {
            require(field.type == Boolean::class.java) { "Only boolean fiels do not need any value" }
            // flip the value
            field.setBoolean(config, !field.getBoolean(config))
            i++
        }
    }


    // allow for now for easier debugging
//    require(config.inputFile != null) { "No input file specified" }

    return config
}