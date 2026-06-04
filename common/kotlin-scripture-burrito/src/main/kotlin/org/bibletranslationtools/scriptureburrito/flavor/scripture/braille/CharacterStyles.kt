package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "callerSymbol"
)
class CharacterStyles {

    @get:JsonProperty("callerSymbol")
    @set:JsonProperty("callerSymbol")
    @JsonProperty("callerSymbol")
    var callerSymbol: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CharacterStyles) return false

        if (callerSymbol != other.callerSymbol) return false

        return true
    }

    override fun hashCode(): Int {
        return callerSymbol?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "CharacterStyles(callerSymbol=$callerSymbol)"
    }
}
