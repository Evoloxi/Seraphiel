package me.evo.seraphiel.api

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class PlayerDBResponse(val data: PlayerDBData) {
    @Serializable
    data class PlayerDBData(val player: PlayerDBPlayer) {
        @Serializable
        data class PlayerDBPlayer @OptIn(ExperimentalUuidApi::class) constructor(val id: Uuid, val username: String)
    }
}