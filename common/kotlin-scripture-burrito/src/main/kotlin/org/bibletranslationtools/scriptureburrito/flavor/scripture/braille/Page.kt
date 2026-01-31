package org.bibletranslationtools.scriptureburrito.flavor.scripture.braille

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "charsPerLine", "linesPerPage", "defaultMarginWidth", "versoLastLineBlank", "carryLines"
)
class Page {

    @get:JsonProperty("charsPerLine")
    @set:JsonProperty("charsPerLine")
    @JsonProperty("charsPerLine")
    var charsPerLine: Double? = null

    @get:JsonProperty("linesPerPage")
    @set:JsonProperty("linesPerPage")
    @JsonProperty("linesPerPage")
    var linesPerPage: Double? = null

    @get:JsonProperty("defaultMarginWidth")
    @set:JsonProperty("defaultMarginWidth")
    @JsonProperty("defaultMarginWidth")
    var defaultMarginWidth: Double? = null

    @get:JsonProperty("versoLastLineBlank")
    @set:JsonProperty("versoLastLineBlank")
    @JsonProperty("versoLastLineBlank")
    var versoLastLineBlank: Boolean? = null

    @get:JsonProperty("carryLines")
    @set:JsonProperty("carryLines")
    @JsonProperty("carryLines")
    var carryLines: Double? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Page) return false

        if (charsPerLine != other.charsPerLine) return false
        if (linesPerPage != other.linesPerPage) return false
        if (defaultMarginWidth != other.defaultMarginWidth) return false
        if (versoLastLineBlank != other.versoLastLineBlank) return false
        if (carryLines != other.carryLines) return false

        return true
    }

    override fun hashCode(): Int {
        var result = charsPerLine?.hashCode() ?: 0
        result = 31 * result + (linesPerPage?.hashCode() ?: 0)
        result = 31 * result + (defaultMarginWidth?.hashCode() ?: 0)
        result = 31 * result + (versoLastLineBlank?.hashCode() ?: 0)
        result = 31 * result + (carryLines?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Page(charsPerLine=$charsPerLine, linesPerPage=$linesPerPage, defaultMarginWidth=$defaultMarginWidth, versoLastLineBlank=$versoLastLineBlank, carryLines=$carryLines)"
    }
}
