package me.evo.seraphiel

import me.evo.seraphiel.Seraphiel.Companion.LOGGER
import me.evo.seraphiel.event.LocationChangeEvent
import net.minecraft.util.ChatComponentText
import net.weavemc.loader.api.event.EventBus

object Utils {
    var location = Location.IRRELEVANT
        set(value) {
            if (field != value) {
                debug("Location changed: $field -> $value")
                EventBus.postEvent(LocationChangeEvent(from = field, to = value))
                field = value
            }
        }

    val color_codes = Regex("§.")
    @JvmStatic
    fun receiveSidebarData() {
        Seraphiel.mc.theWorld?.scoreboard?.scoreObjectives?.forEach {
            val stripped = it.displayName.replace(color_codes, "")
            location = when {
                stripped == "BED WARS" -> {
                    when (it.name) {
                        "Prototype" -> Location.LOBBY
                        "PreScoreboard" -> Location.QUEUE
                        "BForeboard" -> Location.GAME
                        else -> location
                    }
                }
                stripped == "REPLAY" -> Location.REPLAY

                !it.displayName.isNullOrEmpty() && !it.displayName.isNullOrBlank() && it.displayName.length < 3 -> Location.IRRELEVANT
                stripped in setOf("HYPIXEL", "PROTOTYPE") -> Location.LOBBY
                else -> {
                    debug("Unknown scoreboard: ${it.displayName} (${stripped}), ${it.name}, ${it.criteria}")
                    location
                }
            }
        }
    }
    fun debug(message: String) {
        if (true) { // TODO: Re-add config
            val functionName = Thread.currentThread().stackTrace.getOrNull(2)?.methodName ?: "Unknown"
            LOGGER.info("[~$functionName] $message")
        }
    }
    fun chat(message: String, builder: ChatComponentText.() -> Unit = {}) {
        val mc = Seraphiel.mc
        mc.addScheduledTask {
            mc.thePlayer?.addChatMessage(ChatComponentText(message).apply(builder))
        }
    }
    fun info(message: String) = chat("§7[§6Seraphiel§7] §7» §7$message")
    fun error(message: String) = chat("§7[§6Seraphiel§7] §7» §c§lERR: §r§7$message")
    fun success(message: String) = chat("§7[§6Seraphiel§7] §7» §a$message")
}

enum class Location {
    LOBBY,
    QUEUE,
    GAME,
    REPLAY,
    IRRELEVANT
}