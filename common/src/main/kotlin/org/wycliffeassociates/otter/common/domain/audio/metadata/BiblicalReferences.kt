package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import java.util.*
import java.util.regex.Pattern
import kotlin.text.lowercase

/**
 * A data class to represent a parsed Bible book, chapter, and verse reference.
 *
 * @property book The 3-character book code (e.g., "GEN", "JHN").
 * @property chapter The chapter number, or null if not provided.
 * @property verse The starting verse number, or null if not provided.
 * @property verseRangeEnd The ending verse number for a range, or null if not provided.
 */
data class BCV(
    val book: String,
    val chapter: Int? = null,
    val verse: Int? = null,
    val verseRangeEnd: Int? = null
)

object BiblicalReferencesParser {
    val BOOK_TITLE_PATTERN = Pattern.compile("^(?:.*[.|:])?(\\w{2,3}) 0$")
    val CHAPTER_TITLE_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} (\\d{1,3}):0$")
    val CHAPTER_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} (\\d{1,3})$")
    val VERSE_TITLE_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} \\d{1,3}:\\d{1,3}:0$")
    val VERSE_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} \\d{1,3}:(\\d{1,3})(?:-(\\d{1,3}))?$")

    /**
     * Parses a string reference into a BCV data class, attempting to handle a
     * subset of the U23003 scripture reference standard.
     *
     * The function first tries to find a book, chapter, and verse reference. It
     * ignores more complex components like word, character, and marker references
     * to fit the simpler BCV data class.
     *
     * @param reference The string to parse.
     * @return A BCV data class with the parsed components, or null if the string format is invalid.
     */
    fun parseBcv(reference: String): BCV? {
        // This regex is designed to be more flexible and handle the structure
        // of U23003 references by capturing the core BCV components and
        // ignoring the rest.
        // Group 1: Book code (3 uppercase letters or the two-character codes from the spec)
        // Group 2: Chapter number (digits, optional)
        // Group 3: Starting verse number (digits, optional, preceded by ':' or '.')
        // Group 4: Ending verse number (digits, optional, preceded by '-')
        val regex = Regex("^(?:[a-zA-Z0-9\\-]+\\.)?([A-Z0-9]{3})(?:\\s+(\\d+)(?:\\s*[:.]\\s*(\\d+)(?:\\s*-\\s*(\\d+))?)?)?")

        val matchResult = regex.find(reference.trim())

        if (matchResult != null) {
            val book = matchResult.groupValues[1]
            val chapterStr = matchResult.groupValues[2]
            val verseStr = matchResult.groupValues[3]
            val verseRangeEndStr = matchResult.groupValues[4]

            // Convert captured strings to nullable Integers
            val chapter = chapterStr.toIntOrNull()
            val verse = verseStr.toIntOrNull()
            val verseRangeEnd = verseRangeEndStr.toIntOrNull()

            return BCV(book, chapter, verse, verseRangeEnd)
        }

        return null
    }

    fun parseToMarkerLabel(reference: String): String {
        val bookTitle = BOOK_TITLE_PATTERN.matcher(reference)
        val chapterTitle = CHAPTER_TITLE_PATTERN.matcher(reference)
        val chapter = CHAPTER_PATTERN.matcher(reference)
        val verseTitle = VERSE_TITLE_PATTERN.matcher(reference)
        val verse = VERSE_PATTERN.matcher(reference)

        return when {
            bookTitle.matches() -> {
                val title = bookTitle.group(1)!!.lowercase(Locale.US)
                "orature-book-$title"
            }

            chapterTitle.matches() -> {
                val title = chapterTitle.group(1)!!
                "orature-chapter-$title"
            }

            chapter.matches() -> {
                val title = chapter.group(1)!!
                "orature-chapter-$title"
            }

            verseTitle.matches() -> {
                reference
            }

            verse.matches() -> {
                val verseStart = verse.group(1)!!
                val verseEnd = if (verse.groupCount() == 2) verse.group(2) else null
                if (verseEnd != null) {
                    "orature-vm-${verseStart}-${verseEnd}"
                } else {
                    "orature-vm-$verseStart"
                }
            }

            else -> {
                reference
            }
        }
    }
}

object OratureMarkerConverter {
    fun toBiblicalReference(
        marker: AudioMarker,
        bookSlug: String? = null,
        chapterNumber: Int? = null,
    ): String? {
        try {
            return when (marker) {
                is BookMarker -> {
                    "${marker.bookSlug.allCaps()} 0"
                }

                is ChapterMarker -> {
                    "${bookSlug!!.allCaps()} ${marker.chapterNumber}:0"
                }

                is ChunkMarker -> {
                    "${bookSlug!!.allCaps()} ${chapterNumber!!}:${marker.chunk}"
                }

                is VerseMarker -> {
                    val label = if (marker.end != marker.start) {
                        "${marker.start}-${marker.end}"
                    } else {
                        "${marker.start}"
                    }
                    "${bookSlug!!.allCaps()} ${chapterNumber!!}:${marker.label}"
                }

                else -> {
                    null
                }
            }
        } catch (e: NullPointerException) {
            println("$marker $bookSlug $chapterNumber")
            throw e
        }
    }

    private fun String.allCaps(): String {
        return this.uppercase(Locale.US)
    }
}