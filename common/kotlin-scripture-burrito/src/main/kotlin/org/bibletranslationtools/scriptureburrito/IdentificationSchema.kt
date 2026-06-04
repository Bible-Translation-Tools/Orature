package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name", "description", "abbreviation", "primary", "upstream"
)
class IdentificationSchema {

    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var name: HashMap<String, String> = hashMapOf()

    @get:JsonProperty("description")
    @set:JsonProperty("description")
    @JsonProperty("description")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var description: HashMap<String, String> = hashMapOf()

    @get:JsonProperty("abbreviation")
    @set:JsonProperty("abbreviation")
    @JsonProperty("abbreviation")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var abbreviation: HashMap<String, String> = hashMapOf()

    @get:JsonProperty("primary")
    @set:JsonProperty("primary")
    @JsonProperty("primary")
    @JsonPropertyDescription("Contains the primary authority and identification information.")
    @JsonIgnore
    var primary: PrimaryIdentification = PrimaryIdentification()

    @get:JsonProperty("upstream")
    @set:JsonProperty("upstream")
    @JsonProperty("upstream")
    @JsonPropertyDescription("Contains the upstream authority and identification information.")
    var upstream: JsonNode? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdentificationSchema

        if (name != other.name) return false
        if (description != other.description) return false
        if (abbreviation != other.abbreviation) return false
        if (primary != other.primary) return false
        if (upstream != other.upstream) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (description.hashCode())
        result = 31 * result + (abbreviation.hashCode())
        result = 31 * result + primary.hashCode()
        result = 31 * result + (upstream?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "IdentificationSchema(name=$name, description=$description, abbreviation=$abbreviation, primary=$primary, upstream=$upstream)"
    }
}