package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode
import org.bibletranslationtools.scriptureburrito.flavor.FlavorSchema

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "name",
    "isContracted",
    "processor",
    "hyphenationDictionary",
    "numberSign",
    "continuousPoetry",
    "content",
    "page",
    "conventions"
)
class EmbossedBrailleScriptureSchema: FlavorSchema() {

    @get:JsonProperty("isContracted")
    @set:JsonProperty("isContracted")
    @JsonProperty("isContracted")
    var isContracted: Boolean? = null

    @JsonProperty("processor")
    private var processor: Processor? = null

    @JsonProperty("hyphenationDictionary")
    private var hyphenationDictionary: HyphenationDictionary? = null

    @JsonProperty("numberSign")
    private var numberSign: NumberSign? = null

    @JsonProperty("continuousPoetry")
    private var continuousPoetry: ContinuousPoetry? = null

    @JsonProperty("content")
    private var content: Content? = null

    @JsonProperty("page")
    private var page: Page? = null

    @JsonProperty("conventions")
    private var conventions: JsonNode? = null

    @JsonProperty("processor")
    fun getProcessor(): Processor? {
        return processor
    }

    @JsonProperty("processor")
    fun setProcessor(processor: Processor?) {
        this.processor = processor
    }

    @JsonProperty("hyphenationDictionary")
    fun getHyphenationDictionary(): HyphenationDictionary? {
        return hyphenationDictionary
    }

    @JsonProperty("hyphenationDictionary")
    fun setHyphenationDictionary(hyphenationDictionary: HyphenationDictionary?) {
        this.hyphenationDictionary = hyphenationDictionary
    }

    @JsonProperty("numberSign")
    fun getNumberSign(): NumberSign? {
        return numberSign
    }

    @JsonProperty("numberSign")
    fun setNumberSign(numberSign: NumberSign?) {
        this.numberSign = numberSign
    }

    @JsonProperty("continuousPoetry")
    fun getContinuousPoetry(): ContinuousPoetry? {
        return continuousPoetry
    }

    @JsonProperty("continuousPoetry")
    fun setContinuousPoetry(continuousPoetry: ContinuousPoetry?) {
        this.continuousPoetry = continuousPoetry
    }

    @JsonProperty("content")
    fun getContent(): Content? {
        return content
    }

    @JsonProperty("content")
    fun setContent(content: Content?) {
        this.content = content
    }

    @JsonProperty("page")
    fun getPage(): Page? {
        return page
    }

    @JsonProperty("page")
    fun setPage(page: Page?) {
        this.page = page
    }

    @JsonProperty("conventions")
    fun getConventions(): JsonNode? {
        return conventions
    }

    @JsonProperty("conventions")
    fun setConventions(conventions: JsonNode) {
        this.conventions = conventions
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmbossedBrailleScriptureSchema) return false
        if (!super.equals(other)) return false

        if (isContracted != other.isContracted) return false
        if (processor != other.processor) return false
        if (hyphenationDictionary != other.hyphenationDictionary) return false
        if (numberSign != other.numberSign) return false
        if (continuousPoetry != other.continuousPoetry) return false
        if (content != other.content) return false
        if (page != other.page) return false
        if (conventions != other.conventions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (isContracted?.hashCode() ?: 0)
        result = 31 * result + (processor?.hashCode() ?: 0)
        result = 31 * result + (hyphenationDictionary?.hashCode() ?: 0)
        result = 31 * result + (numberSign?.hashCode() ?: 0)
        result = 31 * result + (continuousPoetry?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (page?.hashCode() ?: 0)
        result = 31 * result + (conventions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "EmbossedBrailleScriptureSchema(isContracted=$isContracted, processor=$processor, hyphenationDictionary=$hyphenationDictionary, numberSign=$numberSign, continuousPoetry=$continuousPoetry, content=$content, page=$page, conventions=$conventions)"
    }
}
