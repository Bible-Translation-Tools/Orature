package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.*
import org.bibletranslationtools.scriptureburrito.Format

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
    "promotion",
    "ingredients",
    "localizedNames",
    "recipe"
)
class DerivedMetadataSchema(
    @JsonProperty("format")
    format: org.bibletranslationtools.scriptureburrito.Format,
    @JsonProperty("meta")
    meta: DerivedMetaSchema,

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
    @get:JsonProperty("promotion")
    @set:JsonProperty("promotion")
    @JsonProperty("promotion")
    @JsonPropertyDescription("Contains promotional statements for the burrito.")
    var promotion: PromotionSchema? = null


    @get:JsonProperty("recipe")
    @set:JsonProperty("recipe")
    @JsonProperty("recipe")
    @JsonPropertyDescription("Scripture Burrito recipes.")
    var recipe: MutableList<RecipeSchema>? = ArrayList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DerivedMetadataSchema) return false
        if (!super.equals(other)) return false

        if (promotion != other.promotion) return false
        if (recipe != other.recipe) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (promotion?.hashCode() ?: 0)
        result = 31 * result + (recipe?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "DerivedMetadataSchema(promotion=$promotion, recipe=$recipe, format=$format, meta=$meta, copyright=$copyright, idAuthorities=$idAuthorities, identification=$identification, confidential=$confidential, type=$type, relationships=$relationships, languages=$languages, targetAreas=$targetAreas, agencies=$agencies, ingredients=$ingredients, localizedNames=$localizedNames)"
    }
}
