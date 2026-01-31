package org.bibletranslationtools.scriptureburrito.flavor.scripture.video

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode
import org.bibletranslationtools.scriptureburrito.flavor.FlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Formats

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name", "contentByChapter", "formats", "conventions"
)
class SignLanguageVideoTranslationSchema: FlavorSchema() {

    @get:JsonProperty("contentByChapter")
    @set:JsonProperty("contentByChapter")
    @JsonProperty("contentByChapter")
    var contentByChapter: Boolean? = null

    @JsonProperty("formats")
    private var formats: Formats? = null

    @JsonProperty("conventions")
    private var conventions: JsonNode? = null

    @JsonProperty("formats")
    fun getFormats(): Formats? {
        return formats
    }

    @JsonProperty("formats")
    fun setFormats(formats: Formats?) {
        this.formats = formats
    }

    @JsonProperty("conventions")
    fun getConventions(): JsonNode? {
        return conventions
    }

    @JsonProperty("conventions")
    fun setConventions(conventions: JsonNode?) {
        this.conventions = conventions
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignLanguageVideoTranslationSchema) return false

        if (contentByChapter != other.contentByChapter) return false
        if (formats != other.formats) return false
        if (conventions != other.conventions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + (contentByChapter?.hashCode() ?: 0)
        result = 31 * result + (formats?.hashCode() ?: 0)
        result = 31 * result + (conventions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SignLanguageVideoTranslationSchema(contentByChapter=$contentByChapter, formats=$formats, conventions=$conventions)"
    }
}
