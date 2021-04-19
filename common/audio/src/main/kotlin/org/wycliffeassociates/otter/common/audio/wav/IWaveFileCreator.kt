package org.wycliffeassociates.otter.common.audio.wav

import java.io.File

const val EMPTY_WAVE_FILE_SIZE = 44L

interface IWaveFileCreator {
    fun createEmpty(path: File)
}
