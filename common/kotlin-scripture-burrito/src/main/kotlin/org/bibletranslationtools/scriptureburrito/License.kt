package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "url", "ingredient"
)
class License {
    
    @get:JsonProperty("url")
    @set:JsonProperty("url")
    @JsonProperty("url")
    @JsonPropertyDescription("A valid **Uniform Resource Locator**.")
    var url: String? = null

    @get:JsonProperty("ingredient")
    @set:JsonProperty("ingredient")
    @JsonProperty("ingredient")
    @JsonPropertyDescription("A file path, delimited by forward slashes.")
    var ingredient: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as License

        if (url != other.url) return false
        if (ingredient != other.ingredient) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url?.hashCode() ?: 0
        result = 31 * result + (ingredient?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "License(url=$url, ingredient=$ingredient)"
    }
}
