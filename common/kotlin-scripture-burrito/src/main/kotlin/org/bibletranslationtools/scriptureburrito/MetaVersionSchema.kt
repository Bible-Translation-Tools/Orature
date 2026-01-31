package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MetaVersionSchema(private val value: String) {
    _1_0_0("1.0.0");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, MetaVersionSchema> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): MetaVersionSchema {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}