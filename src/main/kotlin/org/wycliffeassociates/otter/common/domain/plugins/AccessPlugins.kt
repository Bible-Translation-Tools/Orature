package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class AccessPlugins(
    private val pluginRepository: IAudioPluginRepository
) {
    fun getAllPluginData(): Single<List<AudioPluginData>> {
        return pluginRepository.getAll()
    }

    fun getEditorData(): Maybe<AudioPluginData> {
        return pluginRepository.getEditorData()
    }

    fun setEditorData(default: AudioPluginData): Completable {
        return pluginRepository.setEditorData(default)
    }

    fun getRecorderData(): Maybe<AudioPluginData> {
        return pluginRepository.getRecorderData()
    }

    fun setRecorderData(default: AudioPluginData): Completable {
        return pluginRepository.setRecorderData(default)
    }

    fun delete(plugin: AudioPluginData): Completable {
        return pluginRepository.delete(plugin)
    }
}