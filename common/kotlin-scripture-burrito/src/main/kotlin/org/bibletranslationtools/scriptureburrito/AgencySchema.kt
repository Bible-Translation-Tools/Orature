package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder


class Agencies: ArrayList<AgencySchema>()

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "id", "name", "abbr", "url", "roles"
)
class AgencySchema(
    @get:JsonProperty("id")
    @set:JsonProperty("id")
    @JsonProperty("id")
    @JsonPropertyDescription("Opaque system-specific identifier, prefixed with the name of the system as declared in idAuthorities.")
    var id: String,

    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var name: LocalizedText,

    @get:JsonProperty("roles")
    @set:JsonProperty("roles")
    @JsonProperty("roles")
    @JsonPropertyDescription("A list of roles indicating in which respects this agency was involved with the production of this burrito.")
    var roles: MutableList<Role> = ArrayList()
) {
    @get:JsonProperty("abbr")
    @set:JsonProperty("abbr")
    @JsonProperty("abbr")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var abbr: LocalizedText? = null

    @get:JsonProperty("url")
    @set:JsonProperty("url")
    @JsonProperty("url")
    @JsonPropertyDescription("A valid **Uniform Resource Locator**.")
    var url: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AgencySchema

        if (id != other.id) return false
        if (name != other.name) return false
        if (roles != other.roles) return false
        if (abbr != other.abbr) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + roles.hashCode()
        result = 31 * result + (abbr?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}
