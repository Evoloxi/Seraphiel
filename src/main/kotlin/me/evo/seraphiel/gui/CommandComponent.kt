package me.evo.seraphiel.gui

data class CommandComponent(val command: String, val argument: String) {
    fun isViewProfileCommand(): Boolean = command.startsWith("/viewprofile")
}