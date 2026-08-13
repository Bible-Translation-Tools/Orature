package org.bibletranslationtools.scriptureburrito

import org.bibletranslationtools.scriptureburrito.MetaVersionSchema
import org.bibletranslationtools.scriptureburrito.NormalizationSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.bibletranslationtools.scriptureburrito.Category
import java.util.*


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "category", "templateName", "dateCreated", "version", "generator", "defaultLocale", "normalization", "comments"
)
class TemplateMetaSchema(
    @JsonProperty("dateCreated")
    dateCreated: Date,

    @JsonProperty("version")
    version: MetaVersionSchema,

    @JsonProperty("generator")
    generator: SoftwareAndUserInfoSchema? = null,

    @JsonProperty("defaultLocale")
    defaultLocale: String,

    @JsonProperty("normalization")
    normalization: NormalizationSchema? = null,

    @JsonProperty("comments")
    comments: MutableList<String> = ArrayList(),

    @get:JsonProperty("templateName")
    @set:JsonProperty("templateName")
    @JsonProperty("templateName")
    @JsonPropertyDescription("A textual string specified in one or multiple languages, indexed by IETF language tag.")
    var templateName: LocalizedText
) : Meta(
    dateCreated,
    version,
    generator,
    defaultLocale,
    normalization,
    comments
) {

    override fun toString(): String {
        return "TemplateMetaSchema(category:template, templateName=$templateName, dateCreated=$dateCreated, version=$version, generator=$generator, defaultLocale='$defaultLocale', normalization=$normalization, comments=$comments)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TemplateMetaSchema) return false
        if (!super.equals(other)) return false

        if (templateName != other.templateName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + templateName.hashCode()
        return result
    }
}