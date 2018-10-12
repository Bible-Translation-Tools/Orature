package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class PluginActions(
        val pluginRepository: IAudioPluginRepository
) {
    fun getAllPluginData(): Single<List<AudioPluginData>> {
        return pluginRepository.getAll()
    }

    fun getDefaultPluginData(): Maybe<AudioPluginData> {
        return pluginRepository.getDefaultPluginData()
    }

    fun getDefaultPlugin(): Maybe<IAudioPlugin> {
        return pluginRepository.getDefaultPlugin()
    }

    fun setDefaultPluginData(default: AudioPluginData?): Completable {
        return pluginRepository.setDefaultPluginData(default)
    }

    fun initializeDefault(): Completable {
        return pluginRepository
                .getAll()
                .flatMapCompletable {
                    pluginRepository.setDefaultPluginData(it.firstOrNull())
                }
    }
}