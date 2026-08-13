package org.bibletranslationtools.scriptureburrito.flavor.scripture.audio

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Performance(private val value: String) {
    SINGLE_VOICE("singleVoice"),
    MULTIPLE_VOICE("multipleVoice"),
    READING("reading"),
    DRAMA("drama"),
    WITH_MUSIC("withMusic"),
    WITH_EFFECTS("withEffects"),
    WITH_HEADINGS("withHeadings");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, Performance> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Performance {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}
