package org.bibletranslationtools.scriptureburrito.flavor.scripture.print

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "top", "bottom", "inside", "outside"
)
class EdgeSpace {
    @get:JsonProperty("top")
    @set:JsonProperty("top")
    @JsonProperty("top")
    var top: String? = null

    @get:JsonProperty("bottom")
    @set:JsonProperty("bottom")
    @JsonProperty("bottom")
    var bottom: String? = null

    @get:JsonProperty("inside")
    @set:JsonProperty("inside")
    @JsonProperty("inside")
    var inside: String? = null

    @get:JsonProperty("outside")
    @set:JsonProperty("outside")
    @JsonProperty("outside")
    var outside: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EdgeSpace) return false

        if (top != other.top) return false
        if (bottom != other.bottom) return false
        if (inside != other.inside) return false
        if (outside != other.outside) return false

        return true
    }

    override fun hashCode(): Int {
        var result = top?.hashCode() ?: 0
        result = 31 * result + (bottom?.hashCode() ?: 0)
        result = 31 * result + (inside?.hashCode() ?: 0)
        result = 31 * result + (outside?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "EdgeSpace(top=$top, bottom=$bottom, inside=$inside, outside=$outside)"
    }
}
