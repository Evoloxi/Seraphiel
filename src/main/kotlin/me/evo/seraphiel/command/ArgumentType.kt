package me.evo.seraphiel.command

interface ArgumentType<T : Any> {
    fun parse(input: String): T
}

class IntegerArgumentType(private val min: Int = Int.MIN_VALUE, private val max: Int = Int.MAX_VALUE) :
    ArgumentType<Int> {
    override fun parse(input: String): Int {
        val rawInt = input.toIntOrNull() ?: throw IllegalArgumentException("Invalid integer: '$input'")
        return rawInt.takeIf { it in min..max } ?: throw IllegalArgumentException("Integer '$rawInt' out of range [$min, $max]")
    }
}

class DoubleArgumentType(private val min: Double = Double.MIN_VALUE, private val max: Double = Double.MAX_VALUE) :
    ArgumentType<Double> {
    override fun parse(input: String): Double {
        val rawDouble = input.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid double: '$input'")
        return rawDouble.takeIf { it in min..max } ?: throw IllegalArgumentException("Double '$rawDouble' out of range [$min, $max]")
    }
}
class StringArgumentType : ArgumentType<String> {
    override fun parse(input: String): String = input
}