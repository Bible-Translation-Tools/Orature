/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Single
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject


typealias VerseSegments = Map<VerseMarker, File>
class SplitAudioOnCues @Inject constructor(private val directoryProvider: IDirectoryProvider) {

    fun execute(file: File, initialMarker: VerseMarker): VerseSegments {
        val sourceAudio = OratureAudioFile(file)
        val cues = sourceAudio
            .getMarker<VerseMarker>()
            .ifEmpty {
                listOf(initialMarker)
            }
        return splitAudio(file, cues)
    }

    fun executeAsync(file: File, initialMarker: VerseMarker): Single<VerseSegments> {
        return Single.fromCallable {
            execute(file, initialMarker)
        }
    }

    fun execute(file: File, cues: List<VerseMarker>): Single<VerseSegments> {
        return Single.fromCallable {
            splitAudio(file, cues)
        }
    }

    private fun splitAudio(file: File, cues: List<VerseMarker>): VerseSegments {
        val chunks = mutableMapOf<VerseMarker, File>()
        val sourceAudio = AudioFile(file)
        val totalFrames = sourceAudio.totalFrames
        cues.forEachIndexed { index, cue ->
            val audioStartEnd = getChunkAudioRange(index, totalFrames, cues)
            val pcmFile = directoryProvider.createTempFile("chunk$index", ".${AudioFileFormat.PCM.extension}")
            val pcmAudio = AudioFile(pcmFile)
            writeAudio(sourceAudio, pcmAudio, audioStartEnd)
            chunks[cue] = pcmFile
        }
        return chunks
    }

    private fun writeAudio(source: AudioFile, target: AudioFile, startEnd: Pair<Int, Int>) {
        val sourceReader = source.reader(startEnd.first, startEnd.second)
        val targetWriter = target.writer()

        sourceReader.use { reader ->
            reader.open()
            targetWriter.use { writer ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (reader.hasRemaining()) {
                    val written = reader.getPcmBuffer(buffer)
                    writer.write(buffer, 0, written)
                }
            }
        }
    }

    private fun getChunkAudioRange(index: Int, max: Int, cues: List<VerseMarker>): Pair<Int, Int> {
        val current = cues[index].location
        val nextIndex = index + 1
        val next = if (nextIndex in 0..cues.lastIndex) {
            cues[nextIndex].location
        } else {
            max
        }

        return Pair(current, next)
    }
}