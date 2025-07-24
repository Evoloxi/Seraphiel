package me.evo.seraphiel.listener

import kotlinx.coroutines.async
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Utils
import me.evo.seraphiel.Utils.debug
import me.evo.seraphiel.data.TimedSet
import me.evo.seraphiel.event.EntityListEvent
import me.evo.seraphiel.now
import me.evo.seraphiel.request.ApiBridge
import me.evo.seraphiel.request.SuspectRequest
import me.evo.seraphiel.then
import net.minecraft.entity.player.EntityPlayer
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
    val checked: TimedSet<Uuid> = TimedSet(1000, 5.minutes)
    val buffer = HashMap<Uuid, String?>(BUFFER_SIZE)
    var lastFetch = now

    @SubscribeEvent
    fun onPlayerJoin(event: EntityListEvent.Add) {
        if (event.entity !is EntityPlayer) return
        val player = event.entity
        if (player.uniqueID.version() != 4 || checked.contains(player.uniqueID.toKotlinUuid()) || buffer.contains(player.uniqueID.toKotlinUuid())) {
            return
        }
        buffer[player.uniqueID.toKotlinUuid()] = player.name

        if (now - lastFetch < 5.seconds && buffer.size < BUFFER_SIZE) {
            return
        }

        debug("Checking players: ${checked.size} total, ${buffer.size} buffered")

        checked.addAll(buffer.keys)
        val copy = buffer.toMap()
        buffer.clear()
        lastFetch = now

        Seraphiel.IO.async {
            ApiBridge.checkPlayers(copy.map { SuspectRequest(it.value, it.key) })
        }.then { cheaters ->
            if (cheaters != null && cheaters.isNotEmpty()) {
                cheaters.forEach { cheater ->
                    Utils.info("Found a cheater: §c${cheater.name}§7 (UUID: §6${cheater.uuid}§7)")
                }
                Seraphiel.Companion.mc.addScheduledTask {
                    Seraphiel.Companion.mc.thePlayer?.playSound("note.pling", 1.0f, 1.0f)
                }
            }
        }
    }
}