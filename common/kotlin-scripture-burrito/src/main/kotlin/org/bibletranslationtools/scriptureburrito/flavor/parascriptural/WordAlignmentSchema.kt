package org.bibletranslationtools.scriptureburrito.flavor.parascriptural

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name", "autoAlignerVersion", "stopWords", "stemmer", "manualAlignment"
)
class WordAlignmentSchema {

    @get:JsonProperty("name")
    @set:JsonProperty("name")
    @JsonProperty("name")
    var name: JsonNode? = null

    @get:JsonProperty("autoAlignerVersion")
    @set:JsonProperty("autoAlignerVersion")
    @JsonProperty("autoAlignerVersion")
    var autoAlignerVersion: String? = null

    @get:JsonProperty("stopWords")
    @set:JsonProperty("stopWords")
    @JsonProperty("stopWords")
    var stopWords: Boolean? = null

    @JsonProperty("stemmer")
    private var stemmer: Stemmer? = null

    @JsonProperty("manualAlignment")
    private var manualAlignment: ManualAlignment? = null

    @JsonProperty("stemmer")
    fun getStemmer(): Stemmer? {
        return stemmer
    }

    @JsonProperty("stemmer")
    fun setStemmer(stemmer: Stemmer?) {
        this.stemmer = stemmer
    }

    @JsonProperty("manualAlignment")
    fun getManualAlignment(): ManualAlignment? {
        return manualAlignment
    }

    @JsonProperty("manualAlignment")
    fun setManualAlignment(manualAlignment: ManualAlignment?) {
        this.manualAlignment = manualAlignment
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WordAlignmentSchema) return false

        if (name != other.name) return false
        if (autoAlignerVersion != other.autoAlignerVersion) return false
        if (stopWords != other.stopWords) return false
        if (stemmer != other.stemmer) return false
        if (manualAlignment != other.manualAlignment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (autoAlignerVersion?.hashCode() ?: 0)
        result = 31 * result + (stopWords?.hashCode() ?: 0)
        result = 31 * result + (stemmer?.hashCode() ?: 0)
        result = 31 * result + (manualAlignment?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "WordAlignmentSchema(name=$name, autoAlignerVersion=$autoAlignerVersion, stopWords=$stopWords, stemmer=$stemmer, manualAlignment=$manualAlignment)"
    }
}
