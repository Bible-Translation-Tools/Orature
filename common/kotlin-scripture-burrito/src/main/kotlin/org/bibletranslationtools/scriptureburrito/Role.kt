package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Role(private val value: String) {
    RIGHTS_ADMIN("rightsAdmin"),
    RIGHTS_HOLDER("rightsHolder"),
    CONTENT("content"),
    PUBLICATION("publication"),
    MANAGEMENT("management"),
    FINANCE("finance"),
    QA("qa");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, Role> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Role {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}
