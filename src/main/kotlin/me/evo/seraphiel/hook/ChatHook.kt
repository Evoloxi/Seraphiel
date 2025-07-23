package me.evo.seraphiel.hook

import kotlinx.coroutines.async
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Seraphiel.Companion.mc
import me.evo.seraphiel.Location
import me.evo.seraphiel.Utils
import me.evo.seraphiel.event.ChatEvent
import me.evo.seraphiel.extension.containsAny
import me.evo.seraphiel.gui.HoverStats
import me.evo.seraphiel.request.ApiBridge
import me.evo.seraphiel.then
import net.minecraft.event.ClickEvent
import net.weavemc.loader.api.event.SubscribeEvent
import org.lwjgl.opengl.Display
import kotlin.uuid.ExperimentalUuidApi


@Suppress("unused")
object ChatHook {
    private val senderRegex = Regex("\\[([^]]+)]\\s*([^:]+):")
    private val rankRegex = Regex("\\[.*]")

    private val filter = listOf("vip", "rank", "pls", "skyblock", "gift", "plz")

    @OptIn(ExperimentalUuidApi::class)
    @SubscribeEvent
    fun onYapInLobby(event: ChatEvent.Receive) { // TODO: Refactor
        if (!Display.isActive()) return
        if (Utils.location != Location.LOBBY) return
        val message = event.message.unformattedText?.replace(Utils.color_codes, "") ?: return
        if (message.replace(rankRegex, "").containsAny(filter)) return
        val sender = senderRegex.find(message)?.groupValues?.get(2)?.replace(rankRegex, "")?.replace(" ", "") ?: return
        if (sender.contains("'sHypeTrain")) return
        Utils.debug("Requesting stats for yapper: $sender in advance")
        Seraphiel.IO.async { ApiBridge.getPlayerStats(sender) }.then {
            if (it?.uuid != null) {
                HoverStats.loaded[it.uuid] = it
                HoverStats.loadedMojang[sender] = it.uuid
                HoverStats.loading.remove(it.uuid)
                HoverStats.loadingMojang.remove(sender)
            }
        }
    }

    @SubscribeEvent
    fun onReceiveStatsLink(event: ChatEvent.Receive) {
        val message = event.message.chatStyle?.chatClickEvent ?: return
        if (message.action == ClickEvent.Action.OPEN_URL) {
            val url = message.value
            if (url.contains("stats.hypixel.net")) {
                mc.thePlayer.playSound("note.bassattack", 1f, 1.4f)
                println("GAMELINK: $url")
            }
        }
    }
}
