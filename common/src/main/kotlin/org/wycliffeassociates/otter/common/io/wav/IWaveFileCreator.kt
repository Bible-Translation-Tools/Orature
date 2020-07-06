package org.wycliffeassociates.otter.common.io.wav

import java.io.File

const val EMPTY_WAVE_FILE_SIZE = 44L

interface IWaveFileCreator {
    fun createEmpty(path: File)
}
