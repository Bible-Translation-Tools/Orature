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
package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.MediaMerge
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.MergeTextContent
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ExistingSourceImporter @Inject constructor(
    directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val resourceContainerRepository: IResourceContainerRepository,
    private val zipEntryTreeBuilder: IZipEntryTreeBuilder,
    private val deleteUseCase: DeleteResourceContainer,
    private val importUseCase: ImportProjectUseCase
) : RCImporter(directoryProvider, resourceMetadataRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var languageName = ""

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        val existingSource = findExistingResourceMetadata(file)
            ?: return super.passToNextImporter(file, callback, options)

        var sameVersion: Boolean
        var sameVersification: Boolean

        try {
            ResourceContainer.load(file).use { rc ->
                sameVersion = (rc.manifest.dublinCore.version == existingSource.version)
                ResourceContainer.load(existingSource.path).use { existingRC ->
                    languageName = rc.manifest.dublinCore.language.title
                    val sourceVersification = rc.manifest.projects.firstOrNull()?.versification
                    val existingVersification = existingRC.manifest.projects.firstOrNull()?.versification
                    sameVersification = sourceVersification == existingVersification
                }
            }
        } catch (e: IOException) {
            logger.error("Error loading RC", e)
            return Single.just(ImportResult.LOAD_RC_ERROR)
        }

        val singleStream = if (sameVersion || sameVersification) {
            callback?.onNotifyProgress(localizeKey = "mergingSource", percent = 30.0)

            logger.info("RC ${file.name} already imported, updating source...")
            updateSource(existingSource, file).blockingGet()

            callback?.onNotifyProgress(localizeKey = "importing_source_audio", percent = 50.0)

            mergeMedia(file, existingSource.path)
                .flatMap {
                    if (it == ImportResult.SUCCESS) {
                        callback?.onNotifyProgress(localizeKey = "importing_source_text", percent = 90.0)
                        mergeText(file, existingSource.path)
                    } else {
                        Single.just(it)
                    }
                }
        } else {
            // existing resource has a different version, confirms overwrite/delete
            callback?.onNotifyProgress(localizeKey = "overridingSource", percent = 15.0)
            logger.info("RC ${file.name} already imported, but with a different version and different versification.")
            logger.info("Requesting user input to overwrite/delete existing source...")
            val confirmDelete = callback?.onRequestUserInput()
                ?.blockingGet()
                ?.confirmed ?: true

            when {
                !confirmDelete -> {
                    logger.info("User chose to abort import.")
                    Single.just(ImportResult.ABORTED)
                }

                deleteUseCase.deleteSync(existingSource.path) == DeleteResult.SUCCESS -> {
                    // re-import the file after deleting the existing source
                    logger.info("Deleted existing source, re-importing ${file.name}...")
                    importUseCase.import(file)
                }

                else -> {
                    logger.info("User chose to delete existing source, but delete failed.")
                    Single.just(ImportResult.DEPENDENCY_CONSTRAINT)
                }
            }
        }

        return singleStream
            .doOnSuccess { result ->
                notifyCallback(result, callback, file)
            }
    }

    private fun notifyCallback(
        result: ImportResult,
        callback: ProjectImporterCallback?,
        file: File
    ) {
        if (result == ImportResult.SUCCESS) {
            callback?.onNotifySuccess(language = languageName)
        } else {
            callback?.onError(file.name)
        }
    }

    private fun updateSource(metadata: ResourceMetadata, file: File): Single<ImportResult> {
        return Single
            .fromCallable {
                try {
                    ResourceContainer.load(file).use { rc ->
                        val tree = try {
                            IProjectReader.constructContainerTree(rc, zipEntryTreeBuilder)
                        } catch (e: ImportException) {
                            logger.error("Error constructing container tree, file: $file", e)
                            return@fromCallable ImportResult.FAILED
                        }
                        return@fromCallable resourceContainerRepository.updateContent(
                            rc,
                            tree
                        ).map {
                            if (it == ImportResult.SUCCESS) {
                                resourceMetadataRepository.update(metadata, rc).blockingAwait()
                            }
                            it
                        }.map {
                            if (it == ImportResult.SUCCESS) {
                                return@map updateCollections(rc, tree).blockingGet()
                            }
                            it
                        }.blockingGet()
                    }
                } catch (e: IOException) {
                    logger.error("Error loading RC", e)
                    return@fromCallable ImportResult.LOAD_RC_ERROR
                }
            }
    }

    fun mergeMedia(
        newRC: File,
        existingRC: File,
    ): Single<ImportResult> {
        logger.info("RC already imported, merging media...")
        return Single
            .fromCallable {
                MediaMerge.merge(
                    ResourceContainer.load(newRC),
                    ResourceContainer.load(existingRC),
                )
                logger.info("Merge media completed.")
                ImportResult.SUCCESS
            }
            .onErrorReturn {
                logger.error("Merge media failed!", it)
                ImportResult.FAILED
            }
    }

    fun mergeText(
        newRC: File,
        existingRC: File
    ): Single<ImportResult> {
        logger.info("RC already imported, merging text...")
        return Single
            .fromCallable {
                MergeTextContent.merge(
                    ResourceContainer.load(newRC),
                    ResourceContainer.load(existingRC)
                )
                logger.info("Merge text completed.")
                ImportResult.SUCCESS
            }
            .onErrorReturn {
                logger.error("Merge text failed!", it)
                ImportResult.FAILED
            }
    }

    private fun updateCollections(
        container: ResourceContainer,
        tree: OtterTree<CollectionOrContent>
    ): Single<ImportResult> {
        return resourceContainerRepository
            .updateCollectionTitles(
                container,
                tree
            )
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