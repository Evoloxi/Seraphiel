package me.evo.seraphiel.command

import me.evo.seraphiel.Seraphiel
import kotlin.reflect.KClass

open class CommandSource

class PlayerCommandSource : CommandSource() {
    var player = Seraphiel.mc.thePlayer
}

class CommandContext<S : CommandSource>(
    val source: S,
    private val parsedArguments: Map<String, Any>
) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getArgument(name: String, type: KClass<T>): T {
        val value = parsedArguments[name]
            ?: throw IllegalArgumentException("Argument '$name' not found in context for command execution.")
        if (!type.isInstance(value)) {
            throw ClassCastException("Argument '$name' is of type ${value::class.simpleName}, but ${type.simpleName} was expected.")
        }
        return value as T
    }
}

inline fun <reified T : Any> CommandContext<*>.getArgument(name: String): T = getArgument(name, T::class)

typealias CommandRunner<S> = CommandContext<S>.() -> Unit
typealias AsyncCommandRunner<S> = suspend CommandContext<S>.() -> Unit

@DslMarker
annotation class CommandDsl

class Command<S : CommandSource>(val name: String, val rootNode: CommandNode<S>)

@JvmName($$"command$generic")
fun <S : CommandSource> command(name: String, register: Boolean = true, block: LiteralBuilder<S>.() -> Unit): Command<S> {
    val builder = LiteralBuilder<S>(name).apply(block)
    return Command(name, builder.build())
}

fun command(name: String, register: Boolean = true, block: LiteralBuilder<PlayerCommandSource>.() -> Unit): Command<PlayerCommandSource> {
    return command<PlayerCommandSource>(name, register, block.also {
        if (register) CommandManager::registerCommand
    })
}

class DebugSource : CommandSource() {
    var greeting = "Hello"
}

fun main() {
    val myCommand = command<DebugSource>("debugcmd") {
        runs {
            println("${source.greeting} base cmd test")
        }
        literal("say") {
            argument("message", StringArgumentType()) { messageArg ->
                runs {
                    println("echoA: \"$messageArg\"")
                }
            }
        }
        literal("repeat") {
            argument("times", IntegerArgumentType(1, 100)) { integerFromArg ->
                argument("word", StringArgumentType()) { stringFromArg ->
                    runs {
                        println(stringFromArg.repeat(integerFromArg))
                    }
                }
            }
        }
    }
    CommandManager.registerCommand(myCommand)

    val source = DebugSource()
    CommandManager.execute("debugcmd", source)
    CommandManager.execute("debugcmd say hello world", source)
    CommandManager.execute("debugcmd repeat 5 hey", source)
    CommandManager.execute("debugcmd repeat 9 wow", source)
/*
    CommandManager.execute("debugcmd repeat", source)
*/
    CommandManager.tryAutoComplete("mycomman", source).forEach {
        println("Auto-complete suggestion: $it")
    }
}