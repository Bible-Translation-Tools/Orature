package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import java.io.File

class LaunchPlugin(
        private val pluginRepository: IAudioPluginRepository
) {
    fun launchRecorder(file: File): Completable = pluginRepository
                .getRecorder()
                .flatMapCompletable { it.launch(file) }

    fun launchEditor(file: File): Completable = pluginRepository
            .getEditor()
            .flatMapCompletable { it.launch(file) }
}