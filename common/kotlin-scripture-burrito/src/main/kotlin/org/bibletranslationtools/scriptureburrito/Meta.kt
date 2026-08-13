package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import org.bibletranslationtools.scriptureburrito.Category
import org.bibletranslationtools.scriptureburrito.MetaVersionSchema
import org.bibletranslationtools.scriptureburrito.NormalizationSchema
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "category"
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "category")
@JsonSubTypes(
    Type(value = DerivedMetaSchema::class, name = "derived"),
    Type(value = SourceMetaSchema::class, name = "source"),
    Type(value = TemplateMetaSchema::class, name = "template")
)
abstract class Meta(
    @get:JsonProperty("dateCreated")
    @set:JsonProperty("dateCreated")
    @JsonProperty("dateCreated")
    var dateCreated: Date,

    @get:JsonProperty("version")
    @set:JsonProperty("version")
    @JsonProperty("version")
    @JsonPropertyDescription("Version of the Scripture Burrito specification this file follows.")
    var version: MetaVersionSchema,

    @get:JsonProperty("generator")
    @set:JsonProperty("generator")
    @JsonProperty("generator")
    var generator: SoftwareAndUserInfoSchema? = null,

    @get:JsonProperty("defaultLocale")
    @set:JsonProperty("defaultLocale")
    @JsonProperty("defaultLocale")
    @JsonPropertyDescription("A valid IETF language tag as specified by BCP 47.")
    var defaultLocale: String,

    @get:JsonProperty("normalization")
    @set:JsonProperty("normalization")
    @JsonProperty("normalization")
    @JsonPropertyDescription("Unicode normalization options. This applies to both ingredients and metadata.")
    var normalization: NormalizationSchema? = null,

    @get:JsonProperty("comments")
    @set:JsonProperty("comments")
    @JsonProperty("comments")
    @JsonPropertyDescription("Arbitrary text strings attached by users with no effect on the interpretation of the Scripture Burrito.")
    var comments: MutableList<String> = ArrayList()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Meta) return false

        if (dateCreated != other.dateCreated) return false
        if (version != other.version) return false
        if (generator != other.generator) return false
        if (defaultLocale != other.defaultLocale) return false
        if (normalization != other.normalization) return false
        if (comments != other.comments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dateCreated.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + (generator?.hashCode() ?: 0)
        result = 31 * result + defaultLocale.hashCode()
        result = 31 * result + (normalization?.hashCode() ?: 0)
        result = 31 * result + comments.hashCode()
        return result
    }

    override fun toString(): String {
        return "Meta(dateCreated=$dateCreated, version=$version, generator=$generator, defaultLocale='$defaultLocale', normalization=$normalization, comments=$comments)"
    }
}
