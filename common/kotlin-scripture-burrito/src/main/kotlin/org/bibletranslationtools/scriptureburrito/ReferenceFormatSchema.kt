package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "noSpaceBetweenBookAndChapter",
    "chapterVerseSeparator",
    "rangeIndicator",
    "sequenceIndicator",
    "chapterRangeSeparator",
    "chapterNumberSeparator",
    "bookSequenceSeparator",
    "referenceExtraMaterial",
    "referenceFinalPunctuation",
    "bookSourceForMarkerXt",
    "bookSourceForMarkerR"
)
class ReferenceFormatSchema {
    @get:JsonProperty("noSpaceBetweenBookAndChapter")
    @set:JsonProperty("noSpaceBetweenBookAndChapter")
    @JsonProperty("noSpaceBetweenBookAndChapter")
    var noSpaceBetweenBookAndChapter: Boolean? = null

    @get:JsonProperty("chapterVerseSeparator")
    @set:JsonProperty("chapterVerseSeparator")
    @JsonProperty("chapterVerseSeparator")
    var chapterVerseSeparator: String? = null

    @get:JsonProperty("rangeIndicator")
    @set:JsonProperty("rangeIndicator")
    @JsonProperty("rangeIndicator")
    var rangeIndicator: String? = null

    @get:JsonProperty("sequenceIndicator")
    @set:JsonProperty("sequenceIndicator")
    @JsonProperty("sequenceIndicator")
    var sequenceIndicator: String? = null

    @get:JsonProperty("chapterRangeSeparator")
    @set:JsonProperty("chapterRangeSeparator")
    @JsonProperty("chapterRangeSeparator")
    var chapterRangeSeparator: String? = null

    @get:JsonProperty("chapterNumberSeparator")
    @set:JsonProperty("chapterNumberSeparator")
    @JsonProperty("chapterNumberSeparator")
    var chapterNumberSeparator: String? = null

    @get:JsonProperty("bookSequenceSeparator")
    @set:JsonProperty("bookSequenceSeparator")
    @JsonProperty("bookSequenceSeparator")
    var bookSequenceSeparator: String? = null

    @get:JsonProperty("referenceExtraMaterial")
    @set:JsonProperty("referenceExtraMaterial")
    @JsonProperty("referenceExtraMaterial")
    var referenceExtraMaterial: MutableList<String>? = ArrayList()

    @get:JsonProperty("referenceFinalPunctuation")
    @set:JsonProperty("referenceFinalPunctuation")
    @JsonProperty("referenceFinalPunctuation")
    var referenceFinalPunctuation: String? = null

    @get:JsonProperty("bookSourceForMarkerXt")
    @set:JsonProperty("bookSourceForMarkerXt")
    @JsonProperty("bookSourceForMarkerXt")
    var bookSourceForMarkerXt: String? = null

    @get:JsonProperty("bookSourceForMarkerR")
    @set:JsonProperty("bookSourceForMarkerR")
    @JsonProperty("bookSourceForMarkerR")
    var bookSourceForMarkerR: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReferenceFormatSchema) return false

        if (noSpaceBetweenBookAndChapter != other.noSpaceBetweenBookAndChapter) return false
        if (chapterVerseSeparator != other.chapterVerseSeparator) return false
        if (rangeIndicator != other.rangeIndicator) return false
        if (sequenceIndicator != other.sequenceIndicator) return false
        if (chapterRangeSeparator != other.chapterRangeSeparator) return false
        if (chapterNumberSeparator != other.chapterNumberSeparator) return false
        if (bookSequenceSeparator != other.bookSequenceSeparator) return false
        if (referenceExtraMaterial != other.referenceExtraMaterial) return false
        if (referenceFinalPunctuation != other.referenceFinalPunctuation) return false
        if (bookSourceForMarkerXt != other.bookSourceForMarkerXt) return false
        if (bookSourceForMarkerR != other.bookSourceForMarkerR) return false

        return true
    }

    override fun hashCode(): Int {
        var result = noSpaceBetweenBookAndChapter?.hashCode() ?: 0
        result = 31 * result + (chapterVerseSeparator?.hashCode() ?: 0)
        result = 31 * result + (rangeIndicator?.hashCode() ?: 0)
        result = 31 * result + (sequenceIndicator?.hashCode() ?: 0)
        result = 31 * result + (chapterRangeSeparator?.hashCode() ?: 0)
        result = 31 * result + (chapterNumberSeparator?.hashCode() ?: 0)
        result = 31 * result + (bookSequenceSeparator?.hashCode() ?: 0)
        result = 31 * result + (referenceExtraMaterial?.hashCode() ?: 0)
        result = 31 * result + (referenceFinalPunctuation?.hashCode() ?: 0)
        result = 31 * result + (bookSourceForMarkerXt?.hashCode() ?: 0)
        result = 31 * result + (bookSourceForMarkerR?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ReferenceFormatSchema(noSpaceBetweenBookAndChapter=$noSpaceBetweenBookAndChapter, chapterVerseSeparator=$chapterVerseSeparator, rangeIndicator=$rangeIndicator, sequenceIndicator=$sequenceIndicator, chapterRangeSeparator=$chapterRangeSeparator, chapterNumberSeparator=$chapterNumberSeparator, bookSequenceSeparator=$bookSequenceSeparator, referenceExtraMaterial=$referenceExtraMaterial, referenceFinalPunctuation=$referenceFinalPunctuation, bookSourceForMarkerXt=$bookSourceForMarkerXt, bookSourceForMarkerR=$bookSourceForMarkerR)"
    }
}
