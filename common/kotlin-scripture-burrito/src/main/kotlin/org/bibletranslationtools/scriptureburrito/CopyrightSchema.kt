package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "licenses", "publicDomain", "shortStatements"
)
class CopyrightSchema {

    @get:JsonProperty("licenses")
    @set:JsonProperty("licenses")
    @JsonProperty("licenses")
    @JsonPropertyDescription("The licenses, which state under which terms the burrito may be used, can be specified either as URL or as path to an ingredient.")
    var licenses: MutableList<License>? = ArrayList()
    
    @get:JsonProperty("publicDomain")
    @set:JsonProperty("publicDomain")
    @JsonProperty("publicDomain")
    @JsonPropertyDescription("If set to true, the contents of this burrito are in the public domain.")
    var publicDomain: Boolean? = null
    
    @get:JsonProperty("shortStatements")
    @set:JsonProperty("shortStatements")
    @JsonProperty("shortStatements")
    @JsonPropertyDescription("A short statement to explain the copyright / license information to readers")
    var shortStatements: MutableList<ShortStatement> = ArrayList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CopyrightSchema

        if (licenses != other.licenses) return false
        if (publicDomain != other.publicDomain) return false
        if (shortStatements != other.shortStatements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = licenses?.hashCode() ?: 0
        result = 31 * result + (publicDomain?.hashCode() ?: 0)
        result = 31 * result + shortStatements.hashCode()
        return result
    }

    override fun toString(): String {
        return "CopyrightSchema(licenses=$licenses, publicDomain=$publicDomain, shortStatements=$shortStatements)"
    }
}
