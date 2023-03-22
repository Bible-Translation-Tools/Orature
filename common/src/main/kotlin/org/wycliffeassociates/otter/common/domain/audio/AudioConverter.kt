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
package org.wycliffeassociates.otter.common.domain.audio

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import java.io.File
import javax.inject.Inject
import de.sciss.jump3r.Main as jump3r

class AudioConverter @Inject constructor() {
    fun wavToMp3(
        wavFile: File,
        mp3File: File,
        bitrate: Int = 64
    ): Completable {
        return Completable.fromCallable {
            val args = arrayOf(
                "-b", bitrate.toString(),
                "-m", "m",
                wavFile.invariantSeparatorsPath,
                mp3File.invariantSeparatorsPath
            )
            jump3r().run(args)
        }
    }

    fun wavToPcm(wavFile: File, pcmFile: File): Completable {
        return Completable.fromCallable {
            val wavReader = WavFile(wavFile).reader()
            val pcmWriter = PcmFile(pcmFile).writer(append = false)

            wavReader.use { reader ->
                pcmWriter.use { writer ->
                    reader.open()
                    val buffer = ByteArray(10240)
                    while (reader.hasRemaining()) {
                        val written = reader.getPcmBuffer(buffer)
                        writer.write(buffer, 0, written)
                    }
                }
            }
        }
    }

    fun pcmToWav(pcmFile: File, wavFile: File): Completable {
        return Completable.fromCallable {
            val pcmReader = PcmFile(pcmFile).reader()
            val wavWriter = WavFile(
                wavFile,
                DEFAULT_CHANNELS,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_BITS_PER_SAMPLE
            ).writer(append = false)

            pcmReader.use { reader ->
                wavWriter.use { writer ->
                    reader.open()
                    val buffer = ByteArray(10240)
                    while (reader.hasRemaining()) {
                        val written = reader.getPcmBuffer(buffer)
                        writer.write(buffer, 0, written)
                    }
                }
            }
        }
    }
}
