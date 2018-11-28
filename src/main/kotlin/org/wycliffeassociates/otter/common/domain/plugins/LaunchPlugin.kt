package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import java.io.File
import java.lang.RuntimeException

class LaunchPlugin(
        private val pluginRepository: IAudioPluginRepository
) {
    fun launchRecorder(file: File): Completable = pluginRepository
                .getRecorder()
                .doOnComplete { throw RuntimeException("No recorder") }
                .flatMapCompletable { it.launch(file) }

    fun launchEditor(file: File): Completable = pluginRepository
            .getEditor()
            .doOnComplete { throw RuntimeException("No editor") }
            .flatMapCompletable { it.launch(file) }
}