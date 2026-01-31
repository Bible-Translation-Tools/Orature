package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "src", "name"
)
class HyphenationDictionary {

    @get:JsonProperty("src")
    @set:JsonProperty("src")
    @JsonProperty("src")
    var src: String? = null

    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    var name: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HyphenationDictionary) return false

        if (src != other.src) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = src?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "HyphenationDictionary(src=$src, name=$name)"
    }
}
