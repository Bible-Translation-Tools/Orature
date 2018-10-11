package org.wycliffeassociates.otter.jvm.persistence

import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.jvm.device.audioplugin.AudioPlugin

object DefaultPluginPreference {
    var defaultPluginData: AudioPluginData? = null
    val defaultPlugin: IAudioPlugin?
        get() = defaultPluginData?.let { AudioPlugin(it) }
}