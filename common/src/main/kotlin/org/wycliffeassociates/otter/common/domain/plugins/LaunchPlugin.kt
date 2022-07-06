/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import java.io.File
import javax.inject.Inject

class LaunchPlugin @Inject constructor(
    private val pluginRepository: IAudioPluginRepository
) {
    enum class Result {
        SUCCESS,
        NO_PLUGIN
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun launchPlugin(type: PluginType, file: File, pluginParameters: PluginParameters): Single<Result> {
        logger.info("Launching plugin: ${type.name}")
        return pluginRepository
            .getPlugin(type)
            .flatMap {
                it.launch(file, pluginParameters).andThen(Maybe.just(Result.SUCCESS))
            }
            .toSingle(Result.NO_PLUGIN)
            .map {
                if (it == Result.NO_PLUGIN) {
                    logger.error("Plugin $type is unavailable")
                }
                it
            }
    }
}
