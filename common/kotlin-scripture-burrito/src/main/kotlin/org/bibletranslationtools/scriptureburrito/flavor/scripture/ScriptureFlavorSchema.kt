package org.bibletranslationtools.scriptureburrito.flavor.scripture

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.bibletranslationtools.scriptureburrito.flavor.FlavorSchema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name",
    "usfmVersion",
    "translationType",
    "audience",
    "projectType"
)
class ScriptureFlavorSchema: FlavorSchema() {
    @get:JsonProperty("usfmVersion")
    @set:JsonProperty("usfmVersion")
    @JsonProperty("usfmVersion")
    var usfmVersion: String? = null

    @get:JsonProperty("translationType")
    @set:JsonProperty("translationType")
    @JsonProperty("translationType")
    var translationType: String? = null

    @get:JsonProperty("audience")
    @set:JsonProperty("audience")
    @JsonProperty("audience")
    var audience: String? = null

    @get:JsonProperty("projectType")
    @set:JsonProperty("projectType")
    @JsonProperty("projectType")
    var projectType: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScriptureFlavorSchema) return false
        if (!super.equals(other)) return false

        if (usfmVersion != other.usfmVersion) return false
        if (translationType != other.translationType) return false
        if (audience != other.audience) return false
        if (projectType != other.projectType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (usfmVersion?.hashCode() ?: 0)
        result = 31 * result + (translationType?.hashCode() ?: 0)
        result = 31 * result + (audience?.hashCode() ?: 0)
        result = 31 * result + (projectType?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ScriptureFlavorSchema(usfmVersion=$usfmVersion, translationType=$translationType, audience=$audience, projectType=$projectType)"
    }
}
