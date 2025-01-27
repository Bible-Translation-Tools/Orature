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
package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class ConcatenateAudio @Inject constructor(private val directoryProvider: IDirectoryProvider) {

    fun execute(
        files: List<File>,
        includeMarkers: Boolean = true
    ): Single<File> {
        return Single.fromCallable {
            val inputFile = OratureAudioFile(files.first())
            val tempFile = directoryProvider.createTempFile("output", ".wav")
            val outputFile = OratureAudioFile(
                tempFile,
                inputFile.channels,
                inputFile.sampleRate,
                inputFile.bitsPerSample
            )
            outputFile.writer(append = true).use { os ->
                files.forEach { file ->
                    val oratureAudioFile = OratureAudioFile(file)
                    val buffer = ByteArray(10240)
                    oratureAudioFile.reader().use { reader ->
                        reader.open()
                        while (reader.hasRemaining()) {
                            val written = reader.getPcmBuffer(buffer)
                            os.write(buffer, 0, written)
                        }
                    }
                }
            }
            if (includeMarkers) generateMarkers(files, outputFile)

            outputFile.file
        }
    }

    private fun generateMarkers(inputFiles: List<File>, outputAudio: OratureAudioFile) {
        var markerLocation = 0
        var markerCount: Int = -1

        inputFiles.forEach { file ->
            val oratureAudioFile = OratureAudioFile(file)
            val oldMarker = oratureAudioFile.getMarker<VerseMarker>().firstOrNull()
            val markerToAdd = if (oldMarker != null) {
                oldMarker
            } else if (markerCount > 0) {
                markerCount++
                VerseMarker(start= markerCount, end = markerCount, markerLocation)
            } else {
                markerCount = 1
                VerseMarker(start = 1,  end = 1, markerLocation)
            }
            outputAudio.addMarker(markerToAdd)
            markerLocation += oratureAudioFile.totalFrames
        }
        outputAudio.update()
    }
}
