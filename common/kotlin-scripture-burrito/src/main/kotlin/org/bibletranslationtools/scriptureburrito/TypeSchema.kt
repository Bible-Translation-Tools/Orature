package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.bibletranslationtools.scriptureburrito.flavor.FlavorType


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "flavorType"
)
class TypeSchema(
    @get:JsonProperty("flavorType")
    @set:JsonProperty("flavorType")
    @JsonProperty("flavorType")
    var flavorType: FlavorType
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeSchema

        return flavorType == other.flavorType
    }

    override fun hashCode(): Int {
        return flavorType.hashCode()
    }

    override fun toString(): String {
        return "TypeSchema(${flavorType})"
    }
}
