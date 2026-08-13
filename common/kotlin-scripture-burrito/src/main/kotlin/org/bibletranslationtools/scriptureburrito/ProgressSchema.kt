package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "dateStarted", "dateCompleted"
)
class ProgressSchema {
    @get:JsonProperty("dateStarted")
    @set:JsonProperty("dateStarted")
    @JsonProperty("dateStarted")
    var dateStarted: String? = null

    @get:JsonProperty("dateCompleted")
    @set:JsonProperty("dateCompleted")
    @JsonProperty("dateCompleted")
    var dateCompleted: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProgressSchema) return false

        if (dateStarted != other.dateStarted) return false
        if (dateCompleted != other.dateCompleted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dateStarted?.hashCode() ?: 0
        result = 31 * result + (dateCompleted?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ProgressSchema(dateStarted=$dateStarted, dateCompleted=$dateCompleted)"
    }
}
