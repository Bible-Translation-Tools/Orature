package org.wycliffeassociates.otter.common.data.config

import io.reactivex.Completable
import java.io.File

interface IAudioPlugin {
    // Launch the plugin
    fun launch(audioFile: File): Completable
}