package me.evo.seraphiel.command

class ArgumentBuilder<Source : CommandSource, T : Any>(name: String, val type: ArgumentType<T>) : CommandBuilder<Source>(name) {
    internal var dynamicExecutor: (ArgumentBuilder<Source, T>.(T) -> Unit)? = null

    override fun build(): CommandNode<Source> {
        return if (dynamicExecutor != null) {
            CommandNode.DynamicArgument(name, type, dynamicExecutor!!)
        } else {
            CommandNode.Argument(name, type, children.map { it.build() }, runner)
        }
    }
}

class LiteralBuilder<S : CommandSource>(name: String) : CommandBuilder<S>(name) {
    override fun build(): CommandNode.Literal<S> {
        return CommandNode.Literal(name, children.map { it.build() }, runner)
    }
}