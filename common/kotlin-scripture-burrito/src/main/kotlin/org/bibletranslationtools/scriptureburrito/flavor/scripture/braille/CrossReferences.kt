package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "emphasizedWord", "emphasizedPassageStart", "emphasizedPassageEnd"
)
class CrossReferences {
    @get:JsonProperty("emphasizedWord")
    @set:JsonProperty("emphasizedWord")
    @JsonProperty("emphasizedWord")
    var emphasizedWord: String? = null

    @get:JsonProperty("emphasizedPassageStart")
    @set:JsonProperty("emphasizedPassageStart")
    @JsonProperty("emphasizedPassageStart")
    var emphasizedPassageStart: String? = null

    @get:JsonProperty("emphasizedPassageEnd")
    @set:JsonProperty("emphasizedPassageEnd")
    @JsonProperty("emphasizedPassageEnd")
    var emphasizedPassageEnd: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CrossReferences) return false

        if (emphasizedWord != other.emphasizedWord) return false
        if (emphasizedPassageStart != other.emphasizedPassageStart) return false
        if (emphasizedPassageEnd != other.emphasizedPassageEnd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = emphasizedWord?.hashCode() ?: 0
        result = 31 * result + (emphasizedPassageStart?.hashCode() ?: 0)
        result = 31 * result + (emphasizedPassageEnd?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CrossReferences(emphasizedWord=$emphasizedWord, emphasizedPassageStart=$emphasizedPassageStart, emphasizedPassageEnd=$emphasizedPassageEnd)"
    }
}
