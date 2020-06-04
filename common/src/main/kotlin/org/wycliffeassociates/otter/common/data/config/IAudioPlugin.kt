package org.wycliffeassociates.otter.common.data.config

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.PluginParameters
import java.io.File

interface IAudioPlugin {
    fun isNativePlugin(): Boolean
    // Launch the plugin
    fun launch(audioFile: File, pluginParameters: PluginParameters): Completable
}