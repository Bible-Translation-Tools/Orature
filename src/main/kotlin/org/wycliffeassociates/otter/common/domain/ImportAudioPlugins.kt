package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import java.io.File

class ImportAudioPlugins(private val pluginRegistrar: IAudioPluginRegistrar) {
    fun import(pluginFile: File): Completable {
        return pluginRegistrar.import(pluginFile)
    }
    fun importAll(pluginDir: File): Completable {
        return pluginRegistrar.importAll(pluginDir)
    }
}