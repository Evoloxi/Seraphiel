package me.evo.seraphiel.data

import me.evo.seraphiel.Seraphiel
import me.evo.seraphiel.now
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

class TimedCache<K : Any, V>(
    private val maxSize: Int = 100,
    private val timeToLive: Duration = 5.minutes
) : Iterable<Pair<K, V>> {
    val keys: Set<K>
        get() = cache.keys
    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    class CacheEntry<V>(val value: V, val expiryTime: TimeSource.Monotonic.ValueTimeMark)

    fun put(key: K, value: V) {
        val expiryTime = now + timeToLive

        val existing = cache[key]
        if (existing != null && existing.expiryTime.hasPassedNow()) {
            cache.remove(key)
        }

        cache[key] = CacheEntry(value, expiryTime)
        if (size > maxSize) cache.remove(cache.entries.iterator().next().key)
    }

    operator fun get(key: K): V? {
        return cache[key]?.let {
            if (it.expiryTime.hasNotPassedNow()) it.value
            else {
                cache.remove(key)
                null
            }
        }
    }

    operator fun set(it: K, value: V) {
        cache[it] = CacheEntry(value, now + timeToLive)
        if (size > maxSize) cache.remove(cache.entries.iterator().next().key)
    }

    operator fun contains(id: K): Boolean {
        return (cache[id]?.expiryTime?.hasNotPassedNow() ?: false).also { if (!it) cache.remove(id) }
    }

    operator fun minusAssign(id: K) {
        cache.remove(id)
    }

    fun remove(id: K) = cache.remove(id)

    val size: Int
        get() = cache.size

    fun clear() = cache.clear()

    override fun iterator(): Iterator<Pair<K, V>> {
        return cache.iterator().asSequence().map { it.key to it.value.value }.iterator()
    }
}