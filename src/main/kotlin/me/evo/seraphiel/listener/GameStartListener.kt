@file:OptIn(ExperimentalUuidApi::class)

package me.evo.seraphiel.listener

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Seraphiel.Companion.mc
import me.evo.seraphiel.data.TimedCache
import me.evo.seraphiel.Utils.info
import me.evo.seraphiel.event.EntityListEvent
import me.evo.seraphiel.event.LocationChangeEvent
import me.evo.seraphiel.request.ApiBridge
import me.evo.seraphiel.request.SuspectRequest
import me.evo.seraphiel.then
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer
import net.weavemc.loader.api.event.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

object GameStartListener {

    @SubscribeEvent
    fun onGameStart(event: LocationChangeEvent) {
        println("onGameStart")

        val players: Collection<NetworkPlayerInfo> = Seraphiel.mc.netHandler.playerInfoMap
        val suspectList = players.map { SuspectRequest(it.gameProfile.name, it.gameProfile.id.toKotlinUuid()) }
        Seraphiel.IO.async {
            delay(2000)
            ApiBridge.getPlayers(suspectList)
        }.then { cheaters ->
            if (cheaters?.isEmpty() ?: true) {
                return@then
            } else {
                val string = if (cheaters.size == 1) {
                    "Found a cheater: §6${cheaters[0].name}"
                } else {
                    "Found §b${cheaters.size}§7 cheaters: §6${cheaters.joinToString("§7, ") { "§6${it.name}" }}"
                }
                mc.addScheduledTask {
                    mc.thePlayer?.playSound("note.pling", 1.0f, 1.0f)
                }
                info(string)
            }
        }
    }
}


object PlayerJoinListener {
    val checked: TimedCache<Uuid, Byte> = TimedCache(1000, 5.minutes) // 5 minutes // TODO: move to TimedSet
    @SubscribeEvent
    fun onPlayerJoin(event: EntityListEvent.Add) {
        if (event.entity !is EntityPlayer) return
        val player = event.entity
        if (player.uniqueID.version() != 4 || checked.contains(player.uniqueID.toKotlinUuid())) {
            return
        }
        checked.put(player.uniqueID.toKotlinUuid(), 1)
        println("Checking player ${player.name} (${player.uniqueID}) for cheating...")
        Seraphiel.IO.async {
            ApiBridge.getPlayer(SuspectRequest(player.name, player.uniqueID.toKotlinUuid()))
        }.then { cheater ->
            if (cheater != null) {
                info("Found a cheater: §c${cheater.name}§7 (UUID: §6${cheater.uuid}§7)")
                mc.addScheduledTask {
                    mc.thePlayer?.playSound("note.pling", 1.0f, 1.0f)
                }
            }
        }
    }
}
