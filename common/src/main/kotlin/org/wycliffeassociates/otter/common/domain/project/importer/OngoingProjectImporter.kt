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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.Chunkification
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.primitives.SerializableProjectMode
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.takeFilenamePattern
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.project.ProjectAppVersion
import org.wycliffeassociates.otter.common.domain.project.TakeCheckingStatusMap
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.io.zip.IFileReader
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.common.utils.SELECTED_TAKES_FROM_DB
import org.wycliffeassociates.otter.common.utils.computeFileChecksum
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class OngoingProjectImporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val workbookRepository: IWorkbookRepository,
    private val workbookDescriptorRepository: IWorkbookDescriptorRepository,
    private val collectionRepository: ICollectionRepository,
    private val contentRepository: IContentRepository,
    private val takeRepository: ITakeRepository,
    private val languageRepository: ILanguageRepository,
    private val resourceRepository: IResourceRepository,
    private val createProjectUseCase: CreateProject,
    private val concatAudioUseCase: ConcatenateAudio
) : RCImporter(directoryProvider, resourceMetadataRepository) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val contentCache = mutableMapOf<ContentSignature, Content>()
    private var projectName = ""
    private var projectAppVersion = ProjectAppVersion.THREE
    private var projectMode: ProjectMode = ProjectMode.TRANSLATION
    private var takesInChapterFilter: Map<String, Int>? = null
    private var takesCheckingMap: TakeCheckingStatusMap = mapOf()
    private var completedChapters = listOf<Int>() // for Ot1 projects
    private var takesToCompile = mutableMapOf<Int, List<File>>() // for compiling verses of incomplete chapter in Ot1
    private var migratedSelectedTakes = listOf<String>() // list of all selected take paths extracted from Ot1 database

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        val isOngoingProject = isResumableProject(file)
        if (!isOngoingProject) {
            return super.passToNextImporter(file, callback, options)
        }
        projectName = ""
        takesInChapterFilter = null
        takesCheckingMap = mapOf()
        completedChapters = listOf()
        takesToCompile = mutableMapOf()
        migratedSelectedTakes = listOf()
        contentCache.clear()

        return Single
            .fromCallable { projectExists(file) }
            .doOnError {
                logger.error("Error while checking whether project already exists.", it)
            }
            .flatMap { exists ->
                val takesByChapterInProject = fetchTakesInRC(file)

                if (exists && callback != null) {
                    val availableChapters = takesByChapterInProject.values.distinct().sorted()
                    val selectedChapters = getUserSelectedChapter(availableChapters, callback)
                        ?: return@flatMap Single.just(ImportResult.ABORTED)

                    takesInChapterFilter = takesByChapterInProject.filterValues { it in selectedChapters }
                } else {
                    takesInChapterFilter = takesByChapterInProject // accept all takes
                }

                importResumableProject(file, callback)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun isResumableProject(rcFile: File): Boolean {
        return try {
            hasInProgressMarker(rcFile)
        } catch (e: IOException) {
            false
        }
    }

    private fun projectExists(file: File): Boolean {
        ResourceContainer.load(file).use { rc ->
            rc.manifest.dublinCore.let {
                val sourceLanguageSlug = it.source.firstOrNull()?.language
                    ?: return false

                val languageSlug = it.language.identifier
                val projectSlug = rc.manifest.projects.first().let { p ->
                    projectName = p.title
                    p.identifier
                }

                val projects = workbookRepository.getProjects().blockingGet()
                return projects.firstOrNull { existingProject ->
                    sourceLanguageSlug == existingProject.source.language.slug &&
                        languageSlug == existingProject.target.language.slug &&
                        projectSlug == existingProject.target.slug
                }?.let {
                    workbookRepository.closeWorkbook(it)
                    it.projectFilesAccessor.isInitialized()
                } ?: false
            }
        }
    }

    private fun getUserSelectedChapter(
        availableChapters: List<Int>,
        callback: ProjectImporterCallback
    ): List<Int>? {
        val callbackParam = ImportCallbackParameter(availableChapters, projectName)
        return callback.onRequestUserInput(callbackParam).blockingGet().chapters
    }

    private fun fetchTakesInRC(file: File): Map<String, Int> {
        ResourceContainer.load(file).use { rc ->
            val extensionFilter = AudioFileFormat.values().map { it.extension }
            val fileStreamMap = rc.accessor.getInputStreams(".", extensionFilter)
            try {
                val takesMap: Map<String, Int> = fileStreamMap.keys
                    .mapNotNull { path ->
                        parseNumbers(path)?.let {
                            Pair(path, it)
                        }
                    }
                    .associate {
                        Pair(it.first, it.second.contentSignature.chapter)
                    }

                return takesMap
            } finally {
                fileStreamMap.values.forEach { it.close() }
            }
        }
    }

    fun getSourceMetadata(resourceContainer: File): Maybe<ResourceMetadata> {
        return Maybe.fromCallable {
            val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }
            val manifestSources = manifest.dublinCore.source.toSet()
            val manifestProject = manifest.projects.single()
            val sourceCollection = findSourceCollection(manifestSources, manifestProject)
            sourceCollection.resourceContainer
        }
    }

    private fun importResumableProject(
        resourceContainer: File,
        callback: ProjectImporterCallback?
    ): Single<ImportResult> {
        return Single.fromCallable {
            try {
                val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }
                val manifestSources = manifest.dublinCore.source.toSet()
                val manifestProject = try {
                    manifest.projects.single()
                } catch (t: Throwable) {
                    logger.error("In-progress import must have 1 project, but this has {}", manifest.projects.count())
                    throw ImportException(ImportResult.INVALID_RC)
                }

                callback?.onNotifyProgress(
                    localizeKey = "loadingSomething",
                    message = "${manifest.dublinCore.language.identifier}_${manifestProject.identifier}",
                    percent = 10.0
                )
                directoryProvider.newFileReader(resourceContainer).use { fileReader ->
                    val existingSource = fetchExistingSource(manifestProject, manifestSources)
                    try {
                        callback?.onNotifyProgress(localizeKey = "importingSource", percent = 25.0)
                        // Import Sources even if existing source exists in order to potentially merge source audio
                        importSources(fileReader)
                    } catch (e: ImportException) {
                        logger.error("Error importing source of resumable project", e)
                    }
                    val sourceCollection = if (existingSource == null) {
                        findSourceCollection(manifestSources, manifestProject)
                    } else {
                        existingSource
                    }
                    syncProjectVersion(manifest, sourceCollection.resourceContainer!!.version)

                    val metadata = languageRepository
                        .getBySlug(manifest.dublinCore.language.identifier)
                        .map { language ->
                            manifest.dublinCore.mapToMetadata(resourceContainer, language)
                        }
                        .blockingGet()

                    val derived = importResumableProject(fileReader, metadata, manifestProject, sourceCollection, callback)
                    val workbookDescriptor = workbookDescriptorRepository.getAll().blockingGet().firstOrNull {
                        it.targetCollection.id == derived.id && it.sourceCollection.id == sourceCollection.id
                    }
                    callback?.onNotifySuccess(
                        manifest.dublinCore.language.title,
                        manifestProject.title,
                        workbookDescriptor
                    )
                }

                ImportResult.SUCCESS
            } catch (e: ImportException) {
                e.result
            } catch (e: Exception) {
                logger.error("Failed to import in-progress project", e)
                ImportResult.FAILED
            }
        }
    }

    private fun importResumableProject(
        fileReader: IFileReader,
        metadata: ResourceMetadata,
        manifestProject: Project,
        sourceCollection: Collection,
        callback: ProjectImporterCallback?
    ): Collection {
        val sourceMetadata = sourceCollection.resourceContainer!!
        projectMode = getProjectMode(fileReader, metadata.language, sourceCollection.resourceContainer!!.language)
        val isVerseByVerse = projectMode != ProjectMode.TRANSLATION ||
                projectAppVersion == ProjectAppVersion.ONE

        val derivedProject = createDerivedProjects(
            metadata.language,
            sourceCollection,
            projectMode,
            isVerseByVerse
        )

        val translation = createTranslation(sourceMetadata.language, metadata.language)

        val projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            sourceMetadata,
            metadata,
            derivedProject
        )

        projectFilesAccessor.initializeResourceContainerInDir()
        projectFilesAccessor.setProjectMode(projectMode)

        callback?.onNotifyProgress(localizeKey = "copyingSource", percent = 40.0)
        projectFilesAccessor.copySourceFiles(fileReader)

        if (projectAppVersion == ProjectAppVersion.ONE) {
            callback?.onNotifyProgress(localizeKey = "loading_content", percent = 60.0)
            deriveChapterContentFromVerses(derivedProject, projectFilesAccessor)
            setMigrationInfo()
        }
        importContributorInfo(metadata, projectFilesAccessor)
        importChunks(
            derivedProject,
            projectFilesAccessor,
            fileReader
        )
        callback?.onNotifyProgress(localizeKey = "importingTakes", percent = 80.0)

        importTakes(
            fileReader,
            derivedProject,
            manifestProject,
            metadata,
            sourceCollection,
            projectFilesAccessor
        )

        projectFilesAccessor.copyInProgressNarrationFiles(fileReader, manifestProject)
            .doOnError { e ->
                logger.error("Error in importInProgressFiles, project: $derivedProject, manifestProject: $manifestProject")
                logger.error("metadata: $metadata, sourceMetadata: ${sourceCollection.resourceContainer}")
                logger.error("sourceCollection: $sourceCollection", e)
            }
            .blockingSubscribe()

        translation.modifiedTs = LocalDateTime.now()
        languageRepository.updateTranslation(translation).subscribe()
        resetChaptersWithoutTakes(fileReader, derivedProject, projectMode)

        callback?.onNotifyProgress(localizeKey = "finishingUp", percent = 99.0)

        return derivedProject
    }

    private fun setMigrationInfo() {
        migratedSelectedTakes = directoryProvider.tempDirectory.resolve(SELECTED_TAKES_FROM_DB).let {
            if (it.exists()) it.readLines() else listOf()
        }
        val selectedTakeNames = migratedSelectedTakes.map { File(it).name }
        completedChapters = takesInChapterFilter
            ?.filterKeys { takePath ->
                val isChapter = parseNumbers(takePath)
                    ?.contentSignature
                    ?.let { sig -> sig.verse == null } == true

                isChapter && File(takePath).name in selectedTakeNames
            }
            ?.values
            ?.toList()
            ?: listOf()
    }

    private fun getProjectMode(
        fileReader: IFileReader,
        targetLanguage: Language,
        sourceLanguage: Language
    ): ProjectMode {
        if (fileReader.exists(RcConstants.PROJECT_MODE_FILE)) {
            projectAppVersion = ProjectAppVersion.THREE
            val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
            fileReader.bufferedReader(RcConstants.PROJECT_MODE_FILE).use {
                val serialized: SerializableProjectMode = mapper.readValue(it)
                return serialized.mode
            }
        }
        // project mode does not exist until Orature 3
        projectAppVersion = ProjectAppVersion.ONE
        return if (targetLanguage.slug == sourceLanguage.slug) {
            ProjectMode.NARRATION
        } else {
            ProjectMode.TRANSLATION
        }
    }

    private fun importChunks(project: Collection, accessor: ProjectFilesAccessor, fileReader: IFileReader) {
        accessor.copyChunkFile(fileReader)

        val factory = JsonFactory()
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        val mapper = ObjectMapper(factory)
        mapper.registerKotlinModule()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val chunkFileExists = fileReader.exists(RcConstants.CHUNKS_FILE)
        val chunks: Chunkification = if (chunkFileExists) {
            try {
                fileReader.stream(RcConstants.CHUNKS_FILE).let { input ->
                    mapper.readValue(input)
                }
            } catch (e: MismatchedInputException) {
                // empty file
                Chunkification()
            }
        } else {
            Chunkification()
        }

        val chapters = collectionRepository.getChildren(project).blockingGet()
        chapters.forEach { chapter ->
            if (chunks.containsKey(chapter.sort)) {
                val contents = chunks[chapter.sort] ?: listOf()
                contentRepository.deleteForCollection(chapter).blockingAwait()
                contentRepository.insertForCollection(contents, chapter).blockingGet()
            }
        }
    }

    /**
     * Populates all contents under the filtered chapters as verse-by-verse.
     * This method is used when importing projects from Orature 1
     */
    private fun deriveChapterContentFromVerses(
        project: Collection,
        projectAccessor: ProjectFilesAccessor
    ) {
        val filteredChapters = takesInChapterFilter?.values?.distinct()
        collectionRepository.getChildren(project).blockingGet()
            .filter { filteredChapters == null || it.sort in filteredChapters }
            .forEach { chapter ->
                val contents = projectAccessor.getChapterContent(project.slug, chapter.sort)
                    .mapIndexed { index, content ->
                        content.sort = index + 1
                        content.draftNumber = 1
                        content
                    }
                contentRepository.deleteForCollection(chapter, ContentType.TEXT)
                    .andThen(
                        contentRepository.getByCollection(chapter)
                    )
                    .flattenAsObservable { it }
                    .flatMapCompletable { content ->
                        takeRepository.deleteForContent(content)
                    }
                    .andThen(
                        contentRepository.insertForCollection(contents, chapter) // derive contents
                    )
                    .ignoreElement()
                    .blockingGet()
            }
    }

    private fun resetChaptersWithoutTakes(fileReader: IFileReader, derivedProject: Collection, mode: ProjectMode) {
        if (mode != ProjectMode.TRANSLATION) {
            return
        }

        val chunkFileExists = fileReader.exists(RcConstants.CHUNKS_FILE)
        val chapterStarted = if (!chunkFileExists) {
            listOf()
        } else {
            try {
                fileReader.stream(RcConstants.CHUNKS_FILE).let { input ->
                    val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
                    val chunks: Chunkification = mapper.readValue(input)
                    val chapters = chunks.map { it.key }
                    chapters
                }
            } catch (e: Exception) {
                listOf()
            }
        }
        val chaptersNotStarted = collectionRepository
            .collectionsWithoutTakes(derivedProject).blockingGet()
            .filterNot { chapterStarted.contains(it.sort) }

        chaptersNotStarted.forEach { contentRepository.deleteForCollection(it).blockingGet() }
    }

    private fun importContributorInfo(
        metadata: ResourceMetadata,
        projectFilesAccessor: ProjectFilesAccessor
    ) {
        val contributors = ResourceContainer.load(metadata.path).use { rc ->
            rc.manifest.dublinCore.contributor.map { Contributor(it) }
        }
        if (contributors.isNotEmpty()) {
            projectFilesAccessor.setContributorInfo(contributors)
        }
    }

    private fun importTakes(
        fileReader: IFileReader,
        project: Collection,
        manifestProject: Project,
        metadata: ResourceMetadata,
        sourceCollection: Collection,
        projectFilesAccessor: ProjectFilesAccessor
    ) {
        val collectionForTakes = when (metadata.type) {
            // Work around the quirk that resource takes are attached to source, not target project
            ContainerType.Help -> sourceCollection
            else -> project
        }
        val sourceMetadata = sourceCollection.resourceContainer!!

        val selectedTakes = prepareSelectedTakes(fileReader)

        takesCheckingMap = parseCheckingStatusFile(fileReader)

        projectFilesAccessor.copySelectedTakesFile(fileReader)
        projectFilesAccessor.copyTakeFiles(fileReader, manifestProject, ::takeCopyFilter)
            .doOnError { e ->
                logger.error("Error in importTakes, project: $project, manifestProject: $manifestProject")
                logger.error("metadata: $metadata, sourceMetadata: $sourceMetadata")
                logger.error("sourceCollection: $sourceCollection", e)
            }
            .blockingSubscribe { newTakeFile ->
                insertTake(
                    newTakeFile,
                    projectFilesAccessor.audioDir,
                    collectionForTakes,
                    sourceMetadata,
                    selectedTakes
                )
            }
        
        val isNarrationMigration = projectAppVersion == ProjectAppVersion.ONE && projectMode == ProjectMode.NARRATION
        if (isNarrationMigration) {
            takesToCompile.forEach { (chapter, takeFiles) ->
                compileIncompleteChapterNarration(chapter, takeFiles, project, metadata)
            }
        }
    }

    private fun prepareSelectedTakes(fileReader: IFileReader): Set<String> {
        return fileReader
            .bufferedReader(RcConstants.SELECTED_TAKES_FILE)
            .useLines { it.toSet() }
            .mapNotNull { takePath ->
                parseNumbers(takePath)?.let {
                    Pair(takePath, it)
                }
            }
            .filter { (takePath, signature) ->
                takesInChapterFilter?.values?.contains(signature.contentSignature.chapter)
                    ?: true
            }
            .map { (path, _) ->
                path
            }
            .toSet()
    }

    private fun parseCheckingStatusFile(fileReader: IFileReader) =
        if (fileReader.exists(RcConstants.CHECKING_STATUS_FILE)) {
            fileReader.stream(RcConstants.CHECKING_STATUS_FILE).use { stream ->
                ObjectMapper(JsonFactory())
                    .registerKotlinModule()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .readValue<TakeCheckingStatusMap>(stream)
                    .mapKeys { File(it.key).name } // take name as key
            }
        } else {
            mapOf()
        }

    /**
     * Filters only takes that are chosen to import (based on the callback result)
     */
    private fun takeCopyFilter(path: String): Boolean {
        return takesInChapterFilter?.let { takesInChapter ->
            val takePath = takesInChapter.keys.firstOrNull { filterPath ->
                File(filterPath).name == File(path).name
            }
            takePath != null
        } ?: true
    }

    private fun insertTake(
        filepath: String,
        projectAudioDir: File,
        project: Collection,
        metadata: ResourceMetadata,
        selectedTakes: Set<String>
    ) {
        parseNumbers(filepath)?.let { (sig, takeNumber) ->
            getContent(sig, project, metadata)?.let { chunk ->
                val now = LocalDate.now()
                val file = File(filepath).canonicalFile
                val relativeFile = file.relativeTo(projectAudioDir.canonicalFile)
                val isSelected = relativeFile.invariantSeparatorsPath in selectedTakes || filepath in migratedSelectedTakes

                val checkingStatus = when {
                    projectAppVersion.ordinal >= ProjectAppVersion.THREE.ordinal -> takesCheckingMap[relativeFile.name]

                    completedChapters.contains(sig.chapter) -> {
                        TakeCheckingState(CheckingStatus.VERSE, computeFileChecksum(file))
                    }

                    else -> null
                }

                val take = Take(
                    file.name,
                    file,
                    takeNumber,
                    now,
                    null,
                    false,
                    checkingStatus?.status ?: CheckingStatus.UNCHECKED,
                    checkingStatus?.checksum,
                    listOf()
                )
                val insertedId = takeRepository.insertForContent(take, chunk).blockingGet()
                take.id = insertedId

                if (isSelected) {
                    chunk.selectedTake = take
                    contentRepository.update(chunk).blockingAwait()

                    val isNarrationMigration = projectMode == ProjectMode.NARRATION && projectAppVersion == ProjectAppVersion.ONE
                    // store verse take of incomplete chapter narration to compile later
                    if (isNarrationMigration && sig.chapter !in completedChapters && sig.verse != null) {
                        val existingFiles = takesToCompile.getOrDefault(sig.chapter, listOf())
                        takesToCompile[sig.chapter] = existingFiles.plus(file)
                    }
                }
            }
        }
    }

    private fun createDerivedProjects(
        language: Language,
        sourceCollection: Collection,
        mode: ProjectMode,
        verseByVerse: Boolean
    ): Collection {
        val project = createProjectUseCase.create(
            sourceCollection,
            language,
            mode,
            deriveProjectFromVerses = verseByVerse
        ).doOnError {
            logger.error("Error while deriving project(s) during import", it)
        }.blockingGet()

        // populate all books when importing a project
        createProjectUseCase.createAllBooks(
            sourceCollection.resourceContainer!!.language,
            language,
            mode
        ).blockingAwait()

        return project
    }

    private fun findSourceCollection(manifestSources: Set<Source>, manifestProject: Project): Collection {
        val allSourceProjects = collectionRepository.getSourceProjects().blockingGet()
        val sourceCollection: Collection? = allSourceProjects
            .asSequence()
            .filter { sourceProject ->
                sourceProject.resourceContainer
                    ?.run { Source(identifier, language.slug, version) }
                    ?.let { it in manifestSources }
                    ?: false
            }
            .filter {
                it.slug == manifestProject.identifier
            }
            .firstOrNull()

        if (sourceCollection == null) {
            logger.error("Failed to find source that matches requested import.")
            throw ImportException(ImportResult.FAILED)
        }
        return sourceCollection
    }

    private fun hasInProgressMarker(resourceContainer: File): Boolean {
        return directoryProvider.newFileReader(resourceContainer).use {
            it.exists(RcConstants.SELECTED_TAKES_FILE)
        }
    }

    /**
     * Find the relevant source (if any) for the project, regardless of version
     */
    private fun fetchExistingSource(
        manifestProject: Project,
        requestedSources: Set<Source>
    ): Collection? {
        return collectionRepository.getSourceProjects().blockingGet()
            .asSequence()
            .firstOrNull { collection ->
                requestedSources.any { source ->
                    manifestProject.identifier == collection.slug &&
                            source.identifier == collection.resourceContainer!!.identifier &&
                            source.language == collection.resourceContainer!!.language.slug
                }
            }
    }

    private fun importSources(fileReader: IFileReader) {
        val sourceFiles: Sequence<String> = fileReader
            .list(RcConstants.SOURCE_DIR)
            .filter {
                val ext = it.substringAfterLast(".")
                OratureFileFormat.isSupported(ext)
            }

        val firstTry: Map<String, ImportResult> = sourceFiles
            .map { importSource(it, fileReader) }
            .toMap()

        // If our first try results contain both an UNMATCHED_HELP and a SUCCESS, then a retry might help.
        if (firstTry.containsValue(ImportResult.SUCCESS)) {
            firstTry
                .filter { (_, result) -> result == ImportResult.UNMATCHED_HELP }
                .forEach { (file, _) -> importSource(file, fileReader) }
        }
    }

    private fun importSource(fileInZip: String, fileReader: IFileReader): Pair<String, ImportResult> {
        val name = File(fileInZip).nameWithoutExtension
        val result = importAsStream(name, fileReader.stream(fileInZip))
            .blockingGet()
        logger.debug("Import source resource container {} result {}", name, result)
        return fileInZip to result
    }

    private fun createTranslation(sourceLanguage: Language, targetLanguage: Language): Translation {
        val translation = Translation(sourceLanguage, targetLanguage, LocalDateTime.now())
        val id = languageRepository
            .insertTranslation(translation)
            .doOnError { e ->
                logger.error("Error in inserting translation", e)
            }
            .onErrorReturnItem(0)
            .blockingGet()
        translation.id = id
        return translation
    }

    private fun getContent(sig: ContentSignature, project: Collection, metadata: ResourceMetadata): Content? {
        return contentCache.computeIfAbsent(sig) { (chapter, verse, sort, type) ->
            val collection: Observable<Collection> = collectionRepository
                .getChildren(project)
                .flattenAsObservable { it }
                .filter { chapterCollection ->
                    chapterCollection.slug.endsWith("_$chapter")
                }

            val metaOrHelpStartVerse = when (type) {
                ContentType.META -> 1
                else -> 0
            }

            val isHelpVerse = metadata.type == ContainerType.Help && verse != null

            val content: Maybe<Content> = collection
                .flatMap {
                    contentRepository.getByCollection(it).flattenAsObservable { it }
                }
                // If we have help resource chunks, filter to linked TEXT chunks
                .filter { if (isHelpVerse) it.type == ContentType.TEXT else true }
                // If we have help resource chunks, fetch resources by linked TEXT chunk
                // and related resource container
                .flatMap { if (isHelpVerse) resourceRepository.getResources(it, metadata) else Observable.just(it) }
                // If type isn't specified in filename, match on TEXT.
                .filter { content -> content.type == (type ?: ContentType.TEXT) }
                // If verse number isn't specified in filename, assume chapter helps or meta.
                .filter { content ->
                    // start is not unique for chunks, as it refers to verse ranges
                    val number = when (content.labelKey) {
                        "chunk" -> content.sort
                        else -> content.start
                    }
                    number == (verse ?: metaOrHelpStartVerse)
                }
                // If sort isn't specified in filename,
                // DON'T filter on it, because we only need it for helps and meta.
                .filter { content -> sort?.let { content.sort == sort } ?: true }
                .firstElement()

            content.blockingGet()
        }
    }

    /**
     * Compile all the selected takes of every verse into a chapter, so that narration
     * can restore the file as one chapter take. Only call this method if the chapter
     * has not completed/compiled before migrating to Orature 3.x.
     */
    private fun compileIncompleteChapterNarration(
        chapterNumber: Int,
        takeFiles: List<File>,
        project: Collection,
        metadata: ResourceMetadata
    ) {
         val collections = collectionRepository
            .getChildren(project)
            .blockingGet()

        val chapterCollection = collections.find {
            collection -> collection.slug.endsWith("_$chapterNumber")
        } ?: return

        val chapterContent = contentRepository.getCollectionMetaContent(chapterCollection).blockingGet()
        val chunkCount = contentRepository.getByCollection(chapterCollection).blockingGet()
            .count { content -> content.type == ContentType.TEXT }

        val fileNamer = FileNamer(
            start = null,
            end = null,
            sort = chapterNumber,
            contentType = ContentType.META,
            languageSlug = metadata.language.slug,
            bookSlug = project.slug,
            rcSlug = metadata.identifier,
            chunkCount = chunkCount.toLong(),
            chapterCount = collections.count().toLong(),
            chapterTitle = "$chapterNumber",
            chapterSort = chapterNumber
        )
        val fileName = fileNamer.generateName(1, AudioFileFormat.WAV)
        val filesToCompile = takeFiles.sortedBy { parseNumbers(it.name)!!.contentSignature.verse } // sort by verse order
        val compiled = concatAudioUseCase.execute(filesToCompile, includeMarkers = true).blockingGet()
        val chapterFile = takeFiles.first().parentFile.resolve(fileName)
            .apply {
                createNewFile()
                compiled.copyTo(this, overwrite = true)
                compiled.delete()
            }

        val chapterTake = Take(
            fileName,
            chapterFile,
            number = 1,
            LocalDate.now(),
            null,
            false,
            CheckingStatus.UNCHECKED,
            checksum = null,
            listOf()
        )
        val insertedId = takeRepository.insertForContent(chapterTake, chapterContent).blockingGet()
        chapterTake.id = insertedId
        chapterContent.selectedTake = chapterTake
        contentRepository.update(chapterContent).blockingAwait()
    }

    private fun parseNumbers(filename: String): TakeSignature? {
        val matcher = takeFilenamePattern.matcher(filename)
        return if (matcher.find()) {
            val chapter = matcher.group(1).toInt()
            val verse = matcher.group(2)?.toIntOrNull()
            val sort = matcher.group(3)?.toIntOrNull()
            val type = matcher.group(4)?.let { ContentType.of(it) }
            val take = matcher.group(5).toInt()
            TakeSignature(ContentSignature(chapter, verse, sort, type), take)
        } else {
            null
        }
    }

    /** Applies source version to target version before importing. */
    private fun syncProjectVersion(projectManifest: Manifest, version: String) {
        projectManifest.dublinCore.version = version
    }

    data class ContentSignature(val chapter: Int, val verse: Int?, val sort: Int?, val type: ContentType?)
    data class TakeSignature(val contentSignature: ContentSignature, val take: Int)
}
