package me.evo.seraphiel.command

import kotlinx.coroutines.async
import me.evo.seraphiel.Seraphiel

@CommandDsl
abstract class CommandBuilder<Source : CommandSource>(val name: String) {
    val children = mutableListOf<CommandBuilder<Source>>()
    var runner: CommandRunner<Source>? = null

    fun runs(block: CommandRunner<Source>) {
        this.runner = block
    }

    fun runsAsync(block: AsyncCommandRunner<Source>) {
        this.runner = fun CommandContext<Source>.() {
            Seraphiel.async {
                try {
                    block()
                } catch (e: Exception) {
                    Seraphiel.LOGGER.error("Error executing command '$name': ${e.message}", e)
                }
            }
        }
    }

    fun <T : Any> argument(name: String, type: ArgumentType<T>, block: ArgumentBuilder<Source, T>.(T) -> Unit) {
        children.add(ArgumentBuilder<Source, T>(name, type).apply {
            this.dynamicExecutor = block
        })
    }

    fun integer(name: String, block: ArgumentBuilder<Source, Int>.(Int) -> Unit) {
        argument(name, IntegerArgumentType(), block)
    }

    fun double(name: String, block: ArgumentBuilder<Source, Double>.(Double) -> Unit) {
        argument(name, DoubleArgumentType(), block)
    }

    fun string(name: String, block: ArgumentBuilder<Source, String>.(String) -> Unit) {
        argument(name, StringArgumentType(), block)
    }

    fun <T : Any> argument(name: String, type: ArgumentType<T>, block: ArgumentBuilder<Source, T>.() -> Unit = {}) {
        children.add(ArgumentBuilder<Source, T>(name, type).apply(block))
    }

    fun literal(name: String, block: LiteralBuilder<Source>.() -> Unit) {
        children.add(LiteralBuilder<Source>(name).apply(block))
    }

    abstract fun build(): CommandNode<Source>
}