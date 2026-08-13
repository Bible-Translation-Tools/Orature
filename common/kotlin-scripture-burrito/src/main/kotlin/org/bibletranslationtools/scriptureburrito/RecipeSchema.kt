package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "idAuthority", "operation", "data"
)
class RecipeSchema(
    @get:JsonProperty("idAuthority")
    @set:JsonProperty("idAuthority")
    @JsonProperty("idAuthority")
    var idAuthority: String,

    @get:JsonProperty("operation")
    @set:JsonProperty("operation")
    @JsonProperty("operation")
    var operation: String,

    @get:JsonProperty("data")
    @set:JsonProperty("data")
    @JsonProperty("data")
    var data: JsonNode
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecipeSchema

        if (idAuthority != other.idAuthority) return false
        if (operation != other.operation) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idAuthority.hashCode()
        result = 31 * result + operation.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }

    override fun toString(): String {
        return "RecipeSchema(idAuthority='$idAuthority', operation='$operation', data=$data)"
    }
}
