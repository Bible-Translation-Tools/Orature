package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import java.io.File
import java.util.*
import java.util.regex.Pattern

object BiblicalReferencesParser {
    val BOOK_TITLE_PATTERN = Pattern.compile("^(?:.*[.|:])?(\\w{2,3}) 0$")
    val CHAPTER_TITLE_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} (\\d{1,3}):0$")
    val CHAPTER_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} (\\d{1,3})$")
    val VERSE_TITLE_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} \\d{1,3}:\\d{1,3}:0$")
    val VERSE_PATTERN = Pattern.compile("^(?:.*[.|:])?\\w{2,3} \\d{1,3}:(\\d{1,3})(?:-(\\d{1,3}))?$")


    fun parseBiblicalReference(reference: String): String {
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
                if (verseEnd != null) "orature-vm-${verseStart}-${verseEnd}" else "orature-vm-$verseStart"
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
                    val label = if (marker.end != marker.start) "${marker.start}-${marker.end}" else "${marker.start}"
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