/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import io.reactivex.ObservableEmitter
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import java.io.File
import javax.inject.Inject

class InitializeSources @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepo: IResourceMetadataRepository,
    private val installedEntityRepo: IInstalledEntityRepository,
    private val rcImporterFactory: RCImporterFactory
): Installable {

    override val name = "SOURCES"
    override val version = 1

    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var callback: ProjectImporterCallback

    override fun exec(progressEmitter: ObservableEmitter<ProgressStatus>): Completable {
        return Completable
            .fromAction {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    logger.info("Initializing sources...")
                    progressEmitter.onNext(
                        ProgressStatus(titleKey = "initializingSources")
                    )
                    callback = setupImportCallback(progressEmitter)

                    migrate()

                    installedEntityRepo.install(this)
                    logger.info("$name version: $version installed!")
                } else {
                    logger.info("$name up to date with version: $version")
                }
            }
    }

    private fun migrate() {
        `migrate to version 1`()
    }

    private fun `migrate to version 1`() {
        importSources(directoryProvider.internalSourceRCDirectory)
    }

    private fun importSources(dir: File) {
        if (dir.isFile || !dir.exists()) {
            return
        }

        val existingPaths = fetchSourcePaths()

        dir.walk().filter {
            it.isFile && it !in existingPaths
        }.forEach {
            // Find resource containers to import
            if (it.extension in OratureFileFormat.extensionList) {
                importFile(it)
            }
        }
    }

    private fun fetchSourcePaths(): List<File> {
        return resourceMetadataRepo
            .getAllSources()
            .blockingGet()
            .map {
                it.path
            }
    }

    private fun importFile(file: File) {
        rcImporterFactory.makeImporter().import(file, callback).toObservable()
            .doOnError { e ->
                logger.error("Error importing $file.", e)
            }
            .blockingSubscribe {
                logger.info("${file.name} imported!")
            }
    }
}