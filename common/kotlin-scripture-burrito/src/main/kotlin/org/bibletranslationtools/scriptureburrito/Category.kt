package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Category(private val value: String) {
    SOURCE("source"),
    DERIVED("derived"),
    TEMPLATE("template");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, Category> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Category {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}