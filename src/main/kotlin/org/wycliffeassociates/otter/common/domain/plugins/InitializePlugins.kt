package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class InitializePlugins(
        private val pluginRepository: IAudioPluginRepository
) {
    fun initDefault(): Completable = pluginRepository
                .getAll()
                .flatMapCompletable {
                    pluginRepository.setDefaultPluginData(it.firstOrNull())
                }
}