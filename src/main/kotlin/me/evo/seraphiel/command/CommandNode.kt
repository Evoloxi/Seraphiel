package me.evo.seraphiel.command

sealed class CommandNode<S : CommandSource>(
    val name: String,
    val runs: CommandRunner<S>?
) {

    abstract val children: List<CommandNode<S>>

    class Literal<S : CommandSource>(
        name: String,
        override val children: List<CommandNode<S>>,
        runs: CommandRunner<S>?
    ) : CommandNode<S>(name, runs)

    class Argument<S : CommandSource, T : Any>(
        name: String,
        val type: ArgumentType<T>,
        override val children: List<CommandNode<S>>,
        runs: CommandRunner<S>?
    ) : CommandNode<S>(name, runs)

    internal class DynamicArgument<S : CommandSource, T : Any>(
        name: String,
        val type: ArgumentType<T>,

        val dynamicBlock: ArgumentBuilder<S, T>.(T) -> Unit
    ) : CommandNode<S>(name, null) {
        override val children: List<CommandNode<S>> = emptyList()
    }
}
