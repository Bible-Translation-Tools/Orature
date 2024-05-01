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
package org.wycliffeassociates.otter.common.domain.audio

import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import org.wycliffeassociates.otter.common.data.audio.*
import org.wycliffeassociates.otter.common.domain.audio.metadata.OratureMarkers
import java.util.regex.Pattern

internal object OratureCueParser {

    private val parsers = listOf(
        VerseMarkerParser(),
        ChunkMarkerParser(),
        ChapterMarkerParser(),
        BookMarkerParser()
    )

    fun parse(metadata: AudioMetadata): OratureMarkers {
        return parse(metadata.getCues())
    }

    fun parse(audio: AudioFile): OratureMarkers {
        return parse(audio.metadata)
    }

    fun parse(cues: List<AudioCue>): OratureMarkers {
        val markers = OratureMarkers()

        var result = parsers[0].parse(cues)
        for (i in 1 until parsers.size) {
            markers.addMarkers(parsers[i - 1].cueType, result.accepted)
            result = parsers[i].parse(result.rejected)
        }
        markers.addMarkers(parsers.last().cueType, result.accepted)
        markers.addMarkers(OratureCueType.UNKNOWN, result.rejected.map { UnknownMarker(it) })

        return markers
    }
}

internal class MarkerParseResult(val accepted: List<AudioMarker>, val rejected: List<AudioCue>)
internal interface MarkerParser {
    val cueType: OratureCueType
    val pattern: Pattern

    fun match(cue: AudioCue): AudioMarker?

    fun parse(cues: List<AudioCue>): MarkerParseResult {
        val accepted = mutableListOf<AudioMarker>()
        val rejected = mutableListOf<AudioCue>()

        cues.forEach { cue ->
            val result = match(cue)
            if (result != null) {
                accepted.add(result)
            } else {
                rejected.add(cue)
            }
        }

        return MarkerParseResult(accepted, rejected)
    }
}

internal class VerseMarkerParser : MarkerParser {
    override val cueType = OratureCueType.VERSE

    override val pattern: Pattern = Pattern.compile("^orature-vm-(\\d+)(?:-(\\d+))?\$")

    override fun match(cue: AudioCue): AudioMarker? {
        val start: Int
        val end: Int
        val matcher = pattern.matcher(cue.label)
        if (matcher.matches()) {
            start = matcher.group(1).toInt()
            end = if (matcher.groupCount() > 1 && !matcher.group(2).isNullOrBlank()) {
                matcher.group(2).toInt()
            } else start
            return VerseMarker(start, end, cue.location)
        }
        return null
    }

    override fun parse(cues: List<AudioCue>): MarkerParseResult {
        if (cues.isEmpty()) return MarkerParseResult(listOf(), listOf())

        val accepted = mutableListOf<AudioMarker>()
        val rejected = mutableListOf<AudioCue>()

        val oratureCues: Pattern = Pattern.compile("^orature-.*\$")
        val hasOratureCues = cues.any { oratureCues.matcher(it.label).matches() }

        if (hasOratureCues) {
            cues.forEach { cue ->
                val result = match(cue)
                if (result != null) {
                    accepted.add(result)
                } else {
                    rejected.add(cue)
                }
            }
            return MarkerParseResult(accepted, rejected)
        } else {
            matchAlternativeCues(cues, accepted, rejected)
        }

        return MarkerParseResult(accepted, rejected)
    }

    private fun matchAlternativeCues(
        cues: List<AudioCue>,
        accepted: MutableList<AudioMarker>,
        rejected: MutableList<AudioCue>
    ) {
        val oratureRegex = Regex("^orature-vm-(\\d+)(?:-(\\d+))?\$")

        val loneDigitRegex = Regex("^(\\d+)(?:-(\\d+))?$")
        val numberRegex = Regex("(\\d+)(?:-(\\d+))?")

        val oratureCues = cues.filter { it.label.matches(oratureRegex) }
        val leftoverCues = cues.filter { !oratureCues.contains(it) }
        val loneDigits = leftoverCues.filter { it.label.trim().matches(loneDigitRegex) }
        val potentialCues = leftoverCues
            .filter { !loneDigits.contains(it) }
            .filter { numberRegex.containsMatchIn(it.label) }
            .map {
                val match = numberRegex.find(it.label)
                val label = match!!.groupValues.first()!!
                AudioCue(it.location, label)
            }

        if (oratureCues.isNotEmpty()) {
            addMatchingCues(oratureCues, oratureRegex.toPattern(), accepted)
        } else if (loneDigits.isNotEmpty()) {
            addMatchingCues(
                loneDigits.map { AudioCue(it.location, it.label.trim()) },
                loneDigitRegex.toPattern(),
                accepted
            )
        } else if (potentialCues.isNotEmpty()) {
            addMatchingCues(potentialCues, numberRegex.toPattern(), accepted)
        }
        rejected.addAll(leftoverCues)
    }

    private fun addMatchingCues(cues: List<AudioCue>, pattern: Pattern, accepted: MutableList<AudioMarker>) {
        cues.forEach { cue ->
            val matcher = pattern.matcher(cue.label)
            if (matcher.matches()) {
                val start = matcher.group(1).toInt()
                val end = if (matcher.groupCount() > 1 && !matcher.group(2).isNullOrBlank()) {
                    matcher.group(2).toInt()
                } else start
                accepted.add(VerseMarker(start, end, cue.location))
            }
        }
    }
}

internal class ChunkMarkerParser : MarkerParser {
    override val cueType = OratureCueType.CHUNK

    override val pattern: Pattern = Pattern.compile("^orature-chunk-(\\d+)\$")

    override fun match(cue: AudioCue): AudioMarker? {
        val start: Int
        val matcher = pattern.matcher(cue.label)
        if (matcher.matches()) {
            start = matcher.group(1).toInt()
            return ChunkMarker(start, cue.location)
        }
        return null
    }
}

internal class ChapterMarkerParser : MarkerParser {
    override val cueType = OratureCueType.CHAPTER_TITLE

    override val pattern: Pattern = Pattern.compile("^orature-chapter-(\\d+)\$")

    override fun match(cue: AudioCue): AudioMarker? {
        val matcher = pattern.matcher(cue.label)
        if (matcher.matches()) {
            val chapter = matcher.group(1).toInt()
            return ChapterMarker(chapter, cue.location)
        }
        return null
    }
}

internal class BookMarkerParser : MarkerParser {
    override val cueType = OratureCueType.BOOK_TITLE

    override val pattern: Pattern = Pattern.compile("^orature-book-(.+)\$")

    override fun match(cue: AudioCue): AudioMarker? {
        val matcher = pattern.matcher(cue.label)
        if (matcher.matches()) {
            val book = matcher.group(1)
            return BookMarker(book, cue.location)
        }
        return null
    }
}