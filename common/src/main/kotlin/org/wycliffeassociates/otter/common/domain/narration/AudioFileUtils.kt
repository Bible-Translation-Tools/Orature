package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
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
        audio.writer(true).use { writer ->
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
}

/**
 * Constructs a Wav file at path specified by bouncedAudio with audio data provided by the reader parameter and cues
 * provided by the markers parameter
 */
fun bounceAudio(bouncedAudio: File, reader: AudioFileReader, markers: List<AudioMarker>) {
    val bytes = ByteArray(DEFAULT_BUFFER_SIZE)

    reader.open()
    reader.seek(0)

    if (bouncedAudio.exists() && bouncedAudio.length() > 0) {
        bouncedAudio.delete()
    }
    val wav = WavFile(bouncedAudio, 1, 44100, 16)
    WavOutputStream(wav).use { out ->
        while (reader.hasRemaining()) {
            val read = reader.getPcmBuffer(bytes)
            out.write(bytes, 0, read)
        }
    }
    wav.update()
    val oaf = OratureAudioFile(bouncedAudio)
    for (verse in markers) {
        oaf.addMarker<AudioMarker>(verse.clone())
    }
    oaf.update()
}
