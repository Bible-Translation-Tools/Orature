/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.BookElement
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.RcConstants
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.buildManifest
import org.wycliffeassociates.otter.common.io.zip.IFileReader
import org.wycliffeassociates.otter.common.io.zip.IFileWriter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.outputStream
import net.lingala.zip4j.ZipFile as Zip4J

class ProjectFilesAccessor(
    directoryProvider: IDirectoryProvider,
    private val sourceMetadata: ResourceMetadata,
    private val targetMetadata: ResourceMetadata,
    private val project: Collection
) {
    private val log = LoggerFactory.getLogger(ProjectFilesAccessor::class.java)

    val projectDir = directoryProvider.getProjectDirectory(
        sourceMetadata,
        targetMetadata,
        project
    )

    val sourceDir = directoryProvider.getProjectSourceDirectory(
        sourceMetadata,
        targetMetadata,
        project
    )

    val audioDir = directoryProvider.getProjectAudioDirectory(
        sourceMetadata,
        targetMetadata,
        project
    )

    companion object {
        fun getTakesDirPath(): String {
            return RcConstants.TAKE_DIR
        }
    }

    fun copySourceFiles(
        linkedResource: ResourceMetadata? = null,
        excludeMedia: Boolean = true
    ) {
        val target = sourceDir.resolve(sourceMetadata.path.name)
        if (!target.exists()) {
            if (excludeMedia) {
                copySourceWithoutMedia(sourceMetadata.path, target)
            } else {
                sourceMetadata.path.copyTo(target)
            }
        }

        // Copy linked resource
        linkedResource?.let {
            val targetRc = sourceDir.resolve(it.path.name)
            if (!targetRc.exists()) {
                it.path.copyTo(targetRc)
            }
        }
    }

    fun copySourceFiles(fileReader: IFileReader) {
        val sourceFiles: Sequence<String> = fileReader
            .list(RcConstants.SOURCE_DIR)
            .filter {
                val ext = it.substringAfterLast(".")
                OratureFileFormat.isSupported(ext)
            }

        sourceFiles.forEach { path ->
            val inFile = File(path)
            val outFile = sourceDir.resolve(inFile.name)

            if (!outFile.exists()) {
                val stream = fileReader.stream(path)
                stream.transferTo(outFile.outputStream())
            }
        }
    }

    fun copySourceFiles(fileWriter: IFileWriter, linkedResource: ResourceMetadata? = null) {
        val sources = mutableListOf(sourceMetadata)
        linkedResource?.let { sources.add(it) }

        sources
            .map { it.path }
            .distinct()
            .forEach { fileWriter.copyFile(it, RcConstants.SOURCE_DIR) }
    }

    fun initializeResourceContainerInDir() {
        ResourceContainer
            .create(projectDir) {
                val projectPath = "./${RcConstants.PROJECT_CONTENT_DIR}"
                manifest = buildManifest(targetMetadata, sourceMetadata, project, projectPath)
                getLicense(sourceMetadata.path)?.let {
                    addFileToContainer(it, RcConstants.LICENSE_FILE)
                    it.delete()
                }
            }
            .use {
                it.write()
            }
    }

    fun initializeResourceContainerInFile(workbook: Workbook, container: File) {
        ResourceContainer
            .create(container) {
                val projectPath = "./${RcConstants.PROJECT_CONTENT_DIR}"
                manifest = buildManifest(targetMetadata, workbook, projectPath)

                getLicense(workbook.source.resourceMetadata.path)?.let {
                    addFileToContainer(it, RcConstants.LICENSE_FILE)
                    it.delete()
                }
            }
            .use {
                it.write()
            }
    }

    fun createSelectedTakesFile() {
        val outFile = projectDir.resolve(RcConstants.SELECTED_TAKES_FILE)
        outFile.createNewFile()
    }

    fun copySelectedTakesFile(fileReader: IFileReader) {
        val outFile = projectDir.resolve(RcConstants.SELECTED_TAKES_FILE)
        if (!outFile.exists()) {
            fileReader.stream(RcConstants.SELECTED_TAKES_FILE).transferTo(outFile.outputStream())
        }
    }

    fun writeSelectedTakesFile(workbook: Workbook, isBook: Boolean) {
        val selectedTakes = projectDir.resolve(RcConstants.SELECTED_TAKES_FILE)
        selectedTakes.outputStream().use { stream ->
            fetchSelectedTakes(workbook, isBook)
                .map(::relativeTakePath)
                .doOnError { e ->
                    log.error("Error in updateSelectedMarker", e)
                }
                .blockingSubscribe {
                    stream.write("$it\n".toByteArray())
                }
        }
    }

    fun writeSelectedTakesFile(fileWriter: IFileWriter, workbook: Workbook, isBook: Boolean) {
        fileWriter.bufferedWriter(RcConstants.SELECTED_TAKES_FILE).use { _fileWriter ->
            fetchSelectedTakes(workbook, isBook)
                .map(::relativeTakePath)
                .doOnError { e ->
                    log.error("Error in writeSelectedTakesFile", e)
                }
                .blockingSubscribe {
                    _fileWriter.appendln(it)
                }
        }
    }

    fun copyTakeFiles(fileReader: IFileReader, manifestProject: Project): Observable<String> {
        return Observable.just(RcConstants.TAKE_DIR, manifestProject.path)
            .filter(fileReader::exists)
            .flatMap { audioDirInRc ->
                val normalized = File(audioDirInRc).normalize().path
                fileReader.copyDirectory(normalized, audioDir, this::isAudioFile)
            }
    }

    fun copyTakeFiles(
        fileWriter: IFileWriter,
        workbook: Workbook,
        workbookRepository: IWorkbookRepository,
        isBook: Boolean
    ) {
        val selectedChapters = selectedChapterFilePaths(workbook, isBook)
        val deletedTakes = deletedTakeFilePaths(workbook, workbookRepository)
        fileWriter.copyDirectory(audioDir, RcConstants.TAKE_DIR) {
            val normalized = File(it).invariantSeparatorsPath
            !selectedChapters.contains(normalized) && !deletedTakes.contains(normalized)
        }
        fileWriter.copyDirectory(audioDir, RcConstants.PROJECT_CONTENT_DIR) {
            val normalized = File(it).invariantSeparatorsPath
            selectedChapters.contains(normalized)
        }
    }

    private fun copySourceWithoutMedia(source: File, target: File) {
        val ext = source.path.substringAfterLast(".")
        if (!OratureFileFormat.isSupported(ext)) {
            return
        }
        val extractedDir = createTempDirectory("otter").toFile()
        Zip4J(source).extractAll(extractedDir.path)

        // exclude media of unrelated projects
        extractedDir.walk().firstOrNull {
            it.isDirectory && it.name == RcConstants.SOURCE_MEDIA_DIR
        }?.let { mediaDir ->
            mediaDir.listFiles().forEach { subDir ->
                if (subDir.isDirectory && subDir.name != project.slug) {
                    subDir.deleteRecursively()
                }
            }
        }

        if (extractedDir.list().size == 1) {
            Zip4J(target).addFolder(extractedDir.listFiles().first())
        } else {
            Zip4J(target).addFiles(extractedDir.listFiles().toList())
        }

        // update media manifest
        ResourceContainer.load(target).use { rc ->
            val mediaProjectList = rc.media?.projects?.firstOrNull {
                it.identifier == project.slug
            }?.let { mediaProject ->
                listOf(mediaProject)
            } ?: listOf()

            rc.media?.projects = mediaProjectList
            rc.writeMedia()
        }
        extractedDir.deleteRecursively()
    }

    private fun selectedChapterFilePaths(workbook: Workbook, isBook: Boolean): Set<String> {
        return fetchSelectedTakes(workbook, isBook, true)
            .map(this::relativeTakePath)
            .collectInto(hashSetOf<String>(), { set, path -> set.add(path) })
            .blockingGet()
    }

    private fun fetchSelectedTakes(
        workbook: Workbook,
        isBook: Boolean,
        chaptersOnly: Boolean = false
    ): Observable<Take> {
        val chapters = when {
            isBook -> workbook.target.chapters
            // Work around a quirk that records resource takes to the source tree
            else -> Observable.concat(workbook.source.chapters, workbook.target.chapters)
        }
        val bookElements: Observable<BookElement> = when {
            chaptersOnly -> chapters.cast()
            else -> chapters.concatMap { chapter -> chapter.children.startWith(chapter) }
        }
        return bookElements
            .flatMap { getAudioForCurrentResource(it, isBook) }
            .mapNotNull { audio -> audio.selected.value?.value }
    }

    private fun deletedTakeFilePaths(workbook: Workbook, workbookRepository: IWorkbookRepository): List<String> {
        val deletedTakes = workbookRepository
            .getSoftDeletedTakes(workbook.source)
            .blockingGet()
            .map { relativeTakePath(it.path) }
        val targetTakes = workbookRepository
            .getSoftDeletedTakes(workbook.target)
            .blockingGet()
            .map { relativeTakePath(it.path) }
        deletedTakes.toMutableList().addAll(targetTakes)
        return deletedTakes
    }

    private fun getAudioForCurrentResource(
        bookElement: BookElement,
        isBook: Boolean
    ): Observable<AssociatedAudio> {
        return when (isBook) {
            true -> Observable.just(bookElement.audio)
            false -> {
                val resourceGroup = bookElement.resources
                    .firstOrNull { it.metadata.identifier == targetMetadata.identifier }

                resourceGroup?.let { _resourceGroup ->
                    _resourceGroup.resources.flatMapIterable { resource ->
                        listOfNotNull(resource.title.audio, resource.body?.audio)
                    }
                } ?: Observable.empty()
            }
        }
    }

    private fun relativeTakePath(take: Take): String {
        return relativeTakePath(take.file)
    }

    private fun relativeTakePath(file: File): String {
        val relativeFile = file.relativeToOrSelf(audioDir)
        return relativeFile.invariantSeparatorsPath
    }

    private fun getLicense(sourceContainer: File): File? {
        ResourceContainer.load(sourceContainer).use { rc ->
            if (rc.accessor.fileExists(RcConstants.LICENSE_FILE)){
                val license = kotlin.io.path.createTempFile(suffix = ".md")

                rc.accessor.getInputStream(RcConstants.LICENSE_FILE).use{ input ->
                    license.outputStream().write(input.readAllBytes())
                }
                return license.toFile()
            }
        }
        return null
    }

    private fun isAudioFile(file: String) = isAudioFile(File(file))

    private fun isAudioFile(file: File) =
        file.extension.toLowerCase().let { it == "wav" || it == "mp3" }
}
