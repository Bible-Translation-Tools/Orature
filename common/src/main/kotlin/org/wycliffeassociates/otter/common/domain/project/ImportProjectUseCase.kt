package org.wycliffeassociates.otter.common.domain.project

import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Provider

class ImportProjectUseCase
    @Inject
    constructor() {
        @Inject
        lateinit var rcFactoryProvider: Provider<RCImporterFactory>

        @Inject
        lateinit var rcImporterProvider: Provider<OngoingProjectImporter>

        @Inject
        lateinit var directoryProvider: IDirectoryProvider

        private val logger = LoggerFactory.getLogger(javaClass)

        @Throws(
            IllegalArgumentException::class,
            InvalidResourceContainerException::class,
        )
        fun import(
            file: File,
            callback: ProjectImporterCallback?,
            options: ImportOptions? = null,
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

        fun isAlreadyImported(file: File): Boolean {
            return rcFactoryProvider.get()
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
        /*
            If we support 2+ formats, uncomment this
            val factory = when (format) { ... }
         */
            val factory: IProjectImporterFactory = rcFactoryProvider.get()
            return factory.makeImporter()
        }
    }
