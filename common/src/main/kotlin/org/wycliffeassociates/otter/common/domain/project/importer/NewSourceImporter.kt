package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.OtterResourceContainerConfig
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.otterConfigCategories
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.VersificationTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.IOException
import javax.inject.Inject

class NewSourceImporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceContainerRepository: IResourceContainerRepository,
    resourceMetadataRepository: IResourceMetadataRepository,
    private val versificationRepository: IVersificationRepository,
    private val zipEntryTreeBuilder: IZipEntryTreeBuilder
) : RCImporter(directoryProvider, resourceMetadataRepository) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        return importContainer(file)
    }

    fun importContainer(file: File): Single<ImportResult> {
        return Single.fromCallable {
            var exists = false
            logger.info("Importing RC...")
            val internalDir = getInternalDirectory(file) ?: throw ImportException(ImportResult.LOAD_RC_ERROR)
            if (internalDir.exists()) {
                val rcFileExists = file.isFile && internalDir.contains(file.name)
                val rcDirExists = file.isDirectory && internalDir.listFiles().isNotEmpty()
                if (rcFileExists || rcDirExists) {
                    exists = true
                }
            }
            val rcToImport = if (exists) {
                file
            } else {
                copyToInternalDirectory(file, internalDir)
            }
            importFromInternalDir(rcToImport).blockingGet()
        }.onErrorReturn { e ->
            logger.error("Error in importContainer, file: $file", e)
            e.castOrFindImportException()?.result ?: throw e
        }.subscribeOn(Schedulers.io())
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

    private fun cleanUp(container: File, result: ImportResult): Single<ImportResult> = Single.fromCallable {
        container.deleteRecursively()
        return@fromCallable result
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

        val root = OtterTree<CollectionOrContent>(container.toCollection())
        val versificationTree = VersificationTreeBuilder(directoryProvider, versificationRepository).build(container)
        for (node in versificationTree) {
            root.addChild(node)
        }

        return resourceContainerRepository
            .importResourceContainer(container, root, container.manifest.dublinCore.language.identifier)
            .doOnEvent { result, err ->
                if (err != null) {
                    logger.error("Error in importFromInternalDirectory importing rc, file: $fileToLoad", err)
                }
                if (result != ImportResult.SUCCESS || err != null) fileToLoad.deleteRecursively()
            }.flatMap {
                updateContentFromTextContent(container, tree)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun updateContentFromTextContent(
        container: ResourceContainer,
        tree: OtterTree<CollectionOrContent>
    ): Single<ImportResult> {
        return resourceContainerRepository
            .updateContent(
                container,
                tree,
                container.manifest.dublinCore.language.identifier
            )
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

    private fun File.contains(name: String): Boolean {
        if (!this.isDirectory) {
            throw Exception("Cannot call contains on non-directory file")
        }
        return this.listFiles().map { it.name }.contains(name)
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
}