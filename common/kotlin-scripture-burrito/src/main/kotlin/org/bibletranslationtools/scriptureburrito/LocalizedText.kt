package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.bibletranslationtools.scriptureburrito.Format


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "abbr",
    "short",
    "long"
)
class LocalizedText(
    @get:JsonProperty("short")
    @set:JsonProperty("short")
    @JsonProperty("short")
    var short: HashMap<String, String>
) {
    @get:JsonProperty("abbr")
    @set:JsonProperty("abbr")
    @JsonProperty("abbr")
    var abbr: HashMap<String, String> = HashMap()

    @get:JsonProperty("long")
    @set:JsonProperty("long")
    @JsonProperty("long")
    var long: HashMap<String, String> = HashMap()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalizedText

        if (short != other.short) return false
        if (abbr != other.abbr) return false
        if (long != other.long) return false

        return true
    }

    override fun hashCode(): Int {
        var result = short.hashCode()
        result = 31 * result + abbr.hashCode()
        result = 31 * result + long.hashCode()
        return result
    }

    override fun toString(): String {
        return "LocalizedText(short=$short, abbr=$abbr, long=$long)"
    }
}
