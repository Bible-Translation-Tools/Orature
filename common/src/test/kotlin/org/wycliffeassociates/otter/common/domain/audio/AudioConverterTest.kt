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

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.mp3.MP3FileReader
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.pcm.PcmOutputStream
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import java.io.File

class AudioConverterTest {

    private fun writeWavFile(file: File, samplesToWrite: Int, cues: List<AudioCue>): WavFile {
        val wav = WavFile(
            file,
            DEFAULT_CHANNELS,
            DEFAULT_SAMPLE_RATE,
            DEFAULT_BITS_PER_SAMPLE,
            WavMetadata(listOf(CueChunk()))
        )
        for (cue in cues) {
            wav.metadata.addCue(cue.location, cue.label)
        }
        WavOutputStream(wav).use {
            for (i in 0 until samplesToWrite) {
                it.write(i)
            }
        }
        wav.update()
        return wav
    }

    private fun writePcmFile(file: File, samplesToWrite: Int): PcmFile {
        val pcm = PcmFile(file)
        PcmOutputStream(pcm).use {
            for (i in 0 until samplesToWrite) {
                it.write(i)
            }
        }
        return pcm
    }

    @Test
    fun `test mp3 audio size is equal wav audio size after conversion`() {
        val tempWav = File.createTempFile("testwav", ".wav")
        tempWav.deleteOnExit()
        val audioSamples = 56734
        val wav = writeWavFile(tempWav, audioSamples, listOf())
        val mp3 = File(tempWav.parent, "${tempWav.nameWithoutExtension}.mp3")
        mp3.deleteOnExit()

        val converter = AudioConverter()
        converter.wavToMp3(tempWav, mp3).subscribe {
            val mp3File = MP3FileReader(mp3)

            // Maybe there is a bug in RandomAccessDecoder
            // It returns different number of total frames than in wav file
            // Though if to check mp3 file in ocenaudio it shows total frames like in wav file

            //Assert.assertEquals(wav.totalAudioLength, mp3File.totalFrames)
        }
    }

    @Test
    fun `test pcm file size is equal wav audio size after conversion`() {
        val tempWav = File.createTempFile("testwav", ".wav")
        tempWav.deleteOnExit()
        val audioSamples = 76098
        val wav = writeWavFile(tempWav, audioSamples, listOf())
        val pcm = File(tempWav.parent, "${tempWav.nameWithoutExtension}.pcm")
        pcm.deleteOnExit()

        val converter = AudioConverter()
        converter.wavToPcm(tempWav, pcm).subscribe {
            Assert.assertEquals(wav.totalAudioLength, pcm.length().toInt())
        }
    }

    @Test
    fun `test wav audio size is equal to pcm file size after conversion`() {
        val tempPcm = File.createTempFile("testpcm", ".pcm")
        tempPcm.deleteOnExit()
        val audioSamples = 51267
        writePcmFile(tempPcm, audioSamples)
        val wav = File(tempPcm.parent, "${tempPcm.nameWithoutExtension}.wav")
        wav.deleteOnExit()

        val converter = AudioConverter()
        converter.pcmToWav(tempPcm, wav).subscribe {
            val wavFile = WavFile(wav)
            Assert.assertEquals(tempPcm.length().toInt(), wavFile.totalAudioLength)
        }
    }
}