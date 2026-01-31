package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "type", "nameId", "ingredient"
)
class RecipeElementSchema {
    @get:JsonProperty("type")
    @set:JsonProperty("type")
    @JsonProperty("type")
    var type: JsonNode? = null

    @get:JsonProperty("nameId")
    @set:JsonProperty("nameId")
    @JsonProperty("nameId")
    @JsonPropertyDescription("Opaque system-specific identifier, without prefix.")
    var nameId: String? = null
    
    @get:JsonProperty("ingredient")
    @set:JsonProperty("ingredient")
    @JsonProperty("ingredient")
    @JsonPropertyDescription("A file path, delimited by forward slashes.")
    var ingredient: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecipeElementSchema) return false

        if (type != other.type) return false
        if (nameId != other.nameId) return false
        if (ingredient != other.ingredient) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (nameId?.hashCode() ?: 0)
        result = 31 * result + (ingredient?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RecipeElementSchema(type=$type, nameId=$nameId, ingredient=$ingredient)"
    }
}
