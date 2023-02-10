package org.wycliffeassociates.otter.jvm.workbookapp.domain.project.importer

import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.MediaMerge
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class ExistingSourceImporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository
) : RCImporter() {

    @Inject
    lateinit var deleteProvider: Provider<DeleteResourceContainer>

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun import(
        file: File,
        options: ImportOptions,
        callback: ProjectImporterCallback
    ): Single<ImportResult> {
        val existingResource = findExistingResourceMetadata(file)
            ?: return super.import(file, options, callback)

        var isMatchingVersion: Boolean
        ResourceContainer.load(file).use { rc ->
            isMatchingVersion = (rc.manifest.dublinCore.version == existingResource.version)
        }

        return if (isMatchingVersion)  {
            mergeMedia(file, existingResource.path)
        } else {
            // existing resource has a different version, select which one to keep/delete
            /*
            val response = callback.onRequestUserInput()
            deleteProvider.get().delete(response)
            */
            super.import(file, options, callback)
        }
    }

    private fun mergeMedia(
        newRC: File,
        existingRC: File
    ): Single<ImportResult> {
        logger.info("RC already imported, merging media")
        return Single
            .fromCallable {
                MediaMerge(
                    directoryProvider,
                    ResourceContainer.load(newRC),
                    ResourceContainer.load(existingRC)
                ).merge()
                ImportResult.SUCCESS
            }
            .onErrorReturn {
                ImportResult.IMPORT_ERROR
            }
    }

    private fun findExistingResourceMetadata(file: File): ResourceMetadata? {
        ResourceContainer.load(file, true).use { rc ->
            val dublinCore = rc.manifest.dublinCore
            return resourceMetadataRepository.getAllSources().blockingGet()
                .find {
                    it.language.slug == dublinCore.language.identifier &&
                            it.identifier == dublinCore.identifier
                }
        }
    }

    /**
     * Attempts to delete the existing RC before
     * importing the new one if they have different versions.
     * Returns false if it could not delete the existing RC.
     */
    private fun tryUpdateExistingRC(newFile: File): Boolean {
        ResourceContainer.load(newFile).use { newRc ->
            val dublinCore = newRc.manifest.dublinCore
            resourceMetadataRepository.getAllSources().blockingGet()
                .find {
                    it.language.slug == dublinCore.language.identifier &&
                            it.identifier == dublinCore.identifier
                }?.let { existingRc ->
                    var isDeleted = false

                    // delete if matching rc has different version
                    if (existingRc.version != newRc.manifest.dublinCore.version) {
                        logger.info("Existing RC has different version, updating...")

                        val result = deleteProvider.get().delete(existingRc.path).blockingGet()
                        if (result == DeleteResult.SUCCESS) {
                            isDeleted = true
                            logger.info("Removed old RC successfully!")
                        } else {
                            isDeleted = false
                            logger.error(
                                "Failed to update RC " +
                                        "${existingRc.language.slug}-${existingRc.identifier}: ${result.name}."
                            )
                        }
                    }

                    return isDeleted
                } ?: return true
        }
    }
}