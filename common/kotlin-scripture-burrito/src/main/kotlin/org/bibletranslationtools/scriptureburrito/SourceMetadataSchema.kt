package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.*
import org.bibletranslationtools.scriptureburrito.Format
import javax.xml.transform.Source


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "format",
    "meta",
    "idAuthorities",
    "identification",
    "confidential",
    "type",
    "relationships",
    "languages",
    "targetAreas",
    "agencies",
    "copyright",
    "ingredients",
    "localizedNames",
    "progress"
)
class SourceMetadataSchema(
    @JsonProperty("format")
    format: Format,

    @JsonProperty("meta")
    meta: SourceMetaSchema,

    @JsonProperty("idAuthorities")
    @JsonPropertyDescription("Declares one or more identity authorities which may later be referred to using identifier prefixes.")
    idAuthorities: IdAuthoritiesSchema? = null,

    @JsonProperty("identification")
    @JsonPropertyDescription("Identification section.")
    identification: IdentificationSchema? = null,

    @JsonProperty("confidential")
    @JsonPropertyDescription("a true value indicates that the project should not be publicly known and that the identity of project members needs to be kept confidential.")
    confidential: Boolean? = null,

    @JsonProperty("type")
    @JsonPropertyDescription("Contains properties describing the burrito flavor type.")
    type: TypeSchema,

    @JsonProperty("copyright")
    copyright: CopyrightSchema,

    @JsonProperty("relationships")
    @JsonPropertyDescription("Describes a relationship to another burrito that may be obtained from an indicated server.")
    relationships: MutableList<RelationshipSchema> = ArrayList(),

    @JsonProperty("languages")
    @JsonPropertyDescription("A list of all the languages of the contents of this burrito.")
    languages: Languages = Languages(),

    @JsonProperty("targetAreas")
    @JsonPropertyDescription("A list of areas of the primary target audience of this burrito.")
    targetAreas: MutableList<TargetAreaSchema> = ArrayList(),

    @JsonProperty("agencies")
    @JsonPropertyDescription("A list of agencies involved with the contents of the burrito or the work it is derived from.")
    agencies: MutableList<AgencySchema> = ArrayList(),

    @JsonProperty("ingredients")
    @JsonPropertyDescription("Describes the various files contained by the burrito, keyed by the canonical forward-slashed pathname of the file.")
    ingredients: IngredientsSchema = IngredientsSchema(),

    @JsonProperty("localizedNames")
    @JsonPropertyDescription("Contains localized names for books, etc.")
    localizedNames: LocalizedNamesSchema = LocalizedNamesSchema()
) : MetadataSchema(
    format,
    meta,
    copyright,
    idAuthorities,
    identification,
    confidential,
    type,
    relationships,
    languages,
    targetAreas,
    agencies,
    ingredients,
    localizedNames
) {
    @get:JsonProperty("progress")
    @set:JsonProperty("progress")
    @JsonProperty("progress")
    var progress: ProgressSchema? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SourceMetadataSchema) return false
        if (!super.equals(other)) return false

        if (progress != other.progress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (progress?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SourceMetadataSchema(progress=$progress, format=$format, meta=$meta, copyright=$copyright, idAuthorities=$idAuthorities, identification=$identification, confidential=$confidential, type=$type, relationships=$relationships, languages=$languages, targetAreas=$targetAreas, agencies=$agencies, ingredients=$ingredients, localizedNames=$localizedNames)"
    }
}
