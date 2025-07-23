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
import me.evo.seraphiel.CommandComponent
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

    private const val CACHE_SIZE = 200
    private val PLAYER_CACHE_DURATION = 5.minutes
    private val UUID_CACHE_DURATION = 60.minutes
    private val ACTIVATION_DURATION = 0.4.seconds

    private val mutex          = Mutex()
    private val playerCache    = TimedCache<Uuid, Player>(CACHE_SIZE, PLAYER_CACHE_DURATION)
    private val loadingPlayers = LimitedSet<Uuid>(CACHE_SIZE)
    private val invalidPlayers = LimitedSet<Uuid>(CACHE_SIZE)
    private val loadingUuids   = LimitedSet<String>(CACHE_SIZE)
    private val uuidCache      = TimedCache<String, Uuid>(CACHE_SIZE, UUID_CACHE_DURATION)
    private val invalidUuids   = LimitedSet<String>(CACHE_SIZE)

    private var lastHoveredText: String? = null
    private var lastHoverTime = TimeSource.Monotonic.markNow()

    private val isHoverReady: Boolean
        get() = lastHoverTime.elapsedNow() > ACTIVATION_DURATION && lastHoveredText != null

    fun addPrefetched(it: Player) {
        uuidCache[it.displayName] = it.uuid ?: return
        playerCache[it.uuid] = it
        loadingPlayers -= it.uuid
        loadingUuids -= it.displayName
    }

    @JvmStatic
    fun updateHovered(component: IChatComponent?): IChatComponent? {
        val hoverEvent = component?.chatStyle?.chatHoverEvent ?: run {
            resetHoverState()
            return component
        }

        val hoverText = hoverEvent.value?.formattedText ?: return component
        updateHoverState(hoverText, component.chatStyle?.chatClickEvent)

        val clickEvent = component.chatStyle?.chatClickEvent ?: return component
        val cmd = parseClickCommand(clickEvent) ?: return component

        return if (cmd.isViewProfileCommand()) {
            injectIntoComponent(component, hoverText, cmd.argument)
        } else {
            component
        }
    }

    private fun resetHoverState() {
        lastHoveredText = null
    }

    private fun updateHoverState(text: String, clickEvent: ClickEvent?) {
        if (text != lastHoveredText) {
            debug("Hover changed: ${clickEvent?.value}")
            lastHoveredText = text
            lastHoverTime = TimeSource.Monotonic.markNow()
        }
    }

    private fun parseClickCommand(clickEvent: ClickEvent): CommandComponent? {
        if (clickEvent.action != ClickEvent.Action.RUN_COMMAND) return null

        val parts = clickEvent.value?.split(" ", limit = 2) ?: return null
        return if (parts.size == 2) CommandComponent(parts[0], parts[1]) else null
    }

    private fun injectIntoComponent(component: IChatComponent, text: String, playerArg: String): IChatComponent {
        val modifiedComponent = component.createCopy()
        val contentLines = text.split("\n").toMutableList()

        patchContentColors(contentLines)

        val uuid = resolvePlayerUuid(playerArg)
        val statisticsText = generateStatisticsText(playerArg, uuid)

        insertStatisticsIntoContent(contentLines, statisticsText)
        addPrestige(contentLines, uuid)

        modifiedComponent.chatStyle?.chatHoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            ChatComponentText(contentLines.joinToString("\n"))
        )

        return modifiedComponent
    }

    private fun patchContentColors(content: MutableList<String>) {
        val defaultColor = extractLastColor(content) ?: "7"
        content.replaceAll { line ->
            if (line.contains("§")) line else "§$defaultColor$line"
        }
    }

    private fun extractLastColor(content: List<String>): String? =
        content.lastOrNull()
            ?.substringAfterLast("§")
            ?.takeIf { it.length == 1 }

    private fun resolvePlayerUuid(playerArg: String): Uuid? =
        uuidCache[playerArg] ?: playerArg.takeIf { it.isValidUuid() }?.let {
            runCatching { Uuid.parse(it) }.getOrNull()
        }

    private fun generateStatisticsText(playerArg: String, uuid: Uuid?): String = when {
        playerArg in loadingUuids ->
            "§7Loading UUID${loadingAnimation}"

        playerArg in invalidUuids || uuid in invalidPlayers ->
            "§cError loading player statistics"

        uuid in loadingPlayers ->
            "§7Loading${loadingAnimation}"

        uuid != null && uuid in playerCache ->
            formatPlayerStatistics(playerCache[uuid]!!)

        shouldStartLoading(uuid) -> {
            initiateDataLoading(playerArg, uuid)
            "§7Loading${loadingAnimation}"
        }

        else -> progressBar
    }

    private fun formatPlayerStatistics(player: Player): String =
        "§8(${formatFKDR(player.fkdr)} §8| ${formatBBLR(player.bblr)} §8| ${formatWLR(player.wlr)}§8)"

    private fun shouldStartLoading(uuid: Uuid?): Boolean =
        isHoverReady || (uuid != null && uuid in ApiBridge.statcache)

    private fun initiateDataLoading(playerArg: String, uuid: Uuid?) {
        if (uuid == null) {
            requestPlayerUuid(playerArg)
        } else {
            IO.launch { loadPlayerData(uuid) }
        }
    }

    private fun insertStatisticsIntoContent(content: MutableList<String>, statisticsText: String) {
        val insertIndex = content.indexOfFirst { line ->
            line.containsAny("Click to view", "Click here to view")
        }.coerceIn(1, content.size)

        content.add(insertIndex, statisticsText)
    }

    private fun addPrestige(content: MutableList<String>, uuid: Uuid?) {
        uuid?.let { playerId ->
            playerCache[playerId]?.let { player ->
                val prestigeText = prestige(player.stars)

                if (content.any { it.contains("Friends for") }) {
                    val lastIndex = content.lastIndex
                    content[lastIndex] = content[lastIndex].replace(
                        "Click here to view",
                        "Click here to view $prestigeText"
                    )
                } else {
                    content[0] = "$prestigeText ${content[0]}"
                }
            }
        }
    }

    private fun String.isValidUuid(): Boolean =
        runCatching { Uuid.parse(this) }.isSuccess

    private fun requestPlayerUuid(playerName: String) {
        if (playerName in uuidCache || playerName in loadingUuids) return

        IO.launch {
            loadingUuids += playerName
            try {
                val uuid = ApiBridge.getPlayerUUID(playerName)
                if (uuid != null) {
                    uuidCache[playerName] = uuid
                } else {
                    invalidUuids.add(playerName)
                }
            } catch (e: Exception) {
                invalidUuids.add(playerName)
                debug("Failed to fetch UUID for $playerName: ${e.message}")
            } finally {
                loadingUuids -= playerName
            }
        }
    }

    private val progressBar: String
        get() {
            val message = "Continue to hover for statistics."
            val progress = ((lastHoverTime.elapsedNow() / ACTIVATION_DURATION) * message.length)
                .toInt()
                .coerceIn(0, message.length)

            return "§6${message.take(progress)}§e${message.drop(progress)}"
        }

    private val loadingAnimation: String
        get() {
            val frames = listOf(".", "..", "...", "....", " ...", "  ..", "   .", "    ")
            val ticksExisted = Seraphiel.mc.thePlayer?.ticksExisted ?: 0
            val frameIndex = (ticksExisted / 2) % frames.size
            return frames[frameIndex]
        }

    private suspend fun loadPlayerData(uuid: Uuid) {
        mutex.withLock {
            if (uuid in playerCache || uuid in loadingPlayers || uuid in invalidPlayers) return
            loadingPlayers += uuid
        }

        try {
            val playerData = ApiBridge.getPlayerStats(uuid)

            mutex.withLock {
                if (playerData != null) {
                    playerCache[uuid] = playerData
                } else {
                    invalidPlayers.add(uuid)
                }
                loadingPlayers -= uuid
            }

            debug("Completed loading data for $uuid")
        } catch (e: Exception) {
            mutex.withLock {
                loadingPlayers -= uuid
            }
            debug("Failed to load data for $uuid: ${e.message}")
        }
    }

}