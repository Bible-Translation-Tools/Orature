package org.wycliffeassociates.otter.jvm.utils

import java.util.EnumMap

/**
 * Returns the value associated with the key, or throws an IllegalStateException if the returned value is null
 */
fun <K: Enum<K>, V> EnumMap<K, V>.getNotNull(key: K?): V = this[key]
    ?: throw IllegalStateException("Key \"$key\" returned null value in ${this::class.simpleName}")
