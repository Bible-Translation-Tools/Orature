package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "character", "useInMargin"
)
class NumberSign {

    @get:JsonProperty("character")
    @set:JsonProperty("character")
    @JsonProperty("character")
    var character: String? = null

    @get:JsonProperty("useInMargin")
    @set:JsonProperty("useInMargin")
    @JsonProperty("useInMargin")
    var useInMargin: Boolean? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumberSign) return false

        if (character != other.character) return false
        if (useInMargin != other.useInMargin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = character?.hashCode() ?: 0
        result = 31 * result + (useInMargin?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "NumberSign(character=$character, useInMargin=$useInMargin)"
    }
}
