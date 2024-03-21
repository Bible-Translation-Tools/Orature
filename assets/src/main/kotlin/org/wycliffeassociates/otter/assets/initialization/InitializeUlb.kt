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

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import java.io.File
import java.net.URL
import javax.inject.Inject

const val EN_ULB_FILENAME = "en_ulb"
private const val SOURCES_JSON_FILE = "gl_sources.json"
private const val SOURCE_PATH_TEMPLATE = "content/%s.zip"
private const val EN_ULB_PATH = "content/$EN_ULB_FILENAME.zip"

class InitializeUlb @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val installedEntityRepo: IInstalledEntityRepository,
    private val importer: ImportProjectUseCase
) : Installable {

    override val name = "EN_ULB"
    override val version = 2

    private val log = LoggerFactory.getLogger(InitializeUlb::class.java)

    override fun exec(progressEmitter: ObservableEmitter<ProgressStatus>): Completable {
        val callback = setupImportCallback(progressEmitter)

        return Completable
            .fromAction {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")
                    installEnULB(progressEmitter, callback)
                    installGLSources(callback)
                    installedEntityRepo.install(this)
                    log.info("$name version: $version installed!")
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
            .doOnError { e ->
                log.error("Error in initializeUlb", e)
            }
    }

    private fun installEnULB(
        progressEmitter: ObservableEmitter<ProgressStatus>,
        callback: ProjectImporterCallback
    ) {
        val enUlbFile = prepareImportFile()
        if (importer.isAlreadyImported(enUlbFile)) {
            log.info("$EN_ULB_FILENAME already exists, skipped.")
        } else {
            progressEmitter.onNext(
                ProgressStatus(
                    titleKey = "initializingSources",
                    subTitleKey = "loadingSomething",
                    subTitleMessage = name
                )
            )
            importer
                .import(enUlbFile, callback)
                .toObservable()
                .doOnError { e ->
                    log.error("Error importing $EN_ULB_FILENAME.", e)
                }
                .blockingSubscribe { result ->
                    if (result == ImportResult.SUCCESS) {
                        installedEntityRepo.install(this)
                    } else {
                        throw ImportException(result)
                    }
                }
        }
    }

    fun installGLSources(callback: ProjectImporterCallback) {
        getSourcesToPreload()
            .filter { !importer.isAlreadyImported(it) }
            .forEach { file ->
                val result = importer
                    .import(file, callback)
                    .doOnError { e ->
                        log.error("Error while preloading source: $file", e)
                    }
                    .blockingGet()

                if (result == ImportResult.SUCCESS) {
                    log.info("Source loaded: $file")
                } else {
                    log.error("Could not import $file")
                }
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

    private fun getSourcesToPreload(): List<File> {
        val sourcesJson = javaClass.classLoader.getResource(SOURCES_JSON_FILE)
            ?: return listOf()

        val sourcesJsonFile = directoryProvider.createTempFile("gl_sources", ".json")
            .also(File::deleteOnExit)

        sourcesJson.openStream().use { input ->
            sourcesJsonFile.outputStream().use { output ->
                input.transferTo(output)
            }
        }

        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        val resources: List<ResourceInfo> = mapper.readValue(sourcesJson)

        return resources.mapNotNull { res ->
            val resourcePath = SOURCE_PATH_TEMPLATE.format(res.name)
            javaClass.classLoader.getResource(resourcePath)
                ?.let { resource ->
                    val fileToImport = copyResourceToFile(resource)
                    fileToImport
                }
        }
    }

    private fun copyResourceToFile(resource: URL): File {
        val fileToImport = directoryProvider.tempDirectory.resolve(File(resource.file).name)
            .apply {
                createNewFile()
                deleteOnExit()
            }

        resource.openStream().use { input ->
            fileToImport.outputStream().use { output ->
                input.transferTo(output)
            }
        }
        return fileToImport
    }
}

private data class ResourceInfo(val name: String, val url: String)