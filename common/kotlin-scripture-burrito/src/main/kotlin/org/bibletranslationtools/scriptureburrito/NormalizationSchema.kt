package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.HashMap

enum class NormalizationSchema(private val value: String) {
    NFC("NFC"),
    NFD("NFD"),
    NFKC("NFKC"),
    NFKD("NFKD");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, NormalizationSchema> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): NormalizationSchema {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}