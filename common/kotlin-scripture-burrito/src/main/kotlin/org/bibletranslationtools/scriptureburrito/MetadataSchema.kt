package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "meta",
    "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DerivedMetadataSchema::class, name = "derived"),
    JsonSubTypes.Type(value = SourceMetadataSchema::class, name = "source"),
    JsonSubTypes.Type(value = TemplateMetadataSchema::class, name = "template")
)
abstract class MetadataSchema(
    @get:JsonProperty("format")
    @set:JsonProperty("format")
    @JsonProperty("format")
    var format: Format,

    @get:JsonProperty("meta")
    @set:JsonProperty("meta")
    @JsonProperty("meta")
    open var meta: Meta,

    @get:JsonProperty("copyright")
    @set:JsonProperty("copyright")
    @JsonProperty("copyright")
    @JsonPropertyDescription("Describes the copyright holders and license terms of the burrito.")
    var copyright: CopyrightSchema,

    @get:JsonProperty("idAuthorities")
    @set:JsonProperty("idAuthorities")
    @JsonProperty("idAuthorities")
    @JsonPropertyDescription("Declares one or more identity authorities which may later be referred to using identifier prefixes.")
    var idAuthorities: IdAuthoritiesSchema? = null,

    @get:JsonProperty("identification")
    @set:JsonProperty("identification")
    @JsonProperty("identification")
    @JsonPropertyDescription("Identification section.")
    var identification: IdentificationSchema? = null,

    @get:JsonProperty("confidential")
    @set:JsonProperty("confidential")
    @JsonProperty("confidential")
    @JsonPropertyDescription("a true value indicates that the project should not be publicly known and that the identity of project members needs to be kept confidential.")
    var confidential: Boolean? = null,

    @get:JsonProperty("type")
    @set:JsonProperty("type")
    @JsonProperty("type")
    @JsonPropertyDescription("Contains properties describing the burrito flavor type.")
    open var type: TypeSchema? = null,

    @get:JsonProperty("relationships")
    @set:JsonProperty("relationships")
    @JsonProperty("relationships")
    @JsonPropertyDescription("Describes a relationship to another burrito that may be obtained from an indicated server.")
    var relationships: MutableList<RelationshipSchema> = ArrayList(),

    @get:JsonProperty("languages")
    @set:JsonProperty("languages")
    @JsonProperty("languages")
    @JsonPropertyDescription("A list of all the languages of the contents of this burrito.")
    var languages: Languages = Languages(),

    @get:JsonProperty("targetAreas")
    @set:JsonProperty("targetAreas")
    @JsonProperty("targetAreas")
    @JsonPropertyDescription("A list of areas of the primary target audience of this burrito.")
    var targetAreas: MutableList<TargetAreaSchema> = ArrayList(),

    @get:JsonProperty("agencies")
    @set:JsonProperty("agencies")
    @JsonProperty("agencies")
    @JsonPropertyDescription("A list of agencies involved with the contents of the burrito or the work it is derived from.")
    var agencies: MutableList<AgencySchema> = ArrayList(),

    @get:JsonProperty("ingredients")
    @set:JsonProperty("ingredients")
    @JsonProperty("ingredients")
    @JsonPropertyDescription("Describes the various files contained by the burrito, keyed by the canonical forward-slashed pathname of the file.")
    var ingredients: IngredientsSchema = IngredientsSchema(),

    @get:JsonProperty("localizedNames")
    @set:JsonProperty("localizedNames")
    @JsonProperty("localizedNames")
    @JsonPropertyDescription("Contains localized names for books, etc.")
    var localizedNames: LocalizedNamesSchema = LocalizedNamesSchema()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetadataSchema) return false

        if (format != other.format) return false
        if (meta != other.meta) return false
        if (copyright != other.copyright) return false
        if (idAuthorities != other.idAuthorities) return false
        if (identification != other.identification) return false
        if (confidential != other.confidential) return false
        if (type != other.type) return false
        if (relationships != other.relationships) return false
        if (languages != other.languages) return false
        if (targetAreas != other.targetAreas) return false
        if (agencies != other.agencies) return false
        if (ingredients != other.ingredients) return false
        if (localizedNames != other.localizedNames) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + meta.hashCode()
        result = 31 * result + copyright.hashCode()
        result = 31 * result + (idAuthorities?.hashCode() ?: 0)
        result = 31 * result + (identification?.hashCode() ?: 0)
        result = 31 * result + (confidential?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + relationships.hashCode()
        result = 31 * result + languages.hashCode()
        result = 31 * result + targetAreas.hashCode()
        result = 31 * result + agencies.hashCode()
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + localizedNames.hashCode()
        return result
    }

    override fun toString(): String {
        return "MetadataSchema(format=$format, meta=$meta, copyright=$copyright, idAuthorities=$idAuthorities, identification=$identification, confidential=$confidential, type=$type, relationships=$relationships, languages=$languages, targetAreas=$targetAreas, agencies=$agencies, ingredients=$ingredients, localizedNames=$localizedNames)"
    }
}