package me.evo.seraphiel.data

import io.netty.util.internal.ConcurrentSet

@Suppress("MemberVisibilityCanBePrivate")
class LimitedSet<J : Any>(private val maxSize: Int = 100) : Iterable<J> {
    private val set = ConcurrentSet<J>()

    fun add(value: J) {
        set.add(value)
        if (set.size > maxSize) set.remove(set.iterator().next())
    }

    operator fun contains(value: J): Boolean = value in set

    fun addAll(value: Set<J>) {
        set.addAll(value)
        if (set.size > maxSize)
            repeat(set.size - maxSize) {
                set.remove(set.iterator().next())
            }
    }

    fun removeAll(value: Set<J>) {
        set.removeAll(value)
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

    override fun iterator(): Iterator<J> = set.iterator()

    override fun toString(): String {
        return set.joinToString(", ", "[", "]")
    }

    operator fun plusAssign(it: J) {
        add(it)
    }

    fun remove(uuid: J) {
        set.remove(uuid)
    }
}

