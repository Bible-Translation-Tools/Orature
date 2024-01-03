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
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import java.io.File
import javax.inject.Inject

const val EN_ULB_FILENAME = "en_ulb"
private const val EN_ULB_PATH = "content/$EN_ULB_FILENAME.zip"

class InitializeUlb @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val installedEntityRepo: IInstalledEntityRepository,
    private val importer: ImportProjectUseCase
) : Installable {

    override val name = "EN_ULB"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeUlb::class.java)

    override fun exec(progressEmitter: ObservableEmitter<ProgressStatus>): Completable {
        return Completable
            .fromCallable {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    val enUlbFile = prepareImportFile()
                    if (importer.isAlreadyImported(enUlbFile)) {
                        log.info("$EN_ULB_FILENAME already exists, skipped.")
                        return@fromCallable Completable.complete()
                    }

                    log.info("Initializing $name version: $version...")
                    progressEmitter.onNext(
                        ProgressStatus(
                            titleKey = "initializingSources",
                            subTitleKey = "loadingSomething",
                            subTitleMessage = name
                        )
                    )
                    val callback = setupImportCallback(progressEmitter)
                    importer
                        .import(enUlbFile, callback)
                        .toObservable()
                        .doOnError { e ->
                            log.error("Error importing $EN_ULB_FILENAME.", e)
                        }
                        .blockingSubscribe { result ->
                            if (result == ImportResult.SUCCESS) {
                                installedEntityRepo.install(this)
                                log.info("$name version: $version installed!")
                            } else {
                                throw ImportException(result)
                            }
                        }
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
            .doOnError { e ->
                log.error("Error in initializeUlb", e)
            }
    }

    private fun prepareImportFile(): File {
        val enUlbResource = javaClass.classLoader.getResource(EN_ULB_PATH)!!
        val tempFile = directoryProvider.createTempFile("en_ulb-default", ".zip")
            .also(File::deleteOnExit)

        enUlbResource.openStream().use { input ->
            tempFile.outputStream().use { output ->
                input.transferTo(output)
            }
        }
        return tempFile
    }
}
