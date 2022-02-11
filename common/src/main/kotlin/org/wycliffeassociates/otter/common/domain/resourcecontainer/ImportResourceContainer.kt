/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.resourcecontainer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.MediaMerge
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectImporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Provider

class ImportResourceContainer @Inject constructor(
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val resourceContainerRepository: IResourceContainerRepository,
    private val languageRepository: ILanguageRepository,
    private val directoryProvider: IDirectoryProvider,
    private val zipEntryTreeBuilder: IZipEntryTreeBuilder
) {
    private val logger = LoggerFactory.getLogger(ImportResourceContainer::class.java)

    @Inject lateinit var importProvider: Provider<ProjectImporter>
    @Inject lateinit var deleteProvider: Provider<DeleteResourceContainer>

    fun import(file: File): Single<ImportResult> {
        logger.info("Importing resource container: $file")

        val rcFile = if (file.isDirectory) {
            val zip = createTempFile(file.name, "zip")
            directoryProvider.newFileWriter(zip).use { fileWriter ->
                fileWriter.copyDirectory(file, "/")
            }
            zip
        } else {
            file
        }

        val projectImporter = importProvider.get()
        val isValid = validateRc(rcFile)
        val isResumable = isValid && projectImporter.isResumableProject(rcFile)
        val canMergeMedia = isValid && isAlreadyImported(rcFile)

        return when {
            !isValid -> {
                logger.error("Import failed, $rcFile is an invalid RC")
                return Single.just(ImportResult.INVALID_RC)
            }
            isResumable -> {
                logger.info("Importing rc as a resumable project")
                projectImporter.importResumableProject(rcFile)
            }
            canMergeMedia -> {
                logger.info("RC already imported, merging media")
                Single.fromCallable {
                    val existingRC = getExistingMetadata(rcFile)
                    MediaMerge(
                        directoryProvider,
                        ResourceContainer.load(rcFile),
                        ResourceContainer.load(existingRC.path)
                    ).merge()
                    ImportResult.SUCCESS
                }
            }
            tryUpdateExistingRC(rcFile) -> {
                importContainer(rcFile)
            }
            else -> Single.just(ImportResult.DEPENDENCY_ERROR)
        }
    }

    fun import(filename: String, stream: InputStream): Single<ImportResult> {
        val outFile = createTempFile(filename, "zip")

        return Single
            .fromCallable {
                stream.transferTo(outFile.outputStream())
            }
            .flatMap {
                import(outFile)
            }
            .doOnError { e ->
                logger.error("Error in import, filename: $filename", e)
            }
            .doFinally {
                outFile.parentFile.deleteRecursively()
            }
            .subscribeOn(Schedulers.io())
    }

    private fun validateRc(rc: File): Boolean {
        return try {
            ResourceContainer.load(rc, true).use { true }
        } catch (e: Exception) {
            logger.error("Error in validateRc: $rc", e)
            false
        }
    }

    private fun isAlreadyImported(file: File): Boolean {
        val rc = ResourceContainer.load(file, true)
        val language = languageRepository.getBySlug(rc.manifest.dublinCore.language.identifier).blockingGet()
        val resourceMetadata = rc.manifest.dublinCore.mapToMetadata(file, language)
        rc.close()
        return resourceMetadataRepository.exists(resourceMetadata).blockingGet()
    }

    private fun getExistingMetadata(file: File): ResourceMetadata {
        val rc = ResourceContainer.load(file, true)
        val language = languageRepository.getBySlug(rc.manifest.dublinCore.language.identifier).blockingGet()
        val resourceMetadata = rc.manifest.dublinCore.mapToMetadata(file, language)
        rc.close()
        return resourceMetadataRepository.get(resourceMetadata).blockingGet()
    }

    private fun importContainer(file: File): Single<ImportResult> {
        return Single.fromCallable {
            logger.info("Importing RC...")
            val internalDir = getInternalDirectory(file) ?: throw ImportException(ImportResult.LOAD_RC_ERROR)
            if (internalDir.exists()) {
                val rcFileExists = file.isFile && internalDir.contains(file.name)
                val rcDirExists = file.isDirectory && internalDir.listFiles().isNotEmpty()
                if (rcFileExists || rcDirExists) {
                    throw ImportException(ImportResult.ALREADY_EXISTS)
                }
            }
            val copiedRc = copyToInternalDirectory(file, internalDir)
            importFromInternalDir(copiedRc).blockingGet()
        }.onErrorReturn { e ->
            logger.error("Error in importContainer, file: $file", e)
            e.castOrFindImportException()?.result ?: throw e
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Attempts to delete the existing RC before
     * importing the new one if they have different versions.
     * Returns false if it could not delete the existing RC.
     */
    private fun tryUpdateExistingRC(rcFile: File): Boolean {
        ResourceContainer.load(rcFile).use { rc ->
            val dublinCore = rc.manifest.dublinCore
            resourceMetadataRepository.getAllSources().blockingGet()
                .find {
                    it.language.slug == dublinCore.language.identifier &&
                            it.identifier == dublinCore.identifier
                }?.let { existingRc ->
                    var isDeleted = false

                    // delete if matching rc has different version
                    if (existingRc.version != rc.manifest.dublinCore.version) {
                        logger.info("Existing RC has different version, updating...")
                        val result = deleteProvider.get().delete(rc).blockingGet()
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

    private fun File.contains(name: String): Boolean {
        if (!this.isDirectory) {
            throw Exception("Cannot call contains on non-directory file")
        }
        return this.listFiles().map { it.name }.contains(name)
    }

    private fun getInternalDirectory(file: File): File? {
        // Load the external container to get the metadata we need to figure out where to copy to
        val extContainer = try {
            ResourceContainer.load(file, OtterResourceContainerConfig())
        } catch (e: Exception) {
            // Could be checked or unchecked exception from RC library
            logger.error("Error in getInternalDirectory, file: $file", e)
            return null
        }
        return directoryProvider.getSourceContainerDirectory(extContainer)
    }

    private fun importFromInternalDir(fileToLoad: File): Single<ImportResult> {
        // Load the internal container
        val container = try {
            ResourceContainer.load(fileToLoad, OtterResourceContainerConfig())
        } catch (e: Exception) {
            logger.error("Error loading rc in importFromInternalDir, file: $fileToLoad", e)
            return cleanUp(fileToLoad, ImportResult.LOAD_RC_ERROR)
        }

        val tree = try {
            constructContainerTree(container)
        } catch (e: ImportException) {
            logger.error("Error constructing container tree, file: $fileToLoad", e)
            container.close()
            return cleanUp(fileToLoad, e.result)
        }

        return resourceContainerRepository
            .importResourceContainer(container, tree, container.manifest.dublinCore.language.identifier)
            .doOnEvent { result, err ->
                if (err != null) {
                    logger.error("Error in importFromInternalDirectory importing rc, file: $fileToLoad", err)
                }
                if (result != ImportResult.SUCCESS || err != null) fileToLoad.deleteRecursively()
            }
            .subscribeOn(Schedulers.io())
    }

    private fun cleanUp(container: File, result: ImportResult): Single<ImportResult> = Single.fromCallable {
        container.deleteRecursively()
        return@fromCallable result
    }

    private fun copyToInternalDirectory(file: File, destinationDirectory: File): File {
        return if (file.isDirectory) {
            copyRecursivelyToInternalDirectory(file, destinationDirectory)
        } else {
            copyFileToInternalDirectory(file, destinationDirectory)
        }
    }

    private fun copyRecursivelyToInternalDirectory(filepath: File, destinationDirectory: File): File {
        // Copy the resource container into the correct directory
        if (filepath.absoluteFile != destinationDirectory) {
            val success = filepath.copyRecursively(destinationDirectory, true)
            if (!success) {
                throw IOException("Could not copy resource container ${filepath.name} to resource container directory")
            }
        }
        return destinationDirectory
    }

    private fun copyFileToInternalDirectory(filepath: File, destinationDirectory: File): File {
        // Copy the resource container zip file into the correct directory
        val destinationFile = File(destinationDirectory, filepath.name)
        if (filepath.absoluteFile != destinationFile) {
            filepath.copyTo(destinationFile, true)
            val success = destinationDirectory.contains(filepath.name)
            if (!success) {
                throw IOException("Could not copy resource container ${filepath.name} to resource container directory")
            }
        }
        return destinationFile
    }

    /** @throws ImportException */
    private fun constructContainerTree(container: ResourceContainer): OtterTree<CollectionOrContent> {
        val projectReader = try {
            IProjectReader.build(
                format = container.manifest.dublinCore.format,
                isHelp = ContainerType.of(container.manifest.dublinCore.type) == ContainerType.Help
            )
        } catch (e: IllegalArgumentException) {
            logger.error("Error Importing project of type: ${container.manifest.dublinCore.format}", e)
            null
        } ?: throw ImportException(ImportResult.UNSUPPORTED_CONTENT)

        val root = OtterTree<CollectionOrContent>(container.toCollection())
        val categoryInfo = container.otterConfigCategories()
        for (project in container.manifest.projects) {
            var parent = root
            for (categorySlug in project.categories) {
                // use the `latest` RC spec to treat categories as hierarchical
                // look for a matching category under the parent
                val existingCategory = parent.children
                    .map { it as? OtterTree<CollectionOrContent> }
                    .filter { (it?.value as? Collection)?.slug == categorySlug }
                    .firstOrNull()
                parent = if (existingCategory != null) {
                    existingCategory
                } else {
                    // category node does not yet exist
                    val category = categoryInfo.filter { it.identifier == categorySlug }.firstOrNull() ?: continue
                    val categoryNode = OtterTree<CollectionOrContent>(category.toCollection())
                    parent.addChild(categoryNode)
                    categoryNode
                }
            }
            val projectTree = projectReader.constructProjectTree(container, project, zipEntryTreeBuilder)
            parent.addChild(projectTree)
        }
        return root
    }

    private fun createTempFile(name: String, extension: String): File {
        val tempDir = Files.createTempDirectory("orature_temp")
        val tempPath = tempDir.resolve("$name.$extension")
        val tempFile = tempPath.toFile()
        tempFile.deleteOnExit()
        return tempFile
    }
}
