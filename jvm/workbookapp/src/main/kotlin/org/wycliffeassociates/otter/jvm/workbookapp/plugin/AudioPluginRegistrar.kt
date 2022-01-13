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
package org.wycliffeassociates.otter.jvm.workbookapp.plugin

import org.wycliffeassociates.otter.jvm.workbookapp.plugin.parser.ParsedAudioPluginData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.parser.ParsedAudioPluginDataMapper
import java.io.File
import javax.inject.Inject

// Imports plugin data files into database
class AudioPluginRegistrar @Inject constructor(
    private val audioPluginRepository: IAudioPluginRepository
) : IAudioPluginRegistrar {
    private val logger = LoggerFactory.getLogger(AudioPluginRegistrar::class.java)

    // Configure Jackson YAML processor
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    override fun import(pluginFile: File): Completable {
        return Completable.fromSingle(
            Single
                .fromCallable {
                    val parsedAudioPlugin: ParsedAudioPluginData = mapper.readValue(pluginFile)
                    ParsedAudioPluginDataMapper().mapToAudioPluginData(parsedAudioPlugin, pluginFile)
                }
                .flatMap {
                    audioPluginRepository.insert(it)
                }
                .doOnError { e ->
                    logger.error("Error in import for pluginFile $pluginFile", e)
                }
        )
    }

    override fun importAll(pluginDir: File): Completable {
        val audioPluginCompletables = pluginDir
            .listFiles()
            .filter {
                // Only load yaml files
                it
                    .path
                    .lowercase()
                    .endsWith(".yaml")
            }
            .map {
                import(it)
            }
        return Completable.fromObservable(Observable
            .fromIterable(audioPluginCompletables)
            .flatMap {
                it
                    .toSingleDefault(true)
                    .onErrorReturnItem(false)
                    .toObservable()
            }
            .doOnError { e ->
                logger.error("Error in importAll for pluginDir: $pluginDir", e)
            }
        )
    }
}
