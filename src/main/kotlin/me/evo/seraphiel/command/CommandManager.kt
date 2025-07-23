package me.evo.seraphiel.command

import io.ktor.utils.io.charsets.MalformedInputException
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.UnknownCommandException
import me.evo.seraphiel.Utils

object CommandManager {
    private val commands = mutableMapOf<String, Command<*>>()

    fun getCommand(name: String): Command<*>? {
        return commands[name]
    }

    fun registerCommand(command: Command<*>) {
        if (commands.containsKey(command.name)) {
            Seraphiel.LOGGER.warn("Warning: Overwriting command '${command.name}'")
        }
        commands[command.name] = command
    }

    val defaultHander = { e: Exception, source: CommandSource -> Utils.error(e.message ?: e::class.java.name) }

    @JvmStatic
    fun <Source : CommandSource> executeSafe(
        input: String,
        source: Source,
        handler: (Exception, CommandSource) -> Unit = defaultHander
    ) {
        try {
            execute(input, source)
        } catch (e: Exception) {
            handler(e, source)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <Source : CommandSource> execute(input: String, source: Source) {
        val parts = input.trim().split(" ").filter { it.isNotEmpty() }
        if (parts.isEmpty()) {
            throw MalformedInputException("Error: Empty command input.")
        }

        val commandName = parts.first()
        val command = commands[commandName] as? Command<Source> ?: throw UnknownCommandException(commandName)

        val parsedArguments = mutableMapOf<String, Any>()
        var currentPartIndex = 1

        var lastValidRunner: CommandRunner<Source>? = command.rootNode.runs
        var nodesToSearch: List<CommandNode<Source>> = command.rootNode.children

        while (currentPartIndex < parts.size) {
            val currentInputPart = parts[currentPartIndex]
            var matchFoundForThisPart = false

            // literals
            val literalMatch = nodesToSearch.filterIsInstance<CommandNode.Literal<Source>>()
                .find { it.name.equals(currentInputPart, ignoreCase = true) }

            if (literalMatch != null) {
                lastValidRunner = literalMatch.runs
                nodesToSearch = literalMatch.children
                currentPartIndex++
                matchFoundForThisPart = true
            } else {
                // args
                val argMatch = nodesToSearch.firstNotNullOfOrNull { node ->
                    when (node) {
                        is CommandNode.Argument<*, *> if node.type !is StringArgumentType -> {
                            Pair(node, (node.type as ArgumentType<Any>).parse(currentInputPart))
                        }

                        is CommandNode.DynamicArgument<*, *> if node.type !is StringArgumentType -> {
                            Pair(node, (node.type as ArgumentType<Any>).parse(currentInputPart))
                        }

                        else -> null
                    }
                }

                if (argMatch != null) {
                    val (node, value) = argMatch
                    parsedArguments[node.name] = value

                    if (node is CommandNode.DynamicArgument<*, *>) {
                        val dynamicBuilder = ArgumentBuilder<Source, Any>(node.name, node.type as ArgumentType<Any>)
                        (node.dynamicBlock as ArgumentBuilder<Source, Any>.(Any) -> Unit)(dynamicBuilder, value)
                        lastValidRunner = dynamicBuilder.runner
                        nodesToSearch = dynamicBuilder.children.map { it.build() }
                    } else {
                        lastValidRunner = node.runs as CommandRunner<Source>?
                        nodesToSearch = node.children as List<CommandNode<Source>>
                    }
                    currentPartIndex++
                    matchFoundForThisPart = true
                } else {
                    // try greedy
                    val stringArgNode = nodesToSearch.firstNotNullOfOrNull { node ->
                        if ((node is CommandNode.Argument<*, *> && node.type is StringArgumentType) || (node is CommandNode.DynamicArgument<*, *> && node.type is StringArgumentType)) {
                            node
                        } else null
                    }

                    if (stringArgNode != null) {
                        val remainingInput = parts.subList(currentPartIndex, parts.size).joinToString(" ")
                        val value =
                            (stringArgNode as? CommandNode.Argument<Source, String>)?.type?.parse(remainingInput)
                                ?: (stringArgNode as? CommandNode.DynamicArgument<Source, String>)?.type?.parse(
                                    remainingInput
                                )

                        if (value != null) {
                            parsedArguments[stringArgNode.name] = value
                            if (stringArgNode is CommandNode.DynamicArgument<*, *>) {
                                val dynamicBuilder = ArgumentBuilder<Source, String>(
                                    stringArgNode.name,
                                    stringArgNode.type as StringArgumentType
                                )
                                (stringArgNode.dynamicBlock as ArgumentBuilder<Source, String>.(String) -> Unit)(
                                    dynamicBuilder,
                                    value
                                )
                                lastValidRunner = dynamicBuilder.runner
                                nodesToSearch = dynamicBuilder.children.map { it.build() }
                            } else {
                                lastValidRunner = stringArgNode.runs
                                nodesToSearch = stringArgNode.children
                            }
                            // greedy; no more parts
                            currentPartIndex = parts.size
                            matchFoundForThisPart = true
                        }
                    }
                }
            }

            if (!matchFoundForThisPart) {
                throw IllegalArgumentException("Invalid subcommand or argument '${currentInputPart}'.")
            }
        }

        if (lastValidRunner != null) {
            val context = CommandContext(source, parsedArguments)
            lastValidRunner(context)
        } else {
            throw MalformedInputException(
                "Incomplete command. Expected more arguments or a valid subcommand after '${
                    parts.joinToString(
                        " "
                    )
                }'."
            )
        }
    }

    fun <Source : CommandSource> tryAutoComplete(partial: String, source: Source): List<String> {
        val parts = partial.trim().split(" ").filter { it.isNotEmpty() }
        if (parts.isEmpty()) return emptyList()

        val commandName = parts.first()
        val command = commands[commandName] as? Command<Source> ?: return commands.filter { it.key.startsWith(parts.first(), ignoreCase = true) }.map { it.key }

        var currentPartIndex = 1
        val nodesToSearch: List<CommandNode<Source>> = command.rootNode.children

        while (currentPartIndex < parts.size) {
            val currentInputPart = parts[currentPartIndex]

            val literalMatch = nodesToSearch.filterIsInstance<CommandNode.Literal<Source>>()
                .filter { it.name.startsWith(currentInputPart, ignoreCase = true) }


            if (literalMatch.isNotEmpty()) {
                return literalMatch.map { partial + it.name.removePrefix(currentInputPart) }
                    .sortedBy { it.length }
                    .filter { it.isNotEmpty() }
                    .distinct()
            }
            currentPartIndex++
        }
        return emptyList()
    }
}