package org.bibletranslationtools.scriptureburrito.flavor.gloss

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name"
)
class TextStoriesSchema {
    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    var name: JsonNode? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextStoriesSchema) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "TextStoriesSchema(name=$name)"
    }
}
