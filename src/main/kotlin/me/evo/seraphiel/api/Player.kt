@file:OptIn(ExperimentalUuidApi::class)
@file:Suppress("PropertyName")

package me.evo.seraphiel.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*
import kotlin.collections.emptyList
import kotlin.math.pow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
/*
@KeepGeneratedSerializer
*/
@Serializable/*(with = HypixelPlayerSerializer::class)*/
data class Player(
    @SerialName("_id")                val entryId            : String = "",
    @SerialName("uuid")               val uuid               : Uuid?  = null,
    @SerialName("displayname")        val displayName        : String = "",
    @SerialName("rank")               val rank               : String = "",
    @SerialName("packageRank")        val packageRank        : String = "",
    @SerialName("newPackageRank")     val newPackageRank     : String = "",
    @SerialName("monthlyPackageRank") val monthlyPackageRank : String = "",
    @SerialName("firstLogin")         val firstLogin         : Long   = 0,
    @SerialName("lastLogin")          val lastLogin          : Long   = 0,
    @SerialName("lastLogout")         val lastLogout         : Long   = 0,
    @SerialName("networkExp")         val networkExp         : Double = 0.0,
    @SerialName("networkLevel")       val networkLevel       : Double = 0.0,
    @SerialName("mcVersionRp")        val mcVersion          : String = "",
    @SerialName("stats")              val stats              : Stats,
    @SerialName("achievements")       val achievements       : Achievements,
    val activityTimestamps: List<Long> = emptyList(),
    ) {
    val stars: Int get() = achievements.bedwarsLevel
    val fkdr: Double
        get() = stats.bedwars.fkdr
    val wlr: Double
        get() = stats.bedwars.wlr
    val bblr: Double
        get() = stats.bedwars.bblr
    val skill: Double
        get() = stars.toDouble().pow(1.1) * (fkdr.pow(2.5) + wlr.pow(2.2) + bblr.pow(3.3))
    /**
     * Retrieves the displayed rank of this player (Player's top-most rank in the Rank Hierarchy)
     */
    val highestRank
        get() = when {
            !rank.isDefaultRank()               -> rank
            !monthlyPackageRank.isDefaultRank() -> monthlyPackageRank
            !newPackageRank.isDefaultRank()     -> newPackageRank
            !packageRank.isDefaultRank()        -> packageRank
            else                                -> "NONE"
        }

    val hasRank get() = highestRank != "NONE"

    private fun String.isDefaultRank() = this == "NONE" || this == "NORMAL" || isEmpty()
}

@Serializable
data class Stats(
    @SerialName("Bedwars") val bedwars : Bedwars = Bedwars(),
    @SerialName("Duels")   val duels   : Duels = Duels(),
)

@Serializable
data class QuestProgress(
    val completions: List<Completion> = emptyList(),
    val active: Active? = null
)

@Serializable
data class Completion(
    val time: Long
)

@Serializable
data class Active(
    val objectives: Map<String, JsonElement> = emptyMap(),
    val started: Long
)
