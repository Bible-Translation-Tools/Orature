package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "size", "lang", "mimeType", "checksum", "scope", "role"
)
class IngredientSchema {
    
    @get:JsonProperty("size")
    @set:JsonProperty("size")
    @JsonProperty("size")
    @JsonPropertyDescription("The number of bytes that this ingredient takes up on disk.")
    var size: Int? = null
    
    @get:JsonProperty("lang")
    @set:JsonProperty("lang")
    @JsonProperty("lang")
    @JsonPropertyDescription("A valid IETF language tag as specified by BCP 47.")
    var lang: String? = null

    @get:JsonProperty("mimeType")
    @set:JsonProperty("mimeType")
    @JsonProperty("mimeType")
    @JsonPropertyDescription("An IANA media type (also known as MIME type)")
    var mimeType: String? = null

    @get:JsonProperty("checksum")
    @set:JsonProperty("checksum")
    @JsonProperty("checksum")
    var checksum: Checksum? = null

    @get:JsonProperty("scope")
    @set:JsonProperty("scope")
    @JsonProperty("scope")
    @JsonPropertyDescription("Scope specification, used for the whole burrito and for specific ingredients. In both cases it describes the actual content, not future translation goals.")
    var scope: ScopeSchema? = null

    @get:JsonProperty("role")
    @set:JsonProperty("role")
    @JsonProperty("role")
    @JsonPropertyDescription("Roles which may be optionally attached to an ingredient.")
    var role: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IngredientSchema

        if (size != other.size) return false
        if (lang != other.lang) return false
        if (mimeType != other.mimeType) return false
        if (checksum != other.checksum) return false
        if (scope != other.scope) return false
        if (role != other.role) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size ?: 0
        result = 31 * result + (lang?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (checksum?.hashCode() ?: 0)
        result = 31 * result + (scope?.hashCode() ?: 0)
        result = 31 * result + (role?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "IngredientSchema(size=$size, lang=$lang, mimeType=$mimeType, checksum=$checksum, scope=$scope, role=$role)"
    }
}
