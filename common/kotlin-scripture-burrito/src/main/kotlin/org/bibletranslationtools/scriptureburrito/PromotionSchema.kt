package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "statementPlain", "statementRich"
)
class PromotionSchema {

    @get:JsonProperty("statementPlain")
    @set:JsonProperty("statementPlain")
    @JsonProperty("statementPlain")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var statementPlain: LocalizedText? = null
    
    @get:JsonProperty("statementRich")
    @set:JsonProperty("statementRich")
    @JsonProperty("statementRich")
    @JsonPropertyDescription("A simplified XHTML string specified in one or multiple languages, indexed by IETF language tag.")
    var statementRich: JsonNode? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromotionSchema) return false

        if (statementPlain != other.statementPlain) return false
        if (statementRich != other.statementRich) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statementPlain?.hashCode() ?: 0
        result = 31 * result + (statementRich?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "PromotionSchema(statementPlain=$statementPlain, statementRich=$statementRich)"
    }
}
