package me.evo.seraphiel.gui

import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Utils.debug
import me.evo.seraphiel.request.ApiBridge
import kotlin.uuid.Uuid

import me.evo.seraphiel.Seraphiel.IO
import me.evo.seraphiel.api.Player
import me.evo.seraphiel.data.Formatter.formatBBLR
import me.evo.seraphiel.data.Formatter.formatFKDR
import me.evo.seraphiel.data.Formatter.formatWLR
import me.evo.seraphiel.data.Formatter.prestige
import me.evo.seraphiel.data.LimitedSet
import me.evo.seraphiel.data.TimedCache
import me.evo.seraphiel.extension.containsAny
import kotlin.uuid.ExperimentalUuidApi

// TODO: Refactor this ancient ass code
@OptIn(ExperimentalUuidApi::class)
object HoverStats {

    private val mutex = Mutex()
    val loaded = TimedCache<Uuid, Player>(200, 5.minutes)
    val loading = LimitedSet<Uuid>(200)
    private val invalid = LimitedSet<Uuid>(200)
    val loadingMojang = LimitedSet<String>(200)
    val loadedMojang = TimedCache<String, Uuid>(200, 60.minutes)
    private val invalidMojang = LimitedSet<String>(200)
    private var lastText: String? = null
    private var lastHovered = TimeSource.Monotonic.markNow()
    private val activationDuration get() = 0.4.seconds
    private val ready get() = lastHovered.elapsedNow() > activationDuration && lastText != null

    @JvmStatic
    fun updateHovered(component: IChatComponent?): IChatComponent? {
        val chatStyle = component?.chatStyle ?: return component
        val hoverEvent = chatStyle.chatHoverEvent ?: run {
            lastText = null
            return component
        }

        val text = hoverEvent.value?.formattedText ?: return component

        if (text != lastText) {
            debug("Hovered action changed: ${chatStyle.chatClickEvent?.value}")
            lastText = text
            lastHovered = TimeSource.Monotonic.markNow()
        }

        val clickEvent = chatStyle.chatClickEvent ?: return component
        val (cmd, arg) = clickEvent.value?.split(" ", limit = 2)?.takeIf { it.size == 2 } ?: return component

        if (clickEvent.action == ClickEvent.Action.RUN_COMMAND && cmd.startsWith("/viewprofile")) {
            return handleViewProfile(component, text, arg)
        }

        return component
    }

    private fun handleViewProfile(component: IChatComponent, text: String, arg: String): IChatComponent {
        val copy = component.createCopy()
        val content = text.split("\n").toMutableList()
        applyLastColor(content)

        val uuid = loadedMojang[arg] ?: arg.takeIf { it.isUuid }?.let(Uuid::parse)
        val hoverText = getHoverText(arg, uuid)
        val insertIndex = content.indexOfFirst { it.containsAny("Click to view", "Click here to view") }
            .coerceIn(1, content.size)
        content.add(insertIndex, hoverText)

        uuid?.let { uid ->
            loaded[uid]?.let { player ->
                if (content.any { it.contains("Friends for") }) {
                    val last = content.lastIndex
                    content[last] = content[last].replace("Click here to view", "Click here to view ${prestige(player.stars)}")
                } else {
                    content[0] = "${prestige(player.stars)} ${content[0]}"
                }
            }
        }

        copy.chatStyle?.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(content.joinToString("\n")))
        return copy
    }

    private fun applyLastColor(content: MutableList<String>) {
        val lastColor = content.lastOrNull()?.substringAfterLast("§")?.takeIf { it.length == 1 } ?: "7"
        content.replaceAll { line -> if (line.contains("§")) line else "§$lastColor$line" }
    }

    private fun getHoverText(arg: String, uuid: Uuid?): String = when {
        arg in loadingMojang -> "§7Loading Uuid$current"
        arg in invalidMojang || uuid in invalid -> "§cAn error occurred while loading this player's statistics."
        uuid in loading -> "§7Loading$current"
        uuid != null && uuid in loaded -> loaded[uuid]!!.run {
            "§8(${formatFKDR(fkdr)} §8| ${formatBBLR(bblr)} §8| ${formatWLR(wlr)}§8)"
        }
        ready || ((uuid ?: false) in ApiBridge.statcache) -> {
            if (uuid == null) requestUuid(arg) else IO.launch { request(uuid) }
            "§7Loading$current"
        }
        else -> drawBar()
    }

    private val String.isUuid: Boolean
        get() = runCatching { Uuid.parse(this) }.isSuccess

    private fun requestUuid(name: String) {
        if (name in loadedMojang || name in loadingMojang) return
        IO.launch {
            loadingMojang += name
            try {
                ApiBridge.getPlayerUUID(name)?.let {
                    loadedMojang[name] = it
                } ?: run {
                    invalidMojang.add(name)
                }
            } catch (e: Exception) {
                invalidMojang.add(name)
                debug("Error fetching Uuid for $name: ${e.message}")
            } finally {
                loadingMojang -= name
            }
        }
    }


    private fun drawBar(): String {
        val bar = "Continue to hover for statistics."
        val progress = ((lastHovered.elapsedNow() / activationDuration) * bar.length).toInt().coerceIn(0, bar.length)
        return "§6${bar.take(progress)}§e${bar.drop(progress)}"
    }

    private val current: String
        get() {
            val states = listOf(".", "..", "...", "....", " ...", "  ..", "   .", "    ")
            val index = ((Seraphiel.mc.thePlayer?.ticksExisted ?: 0) / 2) % states.size
            return states[index]
        }

    private suspend fun request(uuid: Uuid) {
        mutex.withLock {
            if (uuid in loaded || uuid in loading || uuid in invalid) return
            loading += uuid
        }

        try {
            //debug("Loading $uuid")
            val result = ApiBridge.getPlayerStats(uuid)

            mutex.withLock {
                if (result != null) {
                    loaded[uuid] = result
                    loading -= uuid
                } else {
                    invalid.add(uuid)
                    loading -= uuid
                }
            }
            debug("Removed $uuid from loading")
        } catch (e: Exception) {
            mutex.withLock {
                loading -= uuid
            }
            debug("Error loading $uuid: ${e.message}")
        }
    }
}
