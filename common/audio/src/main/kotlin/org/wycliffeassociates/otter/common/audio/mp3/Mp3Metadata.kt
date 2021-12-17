/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.audio.mp3

import java.io.File
import java.lang.Exception
import kotlin.math.roundToInt
import org.digitalmediaserver.cuelib.CueParser
import org.digitalmediaserver.cuelib.CueSheet
import org.digitalmediaserver.cuelib.CueSheetSerializer
import org.digitalmediaserver.cuelib.FileData
import org.digitalmediaserver.cuelib.Index
import org.digitalmediaserver.cuelib.Position
import org.digitalmediaserver.cuelib.TrackData
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE

/**
 * Frames in Cue files are different from frames in the audio sample context.
 * Frames in the cue context are with respect to CD-ROM Sectors, in which there are 75 in one second.
 * https://en.wikipedia.org/wiki/Cue_sheet_(computing)
 * https://www.cdrfaq.org/faq02.html#S2-43-3
 */
private const val CUE_FRAME_SIZE = 75.0

internal class Mp3Metadata(val file: File) : AudioMetadata {

    private val _cues = mutableListOf<AudioCue>()
    private var title = file.nameWithoutExtension

    init {
        if (file.exists() && file.length() > 0) {
            try {
                val cuesheet = CueParser.parse(file, Charsets.UTF_8)
                if (cuesheet.title.isNotEmpty()) {
                    title = cuesheet.title
                }
                cuesheet.allTrackData.forEach {
                    val label = it.title
                    val index = it.indices.find { it.number == 1 }
                    index?.let {
                        val position = (
                                index.position.totalFrames / CUE_FRAME_SIZE * DEFAULT_SAMPLE_RATE.toFloat()
                                )
                            .roundToInt()
                        _cues.add(AudioCue(position, label))
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun addCue(location: Int, label: String) {
        _cues.add(AudioCue(location, label))
    }

    override fun getCues(): List<AudioCue> {
        return _cues
    }

    fun write() {
        file.delete()
        file.createNewFile()
        val sheet = CueSheet()
        sheet.title = title
        val fileData = FileData(sheet, "\"${file.name}\"", "MP3")
        for ((i, cue) in _cues.sortedBy { it.location }.withIndex()) {
            val cueNumber = cue.label.toInt()
            val trackData = TrackData(fileData, cueNumber, "AUDIO")
            trackData.title = cue.label
            val index = Index()
            index.number = 1
            index.position = Position(cue.location.toLong(), DEFAULT_SAMPLE_RATE)
            trackData.indices.add(index)

            fileData.trackData.add(trackData)
        }
        sheet.fileData.add(fileData)
        val serialized = CueSheetSerializer().serializeCueSheet(sheet)
        file.writer().use {
            it.write(serialized)
        }
    }
}
