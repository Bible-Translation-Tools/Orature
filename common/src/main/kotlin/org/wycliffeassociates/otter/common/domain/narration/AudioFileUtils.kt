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
package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import java.io.File
import javax.inject.Inject

class AudioFileUtils @Inject constructor(private val directoryProvider: IDirectoryProvider) {

    fun getSectionAsFile(audio: AudioFile, reader: AudioFileReader): File {
        val file = directoryProvider.createTempFile("verse", ".${audio.file.extension}")
        val pcmWriter = AudioFile(file).writer(append = false)
        reader.use { reader ->
            pcmWriter.use { writer ->
                reader.open()
                val buffer = ByteArray(10240)
                while (reader.hasRemaining()) {
                    val written = reader.getPcmBuffer(buffer)
                    writer.write(buffer, 0, written)
                }
            }
        }
        return file
    }

    fun appendFile(audio: AudioFile, file: File) {
        val appendedAudio = AudioFile(file)
        audio.writer(true, true).use { writer ->
            appendedAudio.reader().use { reader ->
                reader.open()
                val buffer = ByteArray(10240)
                while (reader.hasRemaining()) {
                    val written = reader.getPcmBuffer(buffer)
                    writer.write(buffer, 0, written)
                }
            }
        }
    }

    fun resampleAudio(source: File, target: File, targetSampleRate: Int = 44100) {
        val audio = AudioAttributes().apply {
            setCodec("libmp3lame")
            setBitRate(128000)
            setChannels(1)  // Set to mono
            setSamplingRate(targetSampleRate)
        }

        val attrs = EncodingAttributes().apply {
            setOutputFormat("mp3")
            setAudioAttributes(audio)
        }

        val encoder = Encoder()
        encoder.encode(MultimediaObject(source), target, attrs)
    }
}