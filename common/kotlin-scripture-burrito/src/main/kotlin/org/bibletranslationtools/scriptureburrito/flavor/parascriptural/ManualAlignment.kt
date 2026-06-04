package org.bibletranslationtools.scriptureburrito.flavor.parascriptural

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "user", "references"
)
class ManualAlignment {

    @get:JsonProperty("user")
    @set:JsonProperty("user")
    @JsonProperty("user")
    var user: String? = null

    @JsonProperty("references")
    private var references: MutableList<Reference>? = ArrayList<Reference>()

    @JsonProperty("references")
    fun getReferences(): MutableList<Reference>? {
        return references
    }

    @JsonProperty("references")
    fun setReferences(references: MutableList<Reference>?) {
        this.references = references
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManualAlignment) return false

        if (user != other.user) return false
        if (references != other.references) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user?.hashCode() ?: 0
        result = 31 * result + (references?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ManualAlignment(user=$user, references=$references)"
    }


}
