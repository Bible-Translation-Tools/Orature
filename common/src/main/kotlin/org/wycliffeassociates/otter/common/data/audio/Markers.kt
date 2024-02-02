/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.data.audio

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.wycliffeassociates.otter.common.audio.AudioCue

enum class MarkerType {
    TITLE,
    CONTENT,
    METADATA,
    UNKNOWN
}


// These sort starts pad out the sort value so that all markers in an audio file can be sorted in BCV order
const val BOOK_SORT_START = 0
const val CHAPTER_SORT_START = 1_000
const val VERSE_SORT_START = 10_000
const val CHUNK_SORT_START = 100_000
const val UNKNOWN_SORT_START = 100_000_000


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = VerseMarker::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = VerseMarker::class, name = "verse_marker"),
    JsonSubTypes.Type(value = ChapterMarker::class, name = "chapter_marker"),
    JsonSubTypes.Type(value = BookMarker::class, name = "book_marker"),
    JsonSubTypes.Type(value = UnknownMarker::class, name = "unknown_marker")
)
interface AudioMarker {
    val type: MarkerType
    /**
     * The marker label which does not contain any namespacing, most often a verse number or verse range
     */
    val label: String
    val location: Int

    /**
     * The marker label with the appropriate namespacing (such as "orature-vm-{number}"
     */
    val formattedLabel: String

    fun toCue(): AudioCue {
        return AudioCue(location, formattedLabel)
    }

    fun clone(): AudioMarker
    fun clone(location: Int): AudioMarker

    val sort: Int
}

data class UnknownMarker(override val location: Int, override val label: String) : AudioMarker {
    override val type = MarkerType.UNKNOWN

    constructor(cue: AudioCue) : this(cue.location, cue.label)

    override val formattedLabel
        get() = label

    override fun toString(): String {
        return formattedLabel
    }

    override fun clone(): UnknownMarker {
        return copy()
    }

    override fun clone(location: Int): UnknownMarker {
        return copy(location = location)
    }

    // Note: this will overflow an int if the position of an unknown marker is beyond ~2GB worth of audio frames
    // which is about 6 hours for 16bit 44.1khz mono
    override val sort = UNKNOWN_SORT_START + location
}

data class BookMarker(val bookSlug: String, override val location: Int) : AudioMarker {
    override val type = MarkerType.TITLE

    override val label: String
        @JsonIgnore
        get() = bookSlug

    override val formattedLabel
        @JsonIgnore
        get() = "orature-book-${label}"

    override fun toString(): String {
        return formattedLabel
    }

    override fun clone(): BookMarker {
        return copy()
    }

    override fun clone(location: Int): BookMarker {
        return copy(location = location)
    }

    override val sort: Int = BOOK_SORT_START
}

data class ChapterMarker(val chapterNumber: Int, override val location: Int) : AudioMarker {
    override val type = MarkerType.TITLE

    override val label: String
        @JsonIgnore
        get() = "$chapterNumber"

    override val formattedLabel
        @JsonIgnore
        get() = "orature-chapter-${label}"

    override fun toString(): String {
        return formattedLabel
    }

    override fun clone(): ChapterMarker {
        return copy()
    }

    override fun clone(location: Int): ChapterMarker {
        return copy(location = location)
    }

    override val sort = CHAPTER_SORT_START + chapterNumber
}

data class VerseMarker(val start: Int, val end: Int, override val location: Int) : AudioMarker {
    override val type = MarkerType.CONTENT

    override val label: String
        @JsonIgnore
        get() = if (end != start) "$start-$end" else "$start"

    override val formattedLabel
        @JsonIgnore
        get() = "orature-vm-${label}"

    override fun toString(): String {
        return formattedLabel
    }

    override fun clone(): VerseMarker {
        return copy()
    }

    override fun clone(location: Int): VerseMarker {
        return copy(location = location)
    }

    override val sort = VERSE_SORT_START + start
}

data class ChunkMarker(val chunk: Int, override val location: Int) : AudioMarker {
    override val type = MarkerType.CONTENT

    override val label = "$chunk"
    override val formattedLabel
        get() = "orature-chunk-${label}"

    override fun toString(): String {
        return formattedLabel
    }

    override fun clone(): ChunkMarker {
        return copy()
    }

    override fun clone(location: Int): ChunkMarker {
        return copy(location = location)
    }

    override val sort = CHUNK_SORT_START + chunk
}