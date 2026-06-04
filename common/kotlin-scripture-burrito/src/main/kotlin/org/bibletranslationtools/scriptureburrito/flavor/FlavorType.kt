package org.bibletranslationtools.scriptureburrito.flavor

import com.fasterxml.jackson.annotation.*
import org.bibletranslationtools.scriptureburrito.Flavor
import org.bibletranslationtools.scriptureburrito.ScopeSchema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name",
    "flavor",
    "currentScope"
)
class FlavorType(
    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    var name: Flavor,

    @get:JsonProperty("flavor")
    @set:JsonProperty("flavor")
    @JsonProperty("flavor")
    var flavor: FlavorSchema,

    @get:JsonProperty("currentScope")
    @set:JsonProperty("currentScope")
    @JsonProperty("currentScope")
    var currentScope: ScopeSchema
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlavorType) return false

        if (name != other.name) return false
        if (flavor != other.flavor) return false
        if (currentScope != other.currentScope) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (flavor?.hashCode() ?: 0)
        result = 31 * result + (currentScope?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "FlavorType(name=$name, flavor=$flavor, currentScope=$currentScope)"
    }
}