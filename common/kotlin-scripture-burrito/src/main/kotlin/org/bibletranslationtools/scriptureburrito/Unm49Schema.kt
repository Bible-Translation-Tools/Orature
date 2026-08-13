package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue


enum class Unm49Schema(private val value: String) {
    _001("001"),
    _002("002"),
    _003("003"),
    _005("005"),
    _009("009"),
    _011("011"),
    _013("013"),
    _014("014"),
    _015("015"),
    _017("017"),
    _018("018"),
    _019("019"),
    _021("021"),
    _024("024"),
    _029("029"),
    _030("030"),
    _034("034"),
    _035("035"),
    _039("039"),
    _053("053"),
    _054("054"),
    _057("057"),
    _061("061"),
    _142("142"),
    _143("143"),
    _145("145"),
    _150("150"),
    _151("151"),
    _154("154"),
    _155("155"),
    _202("202"),
    _419("419"),
    _496("496"),
    _554("554"),
    _591("591"),
    _756("756"),
    _830("830");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS: MutableMap<String, Unm49Schema> = HashMap()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Unm49Schema {
            val constant = CONSTANTS[value]
            requireNotNull(constant) { value }
            return constant
        }
    }
}
