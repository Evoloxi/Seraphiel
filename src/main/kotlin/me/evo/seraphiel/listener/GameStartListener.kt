@file:OptIn(ExperimentalUuidApi::class)

package me.evo.seraphiel.listener

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Seraphiel.Companion.mc
import me.evo.seraphiel.Utils.info
import me.evo.seraphiel.event.LocationChangeEvent
import me.evo.seraphiel.request.ApiBridge
import me.evo.seraphiel.request.SuspectRequest
import me.evo.seraphiel.then
import net.minecraft.client.network.NetworkPlayerInfo
import net.weavemc.loader.api.event.SubscribeEvent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

object GameStartListener {

    @SubscribeEvent
    fun onGameStart(event: LocationChangeEvent) {
        println("onGameStart")

        val players: Collection<NetworkPlayerInfo> = Seraphiel.mc.netHandler.playerInfoMap
        val suspectList = players.map { SuspectRequest(it.gameProfile.name, it.gameProfile.id.toKotlinUuid()) }
        Seraphiel.IO.async {
            delay(2000)
            ApiBridge.checkPlayers(suspectList)
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


