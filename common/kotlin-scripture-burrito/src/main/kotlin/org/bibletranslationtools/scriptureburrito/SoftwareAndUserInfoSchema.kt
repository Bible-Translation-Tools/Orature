package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "softwareName", "softwareVersion", "userId", "userName"
)
class SoftwareAndUserInfoSchema {

    @get:JsonProperty("softwareName")
    @set:JsonProperty("softwareName")
    @JsonProperty("softwareName")
    @JsonPropertyDescription("The name of the program used.")
    var softwareName: String? = null

    @get:JsonProperty("softwareVersion")
    @set:JsonProperty("softwareVersion")
    @JsonProperty("softwareVersion")
    @JsonPropertyDescription("The version of the program used.")
    var softwareVersion: String? = null

    @get:JsonProperty("userId")
    @set:JsonProperty("userId")
    @JsonProperty("userId")
    @JsonPropertyDescription("Opaque system-specific identifier, prefixed with the name of the system as declared in idAuthorities.")
    var userId: String? = null
    
    @get:JsonProperty("userName")
    @set:JsonProperty("userName")
    @JsonProperty("userName")
    @JsonPropertyDescription("The user's full name, if known.")
    var userName: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SoftwareAndUserInfoSchema) return false

        if (softwareName != other.softwareName) return false
        if (softwareVersion != other.softwareVersion) return false
        if (userId != other.userId) return false
        if (userName != other.userName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = softwareName?.hashCode() ?: 0
        result = 31 * result + (softwareVersion?.hashCode() ?: 0)
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (userName?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SoftwareAndUserInfoSchema(softwareName=$softwareName, softwareVersion=$softwareVersion, userId=$userId, userName=$userName)"
    }
}
