package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.*
import org.bibletranslationtools.scriptureburrito.Category
import org.bibletranslationtools.scriptureburrito.MetaVersionSchema
import org.bibletranslationtools.scriptureburrito.NormalizationSchema
import java.util.*


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "category", "dateCreated", "version", "generator", "defaultLocale", "normalization", "comments"
)
class DerivedMetaSchema(
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
    comments: MutableList<String> = ArrayList()
) : Meta(
    dateCreated,
    version,
    generator,
    defaultLocale,
    normalization,
    comments
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DerivedMetaSchema) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun toString(): String {
        return "DerivedMetaSchema(category:derived, dateCreated=$dateCreated, version=$version, generator=$generator, defaultLocale='$defaultLocale', normalization=$normalization, comments=$comments)"
    }
}
