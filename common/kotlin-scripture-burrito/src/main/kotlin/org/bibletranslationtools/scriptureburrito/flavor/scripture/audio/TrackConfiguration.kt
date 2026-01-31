package org.bibletranslationtools.scriptureburrito.flavor.scripture.audio

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class TrackConfiguration(private val value: String) {
    MONO("1/0 (Mono)"),
    DUAL_MONO("Dual mono"),
    STEREO("2/0 (Stereo)"),
    SURROUND("5.1 Surround");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, TrackConfiguration> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): TrackConfiguration {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}