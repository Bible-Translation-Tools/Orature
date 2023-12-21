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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.toMap
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
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
import kotlin.io.path.createTempDirectory
import kotlin.io.path.outputStream
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.AudioMetadataFileFormat
import org.wycliffeassociates.otter.common.domain.project.InProgressNarrationFileFormat
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.domain.project.TakeCheckingStatusMap
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.narration.ChapterRepresentation
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

    fun getChapterAudioDir(workbook: Workbook, chapter: Chapter): File {
        val namer = FileNamer(
            bookSlug = workbook.target.slug,
            languageSlug = workbook.target.language.slug,
            chapterCount = workbook.target.chapters.count().blockingGet(),
            chapterTitle = chapter.title,
            chapterSort = chapter.sort,
            chunkCount = chapter.chunkCount.blockingGet().toLong(),
            contentType = ContentType.TEXT,
            rcSlug = if (workbook.source.language.slug == workbook.target.language.slug) {
                workbook.sourceMetadataSlug
            } else {
                FileNamer.DEFAULT_RC_SLUG
            }
        )
        val formattedChapterName = namer.formatChapterNumber()
        val chapterDir = audioDir.resolve(formattedChapterName).also {
            if (!it.exists()) it.mkdirs()
        }
        return chapterDir
    }

    fun getInProgressNarrationFiles(workbook: Workbook, chapter: Chapter): List<File> {
        val chapterDir = getChapterAudioDir(workbook, chapter)
        val chapterNarrationPath = RcConstants.CHAPTER_NARRATION_FILE.format(chapterDir.name)
        val activeVersesPath = RcConstants.ACTIVE_VERSES_FILE.format(chapterDir.name)

        val chapterNarrationFile = projectDir.resolve(chapterNarrationPath)
        val activeVersesFile = projectDir.resolve(activeVersesPath)

        return listOf(chapterNarrationFile, activeVersesFile)
    }

    fun getNarrationProgress(workbook: Workbook, chapter: Chapter): Double {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        chapterRepresentation.loadFromSerializedVerses()

        return chapterRepresentation.getCompletionProgress()
    }

    fun isInitialized(): Boolean {
        return try {
            ResourceContainer.load(projectDir).close()
            true
        } catch (_: Exception) {
            false
        }
    }

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
        val sourceDirectories = listOf(RcConstants.SOURCE_DIR, RcConstants.SOURCE_AUDIO_DIR)
        for (dir in sourceDirectories) {
            if (!fileReader.exists(dir)) {
                continue
            }
            val sourceFiles: Sequence<String> = fileReader
                .list(dir)
                .filter {
                    val ext = it.substringAfterLast(".")
                    when (dir) {
                        RcConstants.SOURCE_DIR -> OratureFileFormat.isSupported(ext)
                        RcConstants.SOURCE_AUDIO_DIR -> AudioFileFormat.isSupported (ext) || AudioMetadataFileFormat.isSupported(ext)
                        else -> false
                    }
                }

            val outDir = when (dir) {
                RcConstants.SOURCE_DIR -> sourceDir
                RcConstants.SOURCE_AUDIO_DIR -> sourceAudioDir
                else -> continue
            }
            sourceFiles.forEach { path ->
                val inFile = File(path)
                val outFile = outDir.resolve(inFile.name)

                if (!outFile.exists()) {
                    val stream = fileReader.stream(path)
                    stream.transferTo(outFile.outputStream())
                }
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
            .forEach {
                fileWriter.copyFile(it, RcConstants.SOURCE_DIR)
            }
    }

    /**
     * Copies the source files of the project containing project-related media only.
     *
     * @param fileWriter used to write to the project file.
     * @param tempDir a temporary directory used to dump source file before copying.
     * @param linkedResource the associated resource file to the project's source.
     */
    fun copySourceFilesWithRelatedMedia(
        fileWriter: IFileWriter,
        tempDir: File,
        linkedResource: ResourceMetadata? = null
    ) {
        val sources = listOfNotNull(sourceMetadata, linkedResource)
        /* generate a sub-temp directory to avoid dirty file
            being accidentally reused due to the same name */
        val sourceTempDir = createTempDirectory(tempDir.toPath(), "otter-export").toFile()

        sources
            .map { it.path }
            .distinct()
            .forEach { source ->
                // prepare source before copying into export file.
                val newSource = filterSourceFileToContainProjectRelatedMedia(source, sourceTempDir)
                fileWriter.copyFile(newSource, RcConstants.SOURCE_DIR)
            }

        fileWriter.copyDirectory(sourceAudioDir, RcConstants.SOURCE_AUDIO_DIR)
    }

    fun initializeResourceContainerInDir(overwrite: Boolean = true) {
        if (!overwrite) { // if existing container is valid, then use it
            try {
                ResourceContainer.load(projectDir).close()
                return
            } catch (_: Exception) {
                log.info("Unable to load resource container at $projectDir. Creating a new one...")
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

    fun updateSelectedTakesFile(workbook: Workbook): Completable {
        return Completable
            .fromCallable {
                writeSelectedTakesFile(workbook, true)
            }
            .subscribeOn(Schedulers.io())
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

    fun writeSelectedTakesFile(
        fileWriter: IFileWriter,
        workbook: Workbook,
        isBook: Boolean,
        takeFilter: (String) -> Boolean = { true }
    ) {
        fileWriter.bufferedWriter(RcConstants.SELECTED_TAKES_FILE).use { _fileWriter ->
            fetchSelectedTakes(workbook, isBook, filter = takeFilter)
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

        if (inFile.exists()) {
            fileWriter.bufferedWriter(RcConstants.CHUNKS_FILE).use { _fileWriter ->
                inFile.reader().use { _fileReader ->
                    _fileReader.transferTo(_fileWriter)
                }
            }
        }
    }

    fun writeTakeCheckingStatus(
        fileWriter: IFileWriter,
        workbook: Workbook,
        takeFilter: (String) -> Boolean =  { true }
    ): Completable {
        return fetchTakes(workbook)
            .filter { takeFilter(it.name) }
            .map { take ->
                val path = relativeTakePath(take)
                val checking = TakeCheckingState(
                    take.checkingState.value!!.status,
                    take.getSavedChecksum()
                )
                Pair(path, checking)
            }
            .toMap()
            .doOnSuccess { takeCheckingMap: TakeCheckingStatusMap ->
                writeTakeChecking(fileWriter, takeCheckingMap)
            }
            .ignoreElement()
    }

    fun copyTakeFiles(
        fileReader: IFileReader,
        manifestProject: Project,
        filter: (String) -> Boolean = { true }
    ): Observable<String> {
        return Observable.just(RcConstants.TAKE_DIR, manifestProject.path)
            .filter(fileReader::exists)
            .flatMap { audioDirInRc ->
                val normalized = File(audioDirInRc).normalize().path
                fileReader.copyDirectory(normalized, audioDir) {
                    isAudioFile(it) && filter(it)
                }
            }
    }

    fun copyTakeFiles(
        fileWriter: IFileWriter,
        workbook: Workbook,
        workbookRepository: IWorkbookRepository,
        isBook: Boolean,
        filter: (String) -> Boolean = { true }
    ) {
        val selectedChapters = selectedChapterFilePaths(workbook, isBook)
        val deletedTakes = deletedTakeFilePaths(workbook, workbookRepository)
        fileWriter.copyDirectory(audioDir, RcConstants.TAKE_DIR) {
            val normalized = File(it).invariantSeparatorsPath
            !selectedChapters.contains(normalized) && !deletedTakes.contains(normalized)
                    && filter(it)
        }
        fileWriter.copyDirectory(audioDir, RcConstants.MEDIA_DIR) {
            val normalized = File(it).invariantSeparatorsPath
            selectedChapters.contains(normalized) && filter(it)
        }
    }

    fun copyInProgressNarrationFiles(
        fileReader: IFileReader,
        manifestProject: Project
    ): Observable<String> {
        return Observable.just(RcConstants.TAKE_DIR, manifestProject.path)
            .filter(fileReader::exists)
            .flatMap { audioDirInRc ->
                val normalized = File(audioDirInRc).normalize().path
                fileReader.copyDirectory(normalized, audioDir) {
                    isInProgressNarrationFile(it)
                }
            }
    }

    fun copyInProgressNarrationFiles(
        fileWriter: IFileWriter,
        filter: (String) -> Boolean = { true }
    ) {
        fileWriter.copyDirectory(audioDir, RcConstants.TAKE_DIR) {
            filter(it)
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

    fun getChapterContent(projectSlug: String, chapterNumber: Int, showVerseNumber: Boolean = true): List<Content> {
        val chapterContent = arrayListOf<Content>()

        ResourceContainer.load(sourceMetadata.path).use { rc ->
            val projectEntry = rc.manifest.projects.find { it.identifier == projectSlug }
            projectEntry?.let {
                val text = rc.accessor.getReader(it.path.removePrefix("./")).readText()
                val parser = USFMParser(arrayListOf("s5"))
                val doc = parser.parseFromString(text)
                val chapters = doc.getChildMarkers(CMarker::class.java)
                val chap = chapters.find { it.number == chapterNumber }
                chap?.let {
                    it.getChildMarkers(VMarker::class.java).forEachIndexed { idx, vm ->
                        val text = when (showVerseNumber) {
                            true -> "${vm.verseNumber}. ${vm.getText()}"
                            false -> vm.getText()
                        }
                        val content = Content(
                            sort = chapterContent.size,
                            labelKey = ContentLabel.VERSE.value,
                            start = vm.startingVerse,
                            end = vm.endingVerse,
                            text = text,
                            bridged = false,
                            type = ContentType.TEXT,
                            format = "usfm",
                            draftNumber = 0
                        )
                        chapterContent.add(content)

                        // the rest of bridged verses should be marked bridged
                        for (i in vm.startingVerse+1..vm.endingVerse) {
                            chapterContent.add(
                                Content(
                                    sort = chapterContent.size,
                                    labelKey = ContentLabel.VERSE.value,
                                    start = i,
                                    end = vm.endingVerse,
                                    text = "",
                                    bridged = true,
                                    type = ContentType.TEXT,
                                    format = "usfm",
                                    draftNumber = 0
                                )
                            )
                        }
                    }
                }
            }
        }

        return chapterContent
    }

    fun getChapterText(projectSlug: String, chapterNumber: Int, showVerseNumber: Boolean = true): List<String> {
        val chapterText = arrayListOf<String>()

        ResourceContainer.load(sourceMetadata.path).use { rc ->
            val projectEntry = rc.manifest.projects.find { it.identifier == projectSlug }
            projectEntry?.let {
                val text = rc.accessor.getReader(it.path.removePrefix("./")).readText()
                val parser = USFMParser(arrayListOf("s5"))
                val doc = parser.parseFromString(text)
                val chapters = doc.getChildMarkers(CMarker::class.java)
                val chap = chapters.find { it.number == chapterNumber }
                chap?.let {
                    it.getChildMarkers(VMarker::class.java).forEach {
                        when (showVerseNumber) {
                            true -> chapterText.add("${it.verseNumber}. ${it.getText()}")
                            false -> chapterText.add(it.getText())
                        }
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
        val chunkText = arrayListOf<String>()

        ResourceContainer.load(sourceMetadata.path).use { rc ->
            val projectEntry = rc.manifest.projects.find { it.identifier == projectSlug }
            projectEntry?.let {
                val text = rc.accessor.getReader(it.path.removePrefix("./")).readText()
                val parser = USFMParser(arrayListOf("s5"))
                val doc = parser.parseFromString(text)
                val chapters = doc.getChildMarkers(CMarker::class.java)
                val chap = chapters.find { it.number == chapterNumber }
                chap?.let {
                    for (i in startVerse..endVerse) {
                        val verse = it.getChildMarkers(VMarker::class.java).find { it.startingVerse == i }
                        verse?.let {
                            chunkText.add("${it.verseNumber}. ${it.getText()}")
                        }
                    }
                }
            }
        }

        return chunkText
    }

    private fun fetchTakes(
        workbook: Workbook
    ): Observable<Take> {
        val chapters = workbook.target.chapters
        return chapters
            .flatMap { chapter ->
                chapter.chunks
                    .take(1)
                    .flatMapIterable { it }
                    .cast<BookElement>()
                    .startWith(chapter as BookElement)
                    .concatMap {
                        it.audio.getAllTakes().toObservable()
                    }
            }
    }

    private fun writeTakeChecking(
        fileWriter: IFileWriter,
        takeCheckingMap: TakeCheckingStatusMap
    ) {
        fileWriter.bufferedWriter(RcConstants.CHECKING_STATUS_FILE).use { writer ->
            val mapper = ObjectMapper(JsonFactory())
                .registerKotlinModule()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)

            mapper.writeValue(writer, takeCheckingMap)
        }
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

    /**
     * Returns a new file containing source media of the corresponding project only.
     */
    private fun filterSourceFileToContainProjectRelatedMedia(
        source: File,
        tempDir: File
    ): File {
        val newSourceFile = tempDir.resolve(source.nameWithoutExtension + ".zip")
        val newSourceZip = ZipAccessor(newSourceFile)

        ResourceContainer.load(source).use {
            val inMap = it.accessor.getInputStreams(".", listOf())
                .filterKeys { path ->
                    if (path.contains("${RcConstants.SOURCE_MEDIA_DIR}/")) {
                        path.contains("${RcConstants.SOURCE_MEDIA_DIR}/${project.slug}")
                    } else {
                        true
                    }
                }
            val filesToWrite = inMap.mapValues {
                { output: OutputStream ->
                    it.value.copyTo(output)
                    Unit
                }
            }
            try {
                newSourceZip.write(filesToWrite)
            } catch (e: Exception) {
                log.error("Error while copying source container to derived project.", e)
            } finally {
                newSourceZip.close()
            }
        }

        // filter media projects that are not related to the current project
        ResourceContainer.load(newSourceFile).use { rc ->
            val singleProjectList = listOfNotNull(
                rc.media?.projects?.find { it.identifier == project.slug }
            )
            rc.media?.projects = singleProjectList
            rc.writeMedia()
        }

        return newSourceFile
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
        chaptersOnly: Boolean = false,
        filter: (String) -> Boolean = { true }
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
            .mapNotNull { audio ->
                val take = audio.selected.value?.value
                take
            }
            .filter {
                filter(it.file.name)
            }
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
        return deletedTakes + targetTakes
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

    private fun isInProgressNarrationFile(file: String) = isInProgressNarrationFile(File(file))

    private fun isInProgressNarrationFile(file: File) =
        InProgressNarrationFileFormat.isSupported(file.extension)

    fun getChunkFile(): File {
        return projectDir.resolve(RcConstants.CHUNKS_FILE)
    }

    fun copyChunkFile(fileReader: IFileReader) {
        val outFile = projectDir.resolve(RcConstants.CHUNKS_FILE)
        if (!outFile.exists() && fileReader.exists(RcConstants.CHUNKS_FILE)) {
            fileReader.stream(RcConstants.CHUNKS_FILE).transferTo(outFile.outputStream())
        }
    }

    fun getProjectMode(): ProjectMode? {
        val file = projectDir.resolve(RcConstants.PROJECT_MODE_FILE)
        return if (file.exists() && file.length() > 0) {
            val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
            val serialized: SerializableProjectMode = mapper.readValue(file, object : TypeReference<SerializableProjectMode>() {})
            serialized.mode
        } else {
            null
        }
    }

    fun setProjectMode(mode: ProjectMode) {
        val file = projectDir.resolve(RcConstants.PROJECT_MODE_FILE)
        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        mapper.writeValue(file, SerializableProjectMode(mode))
    }

    fun copyProjectModeFile(fileWriter: IFileWriter) {
        val file = projectDir.resolve(RcConstants.PROJECT_MODE_FILE)
        fileWriter.bufferedWriter(RcConstants.PROJECT_MODE_FILE).use { writer ->
            file.bufferedReader().use { reader ->
                reader.transferTo(writer)
            }
        }
    }

    fun copyProjectModeFile(fileReader: IFileReader) {
        val modeFile = projectDir.resolve(RcConstants.PROJECT_MODE_FILE)
        if (fileReader.exists(RcConstants.PROJECT_MODE_FILE)) {
            fileReader.stream(RcConstants.PROJECT_MODE_FILE).transferTo(modeFile.outputStream())
        }
    }

    companion object {
        val ignoredSourceMediaExtensions = listOf("wav", "mp3", "jpg", "jpeg", "png", "cue")

        fun getTakesDirPath(): String {
            return RcConstants.TAKE_DIR
        }
    }
}
