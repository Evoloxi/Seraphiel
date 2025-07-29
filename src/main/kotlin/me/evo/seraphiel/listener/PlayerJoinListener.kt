package me.evo.seraphiel.listener

import kotlinx.coroutines.async
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Seraphiel.Companion.mc
import me.evo.seraphiel.Utils
import me.evo.seraphiel.data.TimedSet
import me.evo.seraphiel.event.EntityListEvent
import me.evo.seraphiel.gui.HoverStats
import me.evo.seraphiel.extension.now
import me.evo.seraphiel.request.ApiBridge
import me.evo.seraphiel.request.SuspectRequest
import me.evo.seraphiel.extension.then
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.weavemc.loader.api.event.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
@Suppress("unused")
object PlayerJoinListener {
    const val BUFFER_SIZE = 24
    val checked: TimedSet<Uuid> = TimedSet(1024, 5.minutes)
    val buffer = HashMap<Uuid, String?>(BUFFER_SIZE)
    var lastFetch = now

    @SubscribeEvent
    fun onPlayerJoin(event: EntityListEvent.Add) {
        if (event.entity !is EntityPlayer) return
        val player = event.entity
        val uuid = player.uniqueID.toKotlinUuid()
        if (player.uniqueID.version() != 4 || uuid in checked || uuid in buffer) {
            return
        }
        buffer[uuid] = player.name

        if (now - lastFetch < 5.seconds && buffer.size < BUFFER_SIZE) {
            return
        }

        checked.addAll(buffer.keys)
        val copy = buffer.toMap()
        buffer.clear()
        lastFetch = now

        Seraphiel.IO.async {
            ApiBridge.checkPlayers(copy.map { SuspectRequest(it.value, it.key) })
        }.then { cheaters ->
            if (cheaters != null && cheaters.isNotEmpty()) {
                cheaters.forEach { cheater ->
                    Seraphiel.IO.async {
                        ApiBridge.getPlayerStats(cheater.uuid)
                    }.then {
                        it?.let(HoverStats::addPrefetched)
                    }
                    Utils.chat("§7[§d❁§7] §8» §7Found a cheater: §5${cheater.name}§7\n§8(uuid: ${cheater.uuid})") {
                        chatStyle.apply {
                            chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewprofile ${cheater.uuid}")
                            chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                                """
                                    §c${cheater.name}
                                    §8Click to view their profile!
                                    """.trimIndent()
                            ))
                        }
                    }
                }
                mc.addScheduledTask {
                    mc.thePlayer?.playSound("note.pling", 0.3f, 1.97f)
                }
            }
        }
    }
}