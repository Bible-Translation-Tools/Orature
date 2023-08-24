package org.wycliffeassociates.otter.common.device

import org.wycliffeassociates.otter.common.audio.AudioFileReader

interface AudioFileReaderProvider {
    fun getAudioFileReader(start: Int? = null, end: Int? = null): AudioFileReader
}