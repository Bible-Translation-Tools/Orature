package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import java.io.File

interface IAudioPlugin {
    fun isNativePlugin(): Boolean
    // Quit the plugin
    fun quit()
    // Launch the plugin
    fun launch(audioFile: File, pluginParameters: PluginParameters): Completable
}
