package org.wycliffeassociates.otter.common.collections

class MultiMap<K, V> : HashMap<K, MutableSet<V>>() {
    operator fun set(k: K, v: V) = put(k, v)

    fun put(key: K, value: V) = getOrPut(key) { mutableSetOf() }.add(value)

    fun kvSequence() = asSequence().flatMap { (k, vs) -> vs.asSequence().map { v -> Pair(k, v) } }
}
