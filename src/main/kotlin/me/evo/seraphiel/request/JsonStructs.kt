package me.evo.seraphiel.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.evo.seraphiel.api.Player
import me.evo.seraphiel.json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class SuspectRequest(
    val name: String? = null,
    val uuid: Uuid? = null
) {
    init {
        require(name != null || uuid != null) { "Either name or uuid must be provided" }
    }
}
@Serializable
data class HypixelResponse(val player: Player?)

@OptIn(ExperimentalUuidApi::class)
interface UuidCarrier {
    val uuid: Uuid
}

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Suspect(val name: String, override val uuid: Uuid) : UuidCarrier {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Suspect) return false

        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}

@Serializable
data class SuspectResponse(val success: Boolean, val suspect: Suspect?) {
    constructor(suspect: Suspect) : this(true, suspect)

    companion object {
        val EMPTY = SuspectResponse(true, null)
    }
}

@Serializable
data class ErrorResponse(val success: Boolean, val message: String) {
    constructor(message: String) : this(false, message)
}

@Serializable
data class MultiSuspectResponse(val success: Boolean, val suspects: List<Suspect> = emptyList()) {
    constructor(suspects: List<Suspect>) : this(true, suspects)

    companion object {
        val EMPTY = MultiSuspectResponse(true, emptyList())
    }
}

inline fun <reified T> decode(element: JsonElement): T = json.decodeFromJsonElement(element)

fun JsonObject.getAllWithPrefix(prefix: String) = JsonObject(filter { it.key.startsWith(prefix) })

operator fun JsonElement?.get(key: String): JsonElement? = when (this) {
    is JsonObject -> this[key]
    else -> null
}

operator fun JsonElement?.get(vararg keys: String): List<JsonElement?>? = when (this) {
    is JsonObject -> keys.map { this[it] }
    else -> null
}