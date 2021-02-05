package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.PluginParameters
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import java.io.File

class LaunchPlugin(
    private val pluginRepository: IAudioPluginRepository
) {
    enum class Result {
        SUCCESS,
        NO_PLUGIN
    }

    fun launchPlugin(type: PluginType, file: File, pluginParameters: PluginParameters): Single<Result> {
        return pluginRepository
            .getPlugin(type)
            .flatMap {
                it.launch(file, pluginParameters).andThen(Maybe.just(Result.SUCCESS))
            }
            .toSingle(Result.NO_PLUGIN)
    }
}
