package me.evo.seraphiel.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Achievements(
    @SerialName("bedwars_level") val bedwarsLevel: Int = 0
)