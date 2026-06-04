package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "statement", "lang", "mimetype"
)
class ShortStatement(
    @get:JsonProperty("statement")
    @set:JsonProperty("statement")
    @JsonProperty("statement")
    var statement: String,

    @get:JsonProperty("lang")
    @set:JsonProperty("lang")
    @JsonProperty("lang")
    @JsonPropertyDescription("A valid IETF language tag as specified by BCP 47.")
    var lang: String? = null,

    @get:JsonProperty("mimetype")
    @set:JsonProperty("mimetype")
    @JsonProperty("mimetype")
    @JsonPropertyDescription("An IANA media type (also known as MIME type)")
    var mimetype: String? = null
) {
    override fun toString(): String {
        return "ShortStatement(statement=$statement, lang=$lang, mimetype=$mimetype)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortStatement

        if (statement != other.statement) return false
        if (lang != other.lang) return false
        if (mimetype != other.mimetype) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statement.hashCode()
        result = 31 * result + (lang?.hashCode() ?: 0)
        result = 31 * result + (mimetype?.hashCode() ?: 0)
        return result
    }
}
