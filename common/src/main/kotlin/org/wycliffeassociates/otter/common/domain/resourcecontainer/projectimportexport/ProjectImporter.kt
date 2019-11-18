package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.common.persistence.zip.IZipFileReader
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.util.regex.Pattern

class ProjectImporter(
    private val resourceContainerImporter: ImportResourceContainer,
    private val directoryProvider: IDirectoryProvider,
    private val collectionRepository: ICollectionRepository,
    private val contentRepository: IContentRepository,
    private val takeRepository: ITakeRepository,
    private val languageRepository: ILanguageRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val contentCache = mutableMapOf<ChapterVerse, Content>()
    private val takeFilenamePattern = Pattern.compile("""_c(\d+)_v(\d+)_t(\d+)\.""")

    fun isResumableProject(resourceContainer: File): Boolean {
        return try {
            resourceContainer.isFile && resourceContainer.extension == "zip" && hasInProgressMarker(resourceContainer)
        } catch (e: IOException) {
            false
        }
    }

    fun importResumableProject(resourceContainer: File): Single<ImportResult> {
        return Single.fromCallable {
            try {
                val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }

                val metadata = languageRepository
                    .getBySlug(manifest.dublinCore.language.identifier)
                    .map { language ->
                        manifest.dublinCore.mapToMetadata(resourceContainer, language)
                    }
                    .blockingGet()

                val manifestSources = manifest.dublinCore.source.toSet()

                val manifestProject = try {
                    manifest.projects.single()
                } catch (t: Throwable) {
                    log.error("In-progress import must have 1 project, but this has {}", manifest.projects.count())
                    throw ImportException(ImportResult.INVALID_RC)
                }

                directoryProvider.newZipFileReader(resourceContainer).use { zipFileReader ->
                    importResumableProject(zipFileReader, metadata, manifestProject, manifestSources)
                }

                ImportResult.SUCCESS
            } catch (e: ImportException) {
                e.result
            } catch (e: Exception) {
                log.error("Failed to import in-progress project", e)
                ImportResult.IMPORT_ERROR
            }
        }
    }

    private fun importResumableProject(
        zipFileReader: IZipFileReader,
        metadata: ResourceMetadata,
        manifestProject: Project,
        manifestSources: Set<Source>
    ) {
        importSources(zipFileReader)

        val sourceCollection = findSourceCollection(manifestSources)

        val derivedProject = createDerivedProject(metadata, sourceCollection)

        importTakes(zipFileReader, derivedProject, manifestProject, sourceCollection.resourceContainer!!)
    }

    private fun importTakes(
        zipFileReader: IZipFileReader,
        project: Collection,
        manifestProject: Project,
        sourceMetadata: ResourceMetadata
    ) {
        val audioDir = directoryProvider.getProjectAudioDirectory(sourceMetadata, project)

        val selectedTakes = zipFileReader
            .bufferedReader(RcConstants.SELECTED_TAKES_FILE)
            .useLines { it.toSet() }

        Observable.just(RcConstants.TAKE_DIR, manifestProject.path)
            .filter(zipFileReader::exists)
            .flatMap { audioDirInRc ->
                zipFileReader.copyDirectory(audioDirInRc, audioDir, this::isAudioFile)
            }
            .subscribe { newTakeFile ->
                insertTake(newTakeFile, audioDir, project, selectedTakes)
            }
    }

    private fun insertTake(
        filepath: String,
        projectAudioDir: File,
        project: Collection,
        selectedTakes: Set<String>
    ) {
        parseNumbers(filepath)?.let { (chapterVerse, takeNumber) ->
            getContent(chapterVerse, project)?.let { chunk ->
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

    private fun createDerivedProject(metadata: ResourceMetadata, sourceCollection: Collection): Collection {
        return CreateProject(collectionRepository)
            .create(sourceCollection, metadata.language)
            .blockingGet()
    }

    private fun findSourceCollection(manifestSources: Set<Source>): Collection {
        val allSourceProjects = collectionRepository.getSourceProjects().blockingGet()
        val sourceCollection: Collection? = allSourceProjects
            .asSequence()
            .filter { sourceProject ->
                sourceProject.resourceContainer
                    ?.run { Source(identifier, language.slug, version) }
                    ?.let { it in manifestSources }
                    ?: false
            }
            .firstOrNull()

        if (sourceCollection == null) {
            log.error("Failed to find source that matches requested import.")
            throw ImportException(ImportResult.IMPORT_ERROR)
        }
        return sourceCollection
    }

    private fun hasInProgressMarker(resourceContainer: File): Boolean {
        return directoryProvider.newZipFileReader(resourceContainer).use {
            it.exists(RcConstants.SELECTED_TAKES_FILE)
        }
    }

    private fun importSources(zipFileReader: IZipFileReader) {
        val sourceFiles: Sequence<String> = zipFileReader
            .list(RcConstants.SOURCE_DIR)
            .filter { it.endsWith(".zip", ignoreCase = true) }

        val firstTry: Map<String, ImportResult> = sourceFiles
            .map { importSource(it, zipFileReader) }
            .toMap()

        // If our first try results contain both an UNMATCHED_HELP and a SUCCESS, then a retry might help.
        if (firstTry.containsValue(ImportResult.SUCCESS)) {
            firstTry
                .filter { (_, result) -> result == ImportResult.UNMATCHED_HELP }
                .forEach { (file, _) -> importSource(file, zipFileReader) }
        }
    }

    private fun importSource(fileInZip: String, zipFileReader: IZipFileReader): Pair<String, ImportResult> {
        val name = File(fileInZip).nameWithoutExtension
        val result = resourceContainerImporter
            .import(name, zipFileReader.stream(fileInZip))
            .blockingGet()
        log.debug("Import source resource container {} result {}", name, result)
        return fileInZip to result
    }

    private fun isAudioFile(file: String) = isAudioFile(File(file))

    private fun isAudioFile(file: File) = file.extension.toLowerCase().let { it == "wav" || it == "mp3" }

    private fun getContent(cv: ChapterVerse, project: Collection): Content? {
        return contentCache.computeIfAbsent(cv) { (c, v) ->
            val collection: Observable<Collection> = collectionRepository
                .getChildren(project)
                .flattenAsObservable { it }
                .filter { chapterCollection -> chapterCollection.slug.endsWith("_$c") }

            val content: Maybe<Content> = collection
                .flatMap {
                    contentRepository.getByCollection(it).flattenAsObservable { it }
                }
                .filter { content -> content.start == v }
                .filter { content -> content.type == ContentType.TEXT }
                .firstElement()

            content.blockingGet()
        }
    }

    private fun parseNumbers(filename: String): ChapterVerseTake? {
        val matcher = takeFilenamePattern.matcher(filename)
        return if (matcher.find()) {
            val chapter = matcher.group(1).toInt()
            val verse = matcher.group(2).toInt()
            val take = matcher.group(3).toInt()
            ChapterVerseTake(ChapterVerse(chapter, verse), take)
        } else {
            null
        }
    }

    data class ChapterVerse(val chapter: Int, val verse: Int)
    data class ChapterVerseTake(val chapterVerse: ChapterVerse, val take: Int)
}
