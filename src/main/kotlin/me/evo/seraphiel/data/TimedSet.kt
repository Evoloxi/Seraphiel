package me.evo.seraphiel.data

import me.evo.seraphiel.extension.now
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

class TimedSet<J : Any>(val maxSize: Int = 100, private val timeToLive: Duration = 5.minutes) : Iterable<J> {
    private val set = ConcurrentHashMap<J, TimeSource.Monotonic.ValueTimeMark>()

    fun add(value: J) {
        set[value] = now + timeToLive
        if (set.size > maxSize) set.remove(set.entries.iterator().next().key)
    }

    operator fun contains(value: J): Boolean {
        return (set[value]?.hasNotPassedNow() ?: false).also { if (!it) set.remove(value) }
    }

    fun addAll(value: Set<J>) {
        value.forEach { add(it) }
    }

    fun removeAll(value: Set<J>) {
        value.forEach { set.remove(it) }
    }

    operator fun plusAssign(value: Set<J>) {
        addAll(value)
    }

    operator fun minusAssign(value: Set<J>) {
        removeAll(value)
    }

    operator fun minusAssign(value: J) {
        set.remove(value)
    }

    fun clear() = set.clear()

    val size: Int get() = set.size

    override fun iterator(): Iterator<J> = set.keys.iterator()

    override fun toString(): String {
        return set.keys.joinToString(", ", "[", "]")
    }

    operator fun plusAssign(it: J) {
        add(it)
    }
}