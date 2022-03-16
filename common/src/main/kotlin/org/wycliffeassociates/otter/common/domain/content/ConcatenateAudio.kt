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
package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class ConcatenateAudio @Inject constructor(private val directoryProvider: IDirectoryProvider) {

    fun execute(
        files: List<File>,
        includeMarkers: Boolean = true
    ): Single<File> {
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
            if (includeMarkers) generateMarkers(files, outputFile)

            outputFile.file
        }
    }

    private fun generateMarkers(inputFiles: List<File>, outputAudio: AudioFile) {
        var markerLocation = 0

        inputFiles.forEach { file ->
            val audioFile = AudioFile(file)
            val marker = audioFile.metadata.getCues().first().label
            outputAudio.metadata.addCue(markerLocation, marker)
            markerLocation += audioFile.totalFrames
        }
        outputAudio.update()
    }
}
