package org.wycliffeassociates.otter.common.data.primitives

class ChapterLabel {
    companion object {
        fun of(bookSlug: String): String {
            return if (bookSlug.startsWith("psa")) {
                "psalm"
            } else "chapter"
        }
    }
}