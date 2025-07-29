@file:Suppress("unused")

package me.evo.seraphiel.command

import kotlinx.coroutines.async
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.Utils.chat
import me.evo.seraphiel.Utils.info
import me.evo.seraphiel.extension.then
import kotlin.uuid.ExperimentalUuidApi

object CommandRegistry {
    @OptIn(ExperimentalUuidApi::class)
    val suspect = command("suspect") {
        string("name") { name ->
            runs {
                chat("Checking $name...")
                Seraphiel.IO.async {
/*                    if (Uuid.parse(name) != null) {
                        ApiBridge.getPlayer(SuspectRequest(null, uuid))
                    } else {
                        ApiBridge.getPlayer(Suspect(name, null))
                    }*/
                }.then { player ->
                    if (player == null) {
                        info("Player not found in database.")
                    } else {
                        //info("Player found: §6${player.name}§7 (UUID: §6${player.uuid}§7)")
                    }
                }
            }
        }
    }
}