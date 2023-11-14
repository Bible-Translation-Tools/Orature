package org.wycliffeassociates.otter.common.domain.audio

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.AudioFileReaderProvider
import java.io.File

class OratureAudioFileReaderProvider(val file: File) : AudioFileReaderProvider {
    override fun getAudioFileReader(start: Int?, end: Int?): AudioFileReader {
        return OratureAudioFile(file).reader(start, end)
    }
}