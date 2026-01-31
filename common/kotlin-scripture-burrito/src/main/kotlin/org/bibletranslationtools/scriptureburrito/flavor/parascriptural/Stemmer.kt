package org.bibletranslationtools.scriptureburrito.flavor.parascriptural

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name", "version", "affixes"
)
class Stemmer {
    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    var name: String? = null

    @get:JsonProperty("version")
    @set:JsonProperty("version")
    @JsonProperty("version")
    var version: String? = null

    @get:JsonProperty("affixes")
    @set:JsonProperty("affixes")
    @JsonProperty("affixes")
    var affixes: Boolean? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stemmer) return false

        if (name != other.name) return false
        if (version != other.version) return false
        if (affixes != other.affixes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (affixes?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Stemmer(name=$name, version=$version, affixes=$affixes)"
    }
}
