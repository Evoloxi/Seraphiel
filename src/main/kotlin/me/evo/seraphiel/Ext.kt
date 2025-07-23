package me.evo.seraphiel

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.TimeSource

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
    encodeDefaults = true
    allowStructuredMapKeys = true
    explicitNulls = false
}

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Deferred<T>.then(callback: (T?) -> Unit) {
    invokeOnCompletion {
        if (it != null) {
            it.printStackTrace()
        } else {
            callback(getCompleted())
        }
    }
}

val now get() = TimeSource.Monotonic.markNow()
