package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "type", "nameId", "content"
)
class RecipeSectionSchema {

    @get:JsonProperty("type")
    @set:JsonProperty("type")
    @JsonProperty("type")
    var type: JsonNode? = null

    @get:JsonProperty("nameId")
    @set:JsonProperty("nameId")
    @JsonProperty("nameId")
    @JsonPropertyDescription("Opaque system-specific identifier, without prefix.")
    var nameId: String? = null

    @get:JsonProperty("content")
    @set:JsonProperty("content")
    @JsonProperty("content")
    var content: MutableList<JsonNode> = ArrayList()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecipeSectionSchema) return false

        if (type != other.type) return false
        if (nameId != other.nameId) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (nameId?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        return result
    }

    override fun toString(): String {
        return "RecipeSectionSchema(type=$type, nameId=$nameId, content=$content)"
    }
}
