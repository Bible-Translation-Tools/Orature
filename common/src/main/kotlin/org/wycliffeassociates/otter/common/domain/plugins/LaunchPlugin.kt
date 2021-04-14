package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class LaunchPlugin @Inject constructor() {
    enum class Result {
        SUCCESS,
        NO_PLUGIN
    }

    fun launchPlugin(plugin: IAudioPlugin, file: File, pluginParameters: PluginParameters): Single<Result> {
        return plugin.launch(file, pluginParameters)
            .andThen(Maybe.just(Result.SUCCESS))
            .toSingle(Result.NO_PLUGIN)
    }
}
