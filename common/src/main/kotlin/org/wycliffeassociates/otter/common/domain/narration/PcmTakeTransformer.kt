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

import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.AudioConverter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.lang.IllegalArgumentException
import javax.inject.Inject

class PcmTakeTransformer @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val audioConverter: AudioConverter
) {

    private var originalTake: Take? = null

    /**
     * Converts take's audio if it's a PCM file to WAV.
     * Otherwise, returns unmodified take
     */
    fun preTransform(take: Take): Take {
        originalTake = take

        val format = AudioFileFormat.of(take.file.extension)

        if (format == AudioFileFormat.PCM) {
            val wav = directoryProvider.createTempFile(
                "take${take.number}",
                ".${AudioFileFormat.WAV.extension}"
            )
            audioConverter.pcmToWav(take.file, wav).blockingGet()
            return take.copy(name = wav.name, file = wav)
        }

        return take
    }

    /**
     * Converts take's WAV audio back to PCM if original file was PCM.
     * Otherwise, returns unmodified take
     */
    @Throws(IllegalArgumentException::class)
    fun postTransform(take: Take): Take {
        return originalTake?.let { origTake ->
            val format = AudioFileFormat.of(origTake.file.extension)
            if (format == AudioFileFormat.PCM) {
                audioConverter.wavToPcm(take.file, origTake.file).blockingGet()
            }
            origTake
        } ?: throw IllegalArgumentException("postTransform is called before preTransform")
    }
}