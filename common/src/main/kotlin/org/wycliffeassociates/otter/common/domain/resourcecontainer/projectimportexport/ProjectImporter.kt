package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.zip.IZipFileReader
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import java.io.File
import java.io.IOException

class ProjectImporter(
    private val resourceContainerImporter: ImportResourceContainer,
    private val directoryProvider: IDirectoryProvider,
    private val collectionRepository: ICollectionRepository,
    private val languageRepository: ILanguageRepository
) {
    fun isInProgress(resourceContainer: File): Boolean {
        return try {
            resourceContainer.isFile && resourceContainer.extension == "zip" && hasInProgressMarker(resourceContainer)
        } catch (e: IOException) {
            false
        }
    }

    fun importInProgress(resourceContainer: File): Single<ImportResult> {
        val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }
        val metadata = languageRepository
            .getBySlug(manifest.dublinCore.language.identifier)
            .map { language ->
                manifest.dublinCore.mapToMetadata(resourceContainer, language)
            }

        return try {
            directoryProvider.newZipFileReader(resourceContainer).use { zipFileReader ->
                importSources(zipFileReader)
                    .andThen { createDerivedProject(metadata) }
                    .toSingleDefault(ImportResult.SUCCESS)
            }
        } catch (e: Exception) {
            Single.just(ImportResult.IMPORT_ERROR)
        }
    }

    private fun createDerivedProject(metadataSingle: Single<ResourceMetadata>): Single<Collection> {
        val metadata: ResourceMetadata by lazy { metadataSingle.blockingGet() }

        val sourceLookup = collectionRepository
            .getRootSources()
            .flattenAsObservable { it }
            .filter {
                it.resourceContainer?.run {
                    language.slug == metadata.language.slug && identifier == metadata.identifier
                } ?: false
            }
            .firstOrError()

        return sourceLookup.flatMap { sourceCollection ->
            CreateProject(collectionRepository).create(sourceCollection, metadata.language)
        }
    }

    private fun hasInProgressMarker(resourceContainer: File): Boolean {
        return directoryProvider.newZipFileReader(resourceContainer).use {
            it.exists(RcConstants.SELECTED_TAKES_FILE)
        }
    }

    private fun importSources(zipFileReader: IZipFileReader): Completable {
        return Completable.fromAction {
            val sourceFiles = zipFileReader
                .list(RcConstants.SOURCE_DIR)
                .filter { it.extension.toLowerCase() == "zip" }

            val firstTry = sourceFiles
                .map { importSource(it, zipFileReader) }
                .toMap()

            // If our first try results contain both an UNMATCHED_HELP and a SUCCESS, then a retry might help.
            if (firstTry.containsValue(ImportResult.SUCCESS)) {
                firstTry
                    .filter { (_, result) -> result == ImportResult.UNMATCHED_HELP }
                    .forEach { (file, _) -> importSource(file, zipFileReader) }
            }
        }
    }

    private fun importSource(fileInZip: File, zipFileReader: IZipFileReader): Pair<File, ImportResult> {
        val name = fileInZip.nameWithoutExtension
        val result = resourceContainerImporter
            .import(name, zipFileReader.stream(fileInZip.path))
            .blockingGet()
        // TODO: Log.info("Import source resource container $name result $result")
        return fileInZip to result
    }
}
