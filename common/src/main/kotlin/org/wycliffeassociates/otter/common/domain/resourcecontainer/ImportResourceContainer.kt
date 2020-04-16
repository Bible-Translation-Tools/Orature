package org.wycliffeassociates.otter.common.domain.resourcecontainer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectImporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ImportResourceContainer(
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val resourceContainerRepository: IResourceContainerRepository,
    private val collectionRepository: ICollectionRepository,
    private val contentRepository: IContentRepository,
    private val takeRepository: ITakeRepository,
    private val languageRepository: ILanguageRepository,
    private val directoryProvider: IDirectoryProvider,
    private val zipEntryTreeBuilder: IZipEntryTreeBuilder
) {

    fun import(file: File): Single<ImportResult> {
        val projectImporter = ProjectImporter(
            this,
            directoryProvider,
            resourceMetadataRepository,
            collectionRepository,
            contentRepository,
            takeRepository,
            languageRepository
        )

        val valid = validateRc(file)
        if (!valid) {
            return Single.just(ImportResult.INVALID_RC)
        }

        val exists = isAlreadyImported(file)
        return if (exists) {
            Single.just(ImportResult.ALREADY_EXISTS)
        } else {
            val resumable = projectImporter.isResumableProject(file)
            if (resumable) {
                projectImporter.importResumableProject(file)
            } else {
                importContainer(file)
            }
        }
    }

    fun import(filename: String, stream: InputStream): Single<ImportResult> {
        val tempFile = File.createTempFile(filename, ".zip")
        return Single
            .fromCallable {
                stream.transferTo(FileOutputStream(tempFile))
            }
            .flatMap {
                import(tempFile)
            }
            .doFinally {
                tempFile.delete()
            }
            .subscribeOn(Schedulers.io())
    }

    private fun validateRc(rc: File): Boolean {
        return try {
            ResourceContainer.load(rc, true).use { true }
        } catch (e: Exception) {
            false
        }
    }

    private fun isAlreadyImported(file: File): Boolean {
        val rc = ResourceContainer.load(file, true)
        val language = languageRepository.getBySlug(rc.manifest.dublinCore.language.identifier).blockingGet()
        val resourceMetadata = rc.manifest.dublinCore.mapToMetadata(file, language)
        return resourceMetadataRepository.exists(resourceMetadata).blockingGet()
    }

    private fun importContainer(file: File): Single<ImportResult> {
        return Single.fromCallable {
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
            e.castOrFindImportException()?.result ?: throw e
        }.subscribeOn(Schedulers.io())
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
            e.printStackTrace()
            return null
        }
        return directoryProvider.getSourceContainerDirectory(extContainer)
    }

    private fun importFromInternalDir(fileToLoad: File): Single<ImportResult> {
        // Load the internal container
        val container = try {
            ResourceContainer.load(fileToLoad, OtterResourceContainerConfig())
        } catch (e: Exception) {
            return cleanUp(fileToLoad, ImportResult.LOAD_RC_ERROR)
        }

        val tree = try {
            constructContainerTree(container)
        } catch (e: ImportException) {
            return cleanUp(fileToLoad, e.result)
        }

        return resourceContainerRepository
            .importResourceContainer(container, tree, container.manifest.dublinCore.language.identifier)
            .doOnEvent { result, err ->
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
            copyToInternalDirectory(file, destinationDirectory)
        } else {
            copyFileToInternalDirectory(file, destinationDirectory)
        }
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

    private fun makeExpandedContainer(container: ResourceContainer): ImportResult {
        val dublinCore = container.manifest.dublinCore
        val containerType = ContainerType.of(dublinCore.type)
        val mimeType = MimeType.of(dublinCore.format)
        if (containerType == ContainerType.Bundle && mimeType == MimeType.USFM) {
            return if (container.expandUSFMBundle()) ImportResult.SUCCESS else ImportResult.INVALID_CONTENT
        }
        return ImportResult.SUCCESS
    }

    /** @throws ImportException */
    private fun constructContainerTree(container: ResourceContainer): OtterTree<CollectionOrContent> {
        val projectReader = IProjectReader.build(
            format = container.manifest.dublinCore.format,
            isHelp = ContainerType.of(container.manifest.dublinCore.type) == ContainerType.Help
        )
            ?: throw ImportException(ImportResult.UNSUPPORTED_CONTENT)

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
}