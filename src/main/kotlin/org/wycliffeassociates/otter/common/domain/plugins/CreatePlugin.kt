package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class CreatePlugin(
        private val pluginRepository: IAudioPluginRepository
) {
    fun create(data: AudioPluginData): Completable {
        return pluginRepository
                .insert(data)
                .toCompletable()
    }
}