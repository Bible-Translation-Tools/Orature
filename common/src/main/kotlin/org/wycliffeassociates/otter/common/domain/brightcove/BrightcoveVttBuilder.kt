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
package org.wycliffeassociates.otter.common.domain.brightcove

import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.domain.audio.metadata.framesToUs
import java.io.File

data class BrightcoveVttCue(
    val id: String,
    val startTimeUs: Long,
    val endTimeUs: Long,
    val text: String
)

class BrightcoveVttBuilder {

    fun buildCues(
        bookCode: String,
        chapterNumber: Int,
        verseMarkers: List<VerseMarker>,
        totalFrames: Int,
        chapterContent: List<Content>
    ): List<BrightcoveVttCue> {
        if (verseMarkers.isEmpty()) {
            return emptyList()
        }

        val sorted = verseMarkers.sortedBy { it.location }
        return sorted.mapIndexed { index, marker ->
            val startUs = framesToUs(marker.location)
            val endUs = if (index == sorted.lastIndex) {
                framesToUs(totalFrames)
            } else {
                framesToUs(sorted[index + 1].location)
            }

            val id = buildCueId(bookCode, chapterNumber, marker.start, marker.end)
            val text = buildVerseText(chapterContent, marker.start, marker.end)
            BrightcoveVttCue(id, startUs, endUs, text)
        }
    }

    fun writeVtt(file: File, cues: List<BrightcoveVttCue>) {
        file.outputStream().writer(Charsets.UTF_8).use { writer ->
            writer.write("WEBVTT\n\n")
            cues.forEach { cue ->
                if (cue.id.isNotBlank()) {
                    writer.write("${cue.id}\n")
                }
                writer.write("${timestamp(cue.startTimeUs)} --> ${timestamp(cue.endTimeUs)}\n")
                if (cue.text.isNotBlank()) {
                    writer.write("${cue.text}\n")
                }
                writer.write("\n")
            }
        }
    }

    private fun buildCueId(bookCode: String, chapterNumber: Int, start: Int, end: Int): String {
        val reference = if (start == end) {
            "$start"
        } else {
            "$start-$end"
        }
        return "${bookCode}_${chapterNumber}:${reference}"
    }

    private fun buildVerseText(contents: List<Content>, start: Int, end: Int): String {
        val texts = contents
            .filter { it.start in start..end }
            .mapNotNull { it.text?.trim() }
            .filter { it.isNotEmpty() }

        if (texts.isNotEmpty()) {
            return texts.joinToString(" ")
        }

        return contents.firstOrNull { it.start == start }
            ?.text
            ?.trim()
            ?: ""
    }

    private fun timestamp(timeUs: Long): String {
        val hours = timeUs / 3_600_000_000L
        val minutes = (timeUs % 3_600_000_000L) / 60_000_000L
        val seconds = (timeUs % 60_000_000L) / 1_000_000L
        val milliseconds = (timeUs % 1_000_000L) / 1000L

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds)
    }
}
