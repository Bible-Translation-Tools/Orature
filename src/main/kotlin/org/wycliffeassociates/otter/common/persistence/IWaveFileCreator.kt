package org.wycliffeassociates.otter.common.persistence

import java.io.File

interface IWaveFileCreator {
    fun createEmpty(path: File)
}