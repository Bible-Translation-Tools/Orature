package org.wycliffeassociates.otter.common.domain.project.importer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.Chunkification
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.takeFilenamePattern
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
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
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
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
    private val resourceRepository: IResourceRepository
) : RCImporter(directoryProvider, resourceMetadataRepository) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val contentCache = mutableMapOf<ContentSignature, Content>()
    private var projectName = ""
    private var takesInChapterFilter: Map<String, Int>? = null
    private var duplicatedTakes: MutableList<String> = mutableListOf()

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        val isOngoingProject = isResumableProject(file)
        if (!isOngoingProject) {
            return super.passToNextImporter(file, callback, options)
        }
        takesInChapterFilter = null
        projectName = ""

        return Single
            .fromCallable { projectExists(file) }
            .doOnError {
                logger.error("Error while checking whether project already exists.", it)
            }
            .flatMap { exists ->
                if (exists && callback != null) {
                    val filterProvided = updateTakesImportFilter(file, callback)
                    if (!filterProvided) {
                        return@flatMap Single.just(ImportResult.ABORTED)
                    }
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
                }?.let { workbook ->
                    directoryProvider.getProjectDirectory(
                        workbook.source.resourceMetadata,
                        workbook.target.resourceMetadata,
                        projectSlug
                    ).exists()
                } ?: false
            }
        }
    }

    private fun updateTakesImportFilter(
        file: File,
        callback: ProjectImporterCallback
    ): Boolean {
        val takesChapterMap = fetchTakesInRC(file)
        val chapterList = takesChapterMap.values.distinct().sorted()
        val callbackParam = ImportCallbackParameter(chapterList, projectName)
        val chaptersToImport = callback.onRequestUserInput(callbackParam).blockingGet().chapters
            ?: return false

        takesInChapterFilter = takesChapterMap.filter { entry -> entry.value in chaptersToImport }
        return true
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
                        callback?.onNotifyProgress(localizeKey = "importingSource", percent = 20.0)
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
                        it.targetCollection == derived && it.sourceCollection == sourceCollection
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
        val derivedProject = createDerivedProjects(metadata.language, sourceCollection, true)

        val translation = createTranslation(sourceMetadata.language, metadata.language)

        val projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            sourceMetadata,
            metadata,
            derivedProject
        )

        projectFilesAccessor.initializeResourceContainerInDir()

        callback?.onNotifyProgress(localizeKey = "copyingSource", percent = 40.0)
        projectFilesAccessor.copySourceFiles(fileReader)

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

        translation.modifiedTs = LocalDateTime.now()
        languageRepository.updateTranslation(translation).subscribe()
        resetChaptersWithoutTakes(fileReader, derivedProject)

        callback?.onNotifyProgress(localizeKey = "finishingUp", percent = 100.0)

        return derivedProject
    }

    private fun importChunks(project: Collection, accessor: ProjectFilesAccessor, fileReader: IFileReader) {
        accessor.copyChunkFile(fileReader)

        val factory = JsonFactory()
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        val mapper = ObjectMapper(factory)
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val chunks = mutableMapOf<Int, List<Content>>()

        val file: File = accessor.getChunkFile()
        try {
            if (file.exists() && file.length() > 0) {
                val typeRef: TypeReference<HashMap<Int, List<Content>>> =
                    object : TypeReference<HashMap<Int, List<Content>>>() {}
                val map: Map<Int, List<Content>> = mapper.readValue(file, typeRef)
                chunks.putAll(map)
            }
        } catch (e: MismatchedInputException) {
            // clear file if it can't be read
            file.writer().use { }
        }

        val chapters = collectionRepository.getChildren(project).blockingGet()
        chapters.forEach { chapter ->
            if (chunks.containsKey(chapter.sort)) {
                val contents = chunks[chapter.sort]
                contentRepository.deleteForCollection(chapter).blockingAwait()
                contents?.forEach { content ->
                    contentRepository.insertForCollection(content, chapter).blockingGet()
                }
            }
        }
    }

    private fun resetChaptersWithoutTakes(fileReader: IFileReader, derivedProject: Collection) {
        val chunkFileExists = fileReader.exists(RcConstants.CHUNKS_FILE)
        val chapterStarted = if (!chunkFileExists) {
            listOf()
        } else {
            try {
                fileReader.stream(RcConstants.CHUNKS_FILE).let { input ->
                    val mapper = ObjectMapper(JsonFactory()).registerModule(KotlinModule())
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

        val existingTakes = projectFilesAccessor.audioDir.walk()
            .filter { AudioFileFormat.isSupported(it.extension) }
            .map { it.name }

        val selectedTakes = prepareSelectedTakes(fileReader)
        duplicatedTakes = takesInChapterFilter
            ?.keys
            ?.filter { takePath -> existingTakes.contains(File(takePath).name) }
            ?.toMutableList()
            ?: mutableListOf()

        projectFilesAccessor.copySelectedTakesFile(fileReader)
        projectFilesAccessor.copyTakeFiles(fileReader, manifestProject, ::takeCopyFilter)
            .doOnError { e ->
                logger.error("Error in importTakes, project: $project, manifestProject: $manifestProject")
                logger.error("metadata: $metadata, sourceMetadata: $sourceMetadata")
                logger.error("sourceCollection: $sourceCollection", e)
            }
            .subscribe { newTakeFile ->
                insertTake(
                    newTakeFile,
                    projectFilesAccessor.audioDir,
                    collectionForTakes,
                    sourceMetadata,
                    selectedTakes
                )
            }

        importDuplicatedTakes(
            fileReader,
            projectFilesAccessor.audioDir,
            collectionForTakes,
            manifestProject,
            sourceMetadata,
            selectedTakes
        )
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

    /**
     * Filters only takes that are chosen to import (based on the callback result)
     * AND excludes duplicated takes (takes that already exist).
     */
    private fun takeCopyFilter(path: String): Boolean {
        return takesInChapterFilter?.let { takesInChapter ->
            val takePath = takesInChapter.keys.firstOrNull { filterPath ->
                File(filterPath).name == File(path).name
            }
            takePath != null && takePath !in duplicatedTakes
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

                val take = Take(file.name, file, takeNumber, now, null, false, listOf())
                take.id = takeRepository.insertForContent(take, chunk).blockingGet()

                if (relativeFile.invariantSeparatorsPath in selectedTakes) {
                    chunk.selectedTake = take
                    contentRepository.update(chunk).blockingAwait()
                }
            }
        }
    }

    private fun createDerivedProjects(
        language: Language,
        sourceCollection: Collection,
        verseByVerse: Boolean
    ): Collection {
        return CreateProject(collectionRepository, resourceMetadataRepository)
            .create(sourceCollection, language, deriveProjectFromVerses = verseByVerse)
            .blockingGet()
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

    private fun importDuplicatedTakes(
        fileReader: IFileReader,
        projectAudioDir: File,
        project: Collection,
        manifestProject: Project,
        metadata: ResourceMetadata,
        selectedTakes: Set<String>
    ) {
        duplicatedTakes.forEach { takePath ->
            parseNumbers(takePath)?.let { (sig, _) ->
                getContent(sig, project, metadata)?.let { content ->
                    val take = copyDuplicatedTakeToProjectDir(
                        takePath,
                        projectAudioDir,
                        content,
                        manifestProject,
                        fileReader
                    ).apply {
                        id = takeRepository.insertForContent(this, content).blockingGet()
                    }

                    if (selectedTakes.any { it.contains(File(takePath).name)}) {
                        content.selectedTake = take
                        contentRepository.update(content).blockingAwait()
                    }
                }
            }
        }
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

    private fun copyDuplicatedTakeToProjectDir(
        takePath: String,
        projectAudioDir: File,
        content: Content,
        manifestProject: Project,
        fileReader: IFileReader
    ): Take {
        val now = LocalDate.now()
        val newTakeNumber = takeRepository.getByContent(content, false)
            .blockingGet()
            .maxByOrNull { it.number }
            ?.let { it.number + 1 }
            ?: 1

        val newFileName = File(takePath).name
            .replaceFirst(Regex("_t\\d"), "_t$newTakeNumber")

        val targetTakeFile = projectAudioDir
            .resolve(getRelativeTakePath(takePath, manifestProject.path))
            .parentFile.resolve(newFileName)
            .apply {
                parentFile.mkdirs()
                createNewFile()
            }

        fileReader.stream(takePath).buffered().use { input ->
            targetTakeFile.outputStream().use {
                input.transferTo(it)
            }
        }

        return Take(newFileName, targetTakeFile, newTakeNumber, now, null, false, listOf())
    }

    private fun getRelativeTakePath(pathInRC: String, metaProjectPath: String): String {
        val metaProjectDir = File(metaProjectPath).normalize()
        val takeDirInRC = File(RcConstants.TAKE_DIR)
        val filePath = File(pathInRC)

        return if (pathInRC.startsWith(metaProjectDir.invariantSeparatorsPath)) {
            filePath.relativeTo(metaProjectDir).invariantSeparatorsPath
        } else {
            filePath.relativeTo(takeDirInRC).invariantSeparatorsPath
        }
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
