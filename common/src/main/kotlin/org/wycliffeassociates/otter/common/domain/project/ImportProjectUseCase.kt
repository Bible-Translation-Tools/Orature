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
package org.wycliffeassociates.otter.common.domain.project

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.project.importer.BurritoImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.TsImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Provider

const val SOURCES_JSON_FILE = "gl_sources.json"
const val SOURCE_PATH_TEMPLATE = "content/%s.zip"

class ImportProjectUseCase @Inject constructor() {

    @Inject
    lateinit var burritoFactoryProvider: BurritoImporterFactory

    @Inject
    lateinit var rcFactoryProvider: RCImporterFactory

    @Inject
    lateinit var tsFactoryProvider: TsImporterFactory

    @Inject
    lateinit var rcImporterProvider: Provider<OngoingProjectImporter>

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(IllegalArgumentException::class)
    fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions? = null
    ): Single<ImportResult> {
        return Single
            .fromCallable {
                val format = ProjectFormatIdentifier.getProjectFormat(file)
                getImporter(format)
            }
            .flatMap {
                it.import(file, callback, options)
            }
            .onErrorReturn {
                logger.error("Failed to import project file: $file. See exception detail below.", it)
                ImportResult.FAILED
            }
    }

    fun import(file: File): Single<ImportResult> {
        return import(file, null, null)
    }

    fun sideloadSource(language: Language): Completable {
        return Single
            .fromCallable {
                getEmbeddedSource(language)
            }
            .subscribeOn(Schedulers.io())
            .doOnError { logger.error("Failed to get embedded source file for ${language.slug}", it) }
            .flatMap { sourceFile ->
                import(sourceFile, null, null)
            }
            .ignoreElement()
    }

    private fun getEmbeddedSource(language: Language): File {
        val resourceName = glSources.find { it.languageCode == language.slug }?.name
        val pathToSource = SOURCE_PATH_TEMPLATE.format(resourceName)

        val sourceFile = javaClass.classLoader.getResource(pathToSource).openStream().use { input ->
            val tempFile = File.createTempFile(resourceName, ".zip", directoryProvider.tempDirectory)
            tempFile.outputStream().use { output ->
                input.transferTo(output)
            }
            tempFile
        }

        return sourceFile
    }

    fun isAlreadyImported(file: File): Boolean {
        return rcFactoryProvider
            .makeImporter()
            .isAlreadyImported(file)
    }

    fun isSourceAudioProject(file: File): Boolean {
        return directoryProvider.newFileReader(file).use {
            !it.exists(RcConstants.SELECTED_TAKES_FILE) && it.exists(RcConstants.SOURCE_MEDIA_DIR)
        }
    }

    fun getSourceMetadata(file: File): Maybe<ResourceMetadata> {
        return when (ProjectFormatIdentifier.getProjectFormat(file)) {
            ProjectFormat.RESOURCE_CONTAINER -> {
                rcImporterProvider.get().getSourceMetadata(file)
            }
            else -> Maybe.empty()
        }
    }

    /**
     * Get the corresponding importer based on the project format.
     */
    private fun getImporter(format: ProjectFormat): IProjectImporter {
        val factory: IProjectImporterFactory = when(format) {
            ProjectFormat.SCRIPTURE_BURRITO -> burritoFactoryProvider
            ProjectFormat.RESOURCE_CONTAINER -> rcFactoryProvider
            ProjectFormat.TSTUDIO -> tsFactoryProvider
            else -> throw Exception("Unsupported project format.")
        }
        return factory.makeImporter()
    }

    companion object {
        val glSources: List<ResourceInfoSerializable> by lazy {
            javaClass.classLoader.getResource(SOURCES_JSON_FILE)!!
                .openStream().use { stream ->
                    val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
                    val sources: List<ResourceInfoSerializable> = mapper.readValue(stream)
                    sources
                }
        }
    }
}

data class ResourceInfoSerializable(val name: String, val languageCode: String, val url: String)