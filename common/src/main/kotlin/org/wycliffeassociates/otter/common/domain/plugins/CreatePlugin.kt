package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Maybe
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class CreatePlugin(
    private val pluginRepository: IAudioPluginRepository
) {
    fun create(data: AudioPluginData): Maybe<Int> {
        return pluginRepository
            .insert(data)
            .flatMapMaybe {
                Maybe.just(it)
            }
    }
}
