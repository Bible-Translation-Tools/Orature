package org.wycliffeassociates.otter.common.data.config

import io.reactivex.Completable
import java.io.File
import org.wycliffeassociates.otter.common.data.PluginParameters

interface IAudioPlugin {
    fun isNativePlugin(): Boolean
    // Launch the plugin
    fun launch(audioFile: File, pluginParameters: PluginParameters): Completable
}
