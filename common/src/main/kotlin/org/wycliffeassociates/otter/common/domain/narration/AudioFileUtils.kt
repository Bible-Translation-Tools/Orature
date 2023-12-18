package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
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