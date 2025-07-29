package me.evo.seraphiel.request

import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.extension.tickerFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.minutes

class ApiHandler(
    private val client: HttpClient,
    val maxRequestsPerMinute: Int
) {
    private val mutex = Mutex()
    private val requestQueue = ArrayDeque<suspend () -> Unit>(32)
    private var requestCount = 0

    init {
        tickerFlow(5.minutes).onEach {
            mutex.withLock { requestCount = 0 }
            processQueue()
        }.launchIn(Seraphiel.IO)
    }


    suspend fun <T> executeRequest(block: suspend () -> T): T =
        suspendCancellableCoroutine { cont ->
            val request: suspend () -> Unit = {
                try {
                    val result = block()
                    cont.resume(result)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
            addRequestToQueue(request)
        }

    private fun addRequestToQueue(request: suspend () -> Unit) {
        Seraphiel.launch {
            mutex.withLock {
                if (requestCount < maxRequestsPerMinute) {
                    requestCount++
                    Seraphiel.IO.launch { request() }
                } else {
                    requestQueue.addLast(request)
                }
            }
        }
    }

    private suspend fun processQueue() {
        mutex.withLock {
            while (requestQueue.isNotEmpty() && requestCount < maxRequestsPerMinute) {
                val request = requestQueue.removeFirst()
                requestCount++
                Seraphiel.IO.launch { request() }
            }
        }
    }
}