package org.bibletranslationtools.scriptureburrito.flavor.scripture.audio

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.bibletranslationtools.scriptureburrito.flavor.FlavorSchema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(
    "name", "conventions"
)
class AudioFlavorSchema() : FlavorSchema() {

    @JsonProperty("conventions")
    private var conventions: AudioConventions? = null

    @JsonProperty("conventions")
    fun getAudioConventions(): AudioConventions? {
        return conventions
    }

    @JsonProperty("conventions")
    fun setAudioConventions(conventions: AudioConventions?) {
        this.conventions = conventions
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioFlavorSchema) return false
        if (!super.equals(other)) return false

        if (conventions != other.conventions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (conventions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AudioFlavorSchema(name=audioTranslation, conventions=$conventions)"
    }
}
