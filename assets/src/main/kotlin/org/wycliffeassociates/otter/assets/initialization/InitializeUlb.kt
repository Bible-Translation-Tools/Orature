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

const val SOURCES_JSON_FILE = "gl_sources.json"
private const val SOURCE_PATH_TEMPLATE = "content/%s.zip"

class InitializeUlb @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val installedEntityRepo: IInstalledEntityRepository,
    private val importer: ImportProjectUseCase
) : Installable {

    override val name = "GL_SOURCES"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeUlb::class.java)

    override fun exec(progressEmitter: ObservableEmitter<ProgressStatus>): Completable {
        val callback = setupImportCallback(progressEmitter)

        return Completable
            .fromAction {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")
                    progressEmitter.onNext(
                        ProgressStatus(titleKey = "initializingSources")
                    )
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

    private fun installGLSources(callback: ProjectImporterCallback) {
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
        val resources: List<ResourceInfoSerializable> = mapper.readValue(sourcesJson)

        return resources.mapNotNull { res ->
            val resourcePath = SOURCE_PATH_TEMPLATE.format(res.name)
            javaClass.classLoader.getResource(resourcePath)
                ?.let { resource ->
                    val fileToImport = copyResourceToFile(resource)
                    fileToImport
                }
        }
    }

    /**
     * Source files in jar must be copied to a temp file before it can be accessed
     */
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

data class ResourceInfoSerializable(val name: String, val url: String)