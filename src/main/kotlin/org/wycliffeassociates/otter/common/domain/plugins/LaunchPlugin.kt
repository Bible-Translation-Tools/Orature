package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import java.io.File

class LaunchPlugin(
        private val pluginRepository: IAudioPluginRepository
) {
    enum class Result {
        SUCCESS,
        NO_PLUGIN
    }

    fun launchRecorder(file: File): Single<Result> = pluginRepository
            .getRecorder()
            .flatMap {
                it.launch(file).andThen(Maybe.just(Result.SUCCESS))
            }
            .toSingle(Result.NO_PLUGIN)

    fun launchEditor(file: File): Single<Result> = pluginRepository
            .getEditor()
            .flatMap {
                it.launch(file).andThen(Maybe.just(Result.SUCCESS))
            }
            .toSingle(Result.NO_PLUGIN)
}