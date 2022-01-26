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
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class InitializeMarker @Inject constructor(
    val directoryProvider: IDirectoryProvider,
    val pluginRepository: IAudioPluginRepository,
    val installedEntityRepo: IInstalledEntityRepository,
    val preferences: IAppPreferences
) : Installable {

    override val name = "MARKER"
    override val version = 16

    val log = LoggerFactory.getLogger(InitializeMarker::class.java)

    override fun exec(): Completable {
        return Completable
            .fromCallable {
                var installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (true) {
                    log.info("Initializing $name version: $version...")
                    importOtterMarker()
                        .doOnComplete {
                            installedEntityRepo.install(this)
                            log.info("Marker imported!")
                            log.info("$name version: $version installed!")
                        }
                        .doOnError { e ->
                            log.error("Error importing marker.", e)
                        }
                        .blockingAwait()
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
    }

    private fun importOtterMarker(): Completable {
        val pluginsDir = directoryProvider.audioPluginDirectory
        val jar = File(pluginsDir, "OratureMarker.jar")
        ClassLoader.getSystemResourceAsStream("plugins/jars/markerapp")
            ?.transferTo(FileOutputStream(jar))
        return pluginRepository.insert(
            AudioPluginData(
                0,
                "OratureMarker",
                "$version.0.0",
                canEdit = false,
                canRecord = false,
                canMark = true,
                executable = jar.absolutePath,
                args = listOf(),
                pluginFile = null
            )
        ).doAfterSuccess { id: Int ->
            preferences.setPluginId(PluginType.MARKER, id).blockingGet()
        }.ignoreElement()
    }
}
