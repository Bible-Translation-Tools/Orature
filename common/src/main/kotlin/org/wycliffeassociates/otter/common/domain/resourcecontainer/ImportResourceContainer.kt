package org.wycliffeassociates.otter.common.domain.resourcecontainer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.CollectionOrContent
import org.wycliffeassociates.otter.common.data.model.MimeType
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
            collectionRepository,
            contentRepository,
            takeRepository,
            languageRepository
        )

        return when {
            projectImporter.isResumableProject(file) -> projectImporter.importResumableProject(file)
            file.isDirectory -> importContainerDirectory(file)
            file.extension == "zip" -> importContainerZipFile(file)
            else -> Single.just(ImportResult.INVALID_RC)
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

    private fun File.contains(name: String): Boolean {
        if (!this.isDirectory) {
            throw Exception("Cannot call contains on non-directory file")
        }
        return this.listFiles().map { it.name }.contains(name)
    }

    private fun importContainerZipFile(file: File): Single<ImportResult> {
        return Single.fromCallable {
            validateRcZip(file) // throws

            val internalDir = getInternalDirectory(file)
                ?: throw ImportException(ImportResult.LOAD_RC_ERROR)

            if (internalDir.exists() && internalDir.contains(file.name)) {
                // Collision on disk: Can't import the resource container
                // Assumes that filesystem internal workbookapp directory and database are in sync
                throw ImportException(ImportResult.ALREADY_EXISTS)
            }

            // Copy to the internal directory
            val newZipFile = copyFileToInternalDirectory(file, internalDir)

            importFromInternalDir(newZipFile, internalDir).blockingGet()
        }.onErrorReturn { e ->
            e.castOrFindImportException()?.result ?: throw e
        }.subscribeOn(Schedulers.io())
    }

    private fun importContainerDirectory(directory: File) =
        Single
            .just(directory)
            .flatMap { containerDir ->
                // Is this a valid resource container
                if (!validateRcDir(containerDir)) return@flatMap Single.just(ImportResult.INVALID_RC)

                val internalDir = getInternalDirectory(containerDir)
                    ?: return@flatMap Single.just(ImportResult.LOAD_RC_ERROR)
                if (internalDir.exists() && internalDir.listFiles().isNotEmpty()) {
                    // Collision on disk: Can't import the resource container
                    // Assumes that filesystem internal workbookapp directory and database are in sync
                    return@flatMap Single.just(ImportResult.ALREADY_EXISTS)
                }

                // Copy to the internal directory
                val newDirectory = copyRecursivelyToInternalDirectory(containerDir, internalDir)

                return@flatMap importFromInternalDir(newDirectory, newDirectory)
            }
            .subscribeOn(Schedulers.io())

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

    private fun importFromInternalDir(fileToLoad: File, newDir: File): Single<ImportResult> {
        // Load the internal container
        val container = try {
            ResourceContainer.load(fileToLoad, OtterResourceContainerConfig())
        } catch (e: Exception) {
            return cleanUp(newDir, ImportResult.LOAD_RC_ERROR)
        }

        val tree = try {
            constructContainerTree(container)
        } catch (e: ImportException) {
            return cleanUp(newDir, e.result)
        }

        return resourceContainerRepository
            .importResourceContainer(container, tree, container.manifest.dublinCore.language.identifier)
            .doOnEvent { result, err ->
                if (result != ImportResult.SUCCESS || err != null) newDir.deleteRecursively()
            }
            .subscribeOn(Schedulers.io())
    }

    private fun cleanUp(containerDir: File, result: ImportResult): Single<ImportResult> = Single.fromCallable {
        containerDir.deleteRecursively()
        return@fromCallable result
    }

    private fun validateRcDir(dir: File): Boolean = dir.contains("manifest.yaml")

    /** Throws appropriately if RC zip is invalid, otherwise returns true. */
    private fun validateRcZip(zip: File): Boolean = ResourceContainer.load(zip, true).use { true }

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

    private fun makeExpandedContainer(container: ResourceContainer): ImportResult {
        val dublinCore = container.manifest.dublinCore
        if (dublinCore.type == "bundle" && MimeType.of(dublinCore.format) == MimeType.USFM) {
            return if (container.expandUSFMBundle()) ImportResult.SUCCESS else ImportResult.INVALID_CONTENT
        }
        return ImportResult.SUCCESS
    }

    /** @throws ImportException */
    private fun constructContainerTree(container: ResourceContainer): OtterTree<CollectionOrContent> {
        val projectReader = IProjectReader.build(
            format = container.manifest.dublinCore.format,
            isHelp = container.manifest.dublinCore.type == "help"
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