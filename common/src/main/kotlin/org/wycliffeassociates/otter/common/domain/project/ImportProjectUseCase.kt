package org.wycliffeassociates.otter.common.domain.project

import io.reactivex.Maybe
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.io.File
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Provider

class ImportProjectUseCase @Inject constructor(
    val collectionRepo: ICollectionRepository
) {

    @Inject
    lateinit var rcFactoryProvider: Provider<RCImporterFactory>

    private val logger = LoggerFactory.getLogger(javaClass)

    @Throws(
        IllegalArgumentException::class,
        InvalidResourceContainerException::class
    )
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

    fun isAlreadyImported(file: File): Boolean {
        return rcFactoryProvider.get()
            .makeImporter()
            .isAlreadyImported(file)
    }

    fun getSourceMetadata(resourceContainer: File): Maybe<ResourceMetadata> {
        val manifest = try {
            ResourceContainer.load(resourceContainer).use { it.manifest }
        } catch(e: Exception) {
            return Maybe.empty()
        }

        val manifestSources = manifest.dublinCore.source.toSet()
        val manifestProject = manifest.projects.single()

        return Maybe.fromCallable {
            collectionRepo.getSourceProjects().blockingGet()
                .filter { it.slug == manifestProject.identifier }
                .mapNotNull { it.resourceContainer }
                .firstOrNull {
                    Source(it.identifier, it.language.slug, it.version) in manifestSources
                }
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