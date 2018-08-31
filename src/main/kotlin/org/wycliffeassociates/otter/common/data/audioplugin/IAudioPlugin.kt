package org.wycliffeassociates.otter.common.data.audioplugin

import io.reactivex.Completable
import java.io.File

interface IAudioPlugin {
    // Launch the plugin
    fun launch(file: File): Completable
}