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

    fun getDefaultPluginData(): Maybe<AudioPluginData> {
        return pluginRepository.getDefaultPluginData()
    }

    fun setDefaultPluginData(default: AudioPluginData?): Completable {
        return pluginRepository.setDefaultPluginData(default)
    }
}