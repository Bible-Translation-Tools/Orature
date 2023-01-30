package org.wycliffeassociates.otter.common.versification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Data class representing the json form of paratext vrs Versification files
 * https://github.com/ubsicap/versification_json/blob/master/versification_as_json.schema.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ParatextVersification(
    val basedOn: String?,
    val maxVerses: MaxVerses,
    val mappedVerses: MappedVerses?,
    val excludedVerses: List<String>?,
    val partialVerses: PartialVerses?
): Versification {
    override fun getVersesInChapter(bookSlug: String, chapterNumber: Int): Int {
        return maxVerses[bookSlug]?.get(chapterNumber)?.toInt() ?: 0
    }
}

typealias MaxVerses = Map<String, List<String>>

typealias MappedVerses = Map<String, String>

typealias PartialVerses = Map<String, List<String>>