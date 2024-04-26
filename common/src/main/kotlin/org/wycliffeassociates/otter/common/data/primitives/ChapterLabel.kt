package org.wycliffeassociates.otter.common.data.primitives

const val PSALMS_SLUG = "psa"

object ChapterLabel {
    fun of(bookSlug: String): String {
        return if (bookSlug.startsWith(PSALMS_SLUG)) {
            ContentLabel.PSALM.value
        } else ContentLabel.CHAPTER.value
    }
}