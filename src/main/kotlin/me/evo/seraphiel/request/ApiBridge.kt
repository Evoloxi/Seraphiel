@file:OptIn(ExperimentalUuidApi::class)

package me.evo.seraphiel.request

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.data.TimedCache
import me.evo.seraphiel.Utils.debug
import me.evo.seraphiel.api.Player
import me.evo.seraphiel.api.PlayerDBResponse
import me.evo.seraphiel.internal.BuildInfo
import me.evo.seraphiel.json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid


object ApiBridge {
    val ktorClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        engine {
            config {
                followRedirects(true)
                callTimeout(5.seconds.toJavaDuration())
                connectTimeout(5.seconds.toJavaDuration())
                readTimeout(5.seconds.toJavaDuration())
            }
        }
    }
    private val handler = ApiHandler(ktorClient, 800)

    val suscache = TimedCache<Uuid, Suspect>(800, 5.minutes)
    val statcache = TimedCache<Uuid, Player>(800, 15.minutes)

    const val DATABASE_URL = "https://api.meownya.cloud/database/pull"
    suspend fun getPlayers(suspects: List<SuspectRequest>): List<Suspect> {
        val request = suspend sus@ {
            val (cached, uncached) = suspects.partition { it.uuid != null && suscache.contains(it.uuid) }
            val cachedPlayers = cached.mapNotNull { it.uuid }.mapNotNull { suscache[it] }
            if (uncached.isEmpty()) return@sus cachedPlayers
            val jsonData = json.encodeToString(suspects.map { SuspectRequest(it.name, it.uuid) })
            val response = handler.executeRequest {
                ktorClient.post(DATABASE_URL) {
                    headers.append("Authorization", BuildInfo.TOKEN)
                    setBody(jsonData)
                }
            }
            try {
                response.body<MultiSuspectResponse>().suspects.also { it.forEach { player -> suscache[player.uuid] } } + cachedPlayers
            } catch (e: Exception) {
                Seraphiel.LOGGER.error("API ERROR: ${e.message}", e)
                cachedPlayers
            }
        }
        return handler.executeRequest(request)
    }

    const val STAT_URL = "https://api.meownya.cloud/proxy/player"
    suspend fun getPlayerStats(uuid: Uuid): Player? {
        return statcache[uuid] ?: try {
            handler.executeRequest {
                debug("Fetching stats for $uuid")
                val res = ktorClient.get(STAT_URL) {
                    headers.append("Authorization", BuildInfo.TOKEN)
                    parameter("uuid", uuid.toString())
                }

                res.body<HypixelResponse>().player
            }?.also { statcache[uuid] = it }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPlayerStats(name: String): Player? {
        return getPlayerUUIDQuick(name)?.let { getPlayerStats(it) } ?: run {
            debug("UUID for $name not found, cannot fetch stats.")
            null
        }
    }

    const val PLAYERDB_URL = "https://playerdb.co/api/player/minecraft"
    val uuidcache = TimedCache<String /*name*/, Uuid>(5000, 8.hours)

    suspend fun getPlayerUUIDQuick(name: String): Uuid? {
        val online = Seraphiel.mc.netHandler.playerInfoMap.associate { it.gameProfile.name to it.gameProfile.id.toKotlinUuid() }.onEach {
            uuidcache[name] = it.value
        }
        return online[name] ?: uuidcache[name] ?: run {
            getPlayerUUID(name)
        }
    }

    suspend fun getPlayerUUID(name: String): Uuid? {
        return uuidcache[name] ?: try {
            val response = handler.executeRequest {
                ktorClient.get("$PLAYERDB_URL/$name") {
                    headers.append("User-Agent", "Seraphiel/1.0 (Minecraft Mod; Contact: mail@meownya.cloud)")
                }
            }
            val player = response.body<PlayerDBResponse>().data.player
            player.id.also { uuidcache[name] = it }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPlayer(suspect: SuspectRequest): Suspect? {
        return getPlayers(listOf(suspect)).firstOrNull()?.also { suscache[it.uuid] = it }
    }
}