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
package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Initializable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import java.io.File
import javax.inject.Inject

class InitializePlugins @Inject constructor(
    val directoryProvider: IDirectoryProvider,
    val audioPluginRegistrar: IAudioPluginRegistrar,
    val pluginRepository: IAudioPluginRepository
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializePlugins::class.java)

    override fun exec(): Completable {
        copyOcenaudioPlugin()

        // Always import new plugins
        return ImportAudioPlugins(audioPluginRegistrar, directoryProvider)
            .importAll()
            .andThen(pluginRepository.initSelected())
            .doOnError { e ->
                log.error("Error initializing plugins", e)
            }
            .doOnComplete {
                log.info("Plugins imported!")
            }
    }

    private fun copyOcenaudioPlugin() {
        if (!File(directoryProvider.audioPluginDirectory, "ocenaudio.yaml").exists()) {
            log.info("Copying ocenaudio plugin")
            ClassLoader.getSystemResourceAsStream("plugins/ocenaudio.yaml")
                .transferTo(
                    File(
                        directoryProvider.audioPluginDirectory.absolutePath,
                        "ocenaudio.yaml"
                    ).outputStream()
                )
        } else {
            log.info("Ocenaudio plugin not initialized but ocenaudio.yaml exists in plugins directory")
        }
    }
}
