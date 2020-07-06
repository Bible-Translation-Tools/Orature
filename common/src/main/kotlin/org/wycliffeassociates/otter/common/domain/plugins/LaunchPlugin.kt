package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File
import org.wycliffeassociates.otter.common.data.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class LaunchPlugin(
    private val pluginRepository: IAudioPluginRepository
) {
    enum class Result {
        SUCCESS,
        NO_PLUGIN
    }

    fun launchRecorder(file: File, pluginParameters: PluginParameters): Single<Result> = pluginRepository
        .getRecorder()
        .flatMap {
            it.launch(file, pluginParameters).andThen(Maybe.just(Result.SUCCESS))
        }
        .toSingle(Result.NO_PLUGIN)

    fun launchEditor(file: File, pluginParameters: PluginParameters): Single<Result> = pluginRepository
        .getEditor()
        .flatMap {
            it.launch(file, pluginParameters).andThen(Maybe.just(Result.SUCCESS))
        }
        .toSingle(Result.NO_PLUGIN)
}
