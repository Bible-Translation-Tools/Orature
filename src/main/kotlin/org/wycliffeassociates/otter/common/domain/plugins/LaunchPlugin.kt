package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import java.io.File

class LaunchPlugin(
        private val pluginRepository: IAudioPluginRepository
) {
    fun launchDefaultPlugin(file: File): Completable = pluginRepository
                .getDefaultPlugin()
                .flatMapCompletable { it.launch(file) }
}