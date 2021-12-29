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
package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.domain.audio.LabeledAudio
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ConcatenateAudio(private val directoryProvider: IDirectoryProvider) {

    fun execute(files: List<File>): Single<File> {
        return Single.fromCallable {
            val inputFile = AudioFile(files.first())
            val tempFile = directoryProvider.createTempFile("output", ".wav")
            val outputFile = AudioFile(
                tempFile,
                inputFile.channels,
                inputFile.sampleRate,
                inputFile.bitsPerSample
            )
            outputFile.writer(append = true).use { os ->
                files.forEach { file ->
                    val audioFile = AudioFile(file)
                    val buffer = ByteArray(10240)
                    val reader = audioFile.reader()
                    reader.open()
                    while (reader.hasRemaining()) {
                        val written = reader.getPcmBuffer(buffer)
                        os.write(buffer, 0, written)
                    }
                    reader.release()
                }
            }
            outputFile.file
        }
    }

    fun concatWithMarkers(audioList: List<LabeledAudio>): Single<File> {
        return Single.fromCallable {
            val inputFile = AudioFile(audioList.first().file)
            val tempFile = directoryProvider.createTempFile("output", ".wav")
            val outputFile = AudioFile(
                tempFile,
                inputFile.channels,
                inputFile.sampleRate,
                inputFile.bitsPerSample
            )

            var markerLocation = 0

            outputFile.writer(append = true).use { os ->
                audioList.forEach { audio ->
                    val audioFile = AudioFile(audio.file)
                    val buffer = ByteArray(10240)
                    val reader = audioFile.reader()
                    reader.open()
                    while (reader.hasRemaining()) {
                        val written = reader.getPcmBuffer(buffer)
                        os.write(buffer, 0, written)
                    }
                    reader.release()

                    outputFile.metadata.addCue(
                        markerLocation, audio.title
                    )
                    markerLocation += audioFile.totalFrames
                }
            }
            outputFile.update()
            outputFile.file
        }
    }
}
