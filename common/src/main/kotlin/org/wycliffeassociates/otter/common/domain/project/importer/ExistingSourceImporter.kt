package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.MediaMerge
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import javax.inject.Inject

class ExistingSourceImporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val deleteUseCase: DeleteResourceContainer
) : RCImporter(directoryProvider, resourceMetadataRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        val existingSource = findExistingResourceMetadata(file)
            ?: return super.passToNextImporter(file, callback, options)

        var sameVersion: Boolean
        ResourceContainer.load(file).use { rc ->
            sameVersion = (rc.manifest.dublinCore.version == existingSource.version)
        }

        return if (sameVersion) {
            mergeMedia(file, existingSource.path)
        } else {
            // existing resource has a different version, confirms overwrite/delete
            /*
            val confirmDelete = callback.onRequestUserInput()
            */
            val result = deleteUseCase.deleteSync(existingSource.path)
            if (result != DeleteResult.SUCCESS) {
                Single.just(ImportResult.FAILED)
            } else {
                super.passToNextImporter(file, callback, options)
            }
        }
    }

    private fun mergeMedia(
        newRC: File,
        existingRC: File
    ): Single<ImportResult> {
        logger.info("RC already imported, merging media...")
        return Single
            .fromCallable {
                MediaMerge(
                    directoryProvider,
                    ResourceContainer.load(newRC),
                    ResourceContainer.load(existingRC)
                ).merge()
                logger.info("Merge media completed.")
                ImportResult.SUCCESS
            }
            .onErrorReturn {
                logger.error("Merge media failed!", it)
                ImportResult.IMPORT_ERROR
            }
    }

    private fun findExistingResourceMetadata(file: File): ResourceMetadata? {
        ResourceContainer.load(file, true).use { rc ->
            val dublinCore = rc.manifest.dublinCore
            return resourceMetadataRepository.getAllSources()
                .blockingGet()
                .find {
                    it.language.slug == dublinCore.language.identifier &&
                            it.identifier == dublinCore.identifier
                }
        }
    }
}