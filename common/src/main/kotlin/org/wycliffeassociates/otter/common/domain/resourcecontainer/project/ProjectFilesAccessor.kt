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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.toObservable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Contributor
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
import org.wycliffeassociates.resourcecontainer.ZipAccessor
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.io.OutputStream
import kotlin.io.path.outputStream
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm.getText
import org.wycliffeassociates.usfmtools.USFMParser
import org.wycliffeassociates.usfmtools.models.markers.CMarker
import org.wycliffeassociates.usfmtools.models.markers.VMarker


class ProjectFilesAccessor(
    directoryProvider: IDirectoryProvider,
    private val sourceMetadata: ResourceMetadata,
    private val targetMetadata: ResourceMetadata,
    private val project: Collection
) {

    constructor(
        directoryProvider: IDirectoryProvider,
        sourceMetadata: ResourceMetadata,
        targetMetadata: ResourceMetadata,
        project: Book
    ) : this(directoryProvider, sourceMetadata, targetMetadata, project.toCollection())

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

    val sourceAudioDir = directoryProvider.getProjectSourceAudioDirectory(
        sourceMetadata,
        targetMetadata,
        project.slug
    )

    val audioDir = directoryProvider.getProjectAudioDirectory(
        sourceMetadata,
        targetMetadata,
        project
    )

    fun copySourceFiles(
        linkedResource: ResourceMetadata? = null,
        excludeMedia: Boolean = true
    ) {
        val target = sourceDir.resolve(sourceMetadata.path.nameWithoutExtension + ".zip")
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
        if (sourceAudioDir.exists()) {
            sourceAudioDir.listFiles()?.forEach {
                fileWriter.copyFile(it, RcConstants.SOURCE_AUDIO_DIR)
            }
        }

        val sources = mutableListOf(sourceMetadata)
        linkedResource?.let { sources.add(it) }

        sources
            .map { it.path }
            .distinct()
            .forEach { fileWriter.copyFile(it, RcConstants.SOURCE_DIR) }
    }


    fun initializeResourceContainerInDir(overwrite: Boolean = true) {
        if (!overwrite) { // if existing container is valid, then use it
            try {
                ResourceContainer.load(projectDir).close()
                return
            } catch (e: Exception) {
                log.error("Error in loading resource container $projectDir", e)
            }
        }

        ResourceContainer
            .create(projectDir) {
                val projectPath = "./${RcConstants.MEDIA_DIR}"
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
                val projectPath = "./${RcConstants.MEDIA_DIR}"
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

    fun createChunksFile() {
        val outFile = projectDir.resolve(RcConstants.CHUNKS_FILE)
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
                    _fileWriter.appendLine(it)
                }
        }
    }

    fun writeChunksFile(fileWriter: IFileWriter) {
        val inFile = projectDir.resolve(RcConstants.CHUNKS_FILE)

        fileWriter.bufferedWriter(RcConstants.CHUNKS_FILE).use { _fileWriter ->
            inFile.reader().use { _fileReader ->
                _fileReader.transferTo(_fileWriter)
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
        fileWriter.copyDirectory(audioDir, RcConstants.MEDIA_DIR) {
            val normalized = File(it).invariantSeparatorsPath
            selectedChapters.contains(normalized)
        }
    }

    fun getContributorInfo(): List<Contributor> {
        return ResourceContainer.load(projectDir).use { rc ->
            rc.manifest.dublinCore.contributor.map { Contributor(it) }
        }
    }

    fun setContributorInfo(contributors: List<Contributor>) {
        ResourceContainer.load(projectDir).use { rc ->
            rc.manifest.dublinCore.contributor = contributors.map { it.toString() }.toMutableList()
            rc.writeManifest()
        }
    }

    fun getChapterText(projectSlug: String, chapterNumber: Int): List<String> {
        val chapterText = arrayListOf<String>()

        println(sourceMetadata.path)
        ResourceContainer.load(sourceMetadata.path).use { rc ->
            val projectEntry = rc.manifest.projects.find { it.identifier == projectSlug }
            projectEntry?.let {
                println(it.path)
                println(rc.accessor.fileExists(it.path.removePrefix("./")))
                val text = rc.accessor.getReader(it.path.removePrefix("./")).readText()
                val parser = USFMParser(arrayListOf("s5"))
                val doc = parser.parseFromString(text)
                val chapters = doc.getChildMarkers(CMarker::class.java)
                val chap = chapters.find { it.number == chapterNumber }
                chap?.let {
                    it.getChildMarkers(VMarker::class.java).forEach {
                        chapterText.add(it.getText())
                    }
                }
            }
        }

        return chapterText
    }

    fun getChunkText(
        projectSlug: String,
        chapterNumber: Int,
        startVerse: Int,
        endVerse: Int
    ): List<String> {
        val chapterText = arrayListOf<String>()

        ResourceContainer.load(sourceMetadata.path).use { rc ->
            val projectEntry = rc.manifest.projects.find { it.identifier == projectSlug }
            projectEntry?.let {
                println(it.path)
                println(rc.accessor.fileExists(it.path.removePrefix("./")))
                val text = rc.accessor.getReader(it.path.removePrefix("./")).readText()
                val parser = USFMParser(arrayListOf("s5"))
                val doc = parser.parseFromString(text)
                val chapters = doc.getChildMarkers(CMarker::class.java)
                val chap = chapters.find { it.number == chapterNumber }
                chap?.let {
                    for (i in startVerse..endVerse) {
                        val verse = it.getChildMarkers(VMarker::class.java).find { it.startingVerse == i }
                        verse?.let {
                            chapterText.add(it.getText())
                        }
                    }
                }
            }
        }

        return chapterText
    }

    private fun copySourceWithoutMedia(source: File, target: File) {
        if (!OratureFileFormat.isSupported(source.extension)) {
            return
        }

        val targetZip = ZipAccessor(target)
        ResourceContainer.load(source).use {
            val inMap = it.accessor.getInputStreams(".", listOf())
                .filterKeys {
                    File(it).extension !in ignoredSourceMediaExtensions
                }
            val filesToWrite = inMap.mapValues {
                { output: OutputStream ->
                    it.value.copyTo(output)
                    Unit
                }
            }
            try {
                targetZip.write(filesToWrite)
            } catch (e: Exception) {
                log.error("Error while copying source container to derived project.", e)
            }
        }

        // update media manifest
        ResourceContainer.load(target).use { rc ->
            rc.media?.projects = listOf()
            rc.writeMedia()
        }
    }

    fun selectedChapterFilePaths(workbook: Workbook, isBook: Boolean): Set<String> {
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
        println("got chapters")

        val bookElements: Observable<BookElement> = when {
            chaptersOnly -> chapters.cast()
            else -> chapters.concatMap { chapter ->
                chapter.chunks.values.toObservable().cast()
            }
        }
        println("got book elements")

        return bookElements
            .flatMap {
                getAudioForCurrentResource(it, isBook) }
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
            if (rc.accessor.fileExists(RcConstants.LICENSE_FILE)) {
                val license = kotlin.io.path.createTempFile(suffix = ".md")

                rc.accessor.getInputStream(RcConstants.LICENSE_FILE).use { input ->
                    license.outputStream().write(input.readAllBytes())
                }
                return license.toFile()
            }
        }
        return null
    }

    private fun isAudioFile(file: String) = isAudioFile(File(file))

    private fun isAudioFile(file: File) =
        file.extension.lowercase().let { it == "wav" || it == "mp3" }

    fun getChunkFile(): File {
        return projectDir.resolve(RcConstants.CHUNKS_FILE)
    }

    fun copyChunkFile(fileReader: IFileReader) {
        val outFile = projectDir.resolve(RcConstants.CHUNKS_FILE)
        if (!outFile.exists() && fileReader.exists(RcConstants.CHUNKS_FILE)) {
            fileReader.stream(RcConstants.CHUNKS_FILE).transferTo(outFile.outputStream())
        }
    }

    companion object {
        val ignoredSourceMediaExtensions = listOf("wav", "mp3", "jpg", "jpeg", "png", "cue")

        fun getTakesDirPath(): String {
            return RcConstants.TAKE_DIR
        }
    }
}
