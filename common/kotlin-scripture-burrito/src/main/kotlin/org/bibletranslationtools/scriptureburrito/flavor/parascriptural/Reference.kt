package org.bibletranslationtools.scriptureburrito.flavor.parascriptural

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "start", "end"
)
class Reference {
    @get:JsonProperty("start")
    @set:JsonProperty("start")
    @JsonProperty("start")
    var start: Double? = null

    @get:JsonProperty("end")
    @set:JsonProperty("end")
    @JsonProperty("end")
    var end: Double? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Reference) return false

        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start?.hashCode() ?: 0
        result = 31 * result + (end?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Reference(start=$start, end=$end)"
    }
}
