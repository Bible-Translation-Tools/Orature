package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
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
    private val contentCache = mutableMapOf<ChapterVerse, Content>()
    private val takeFilenamePattern = Pattern.compile("""_c(\d+)_v(\d+)_t(\d+)\.""")

    fun isInProgress(resourceContainer: File): Boolean {
        return try {
            resourceContainer.isFile && resourceContainer.extension == "zip" && hasInProgressMarker(resourceContainer)
        } catch (e: IOException) {
            false
        }
    }

    fun importInProgress(resourceContainer: File): Single<ImportResult> {
        return Single.fromCallable {
            try {
                val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }
                val metadata = languageRepository
                    .getBySlug(manifest.dublinCore.language.identifier)
                    .map { language ->
                        manifest.dublinCore.mapToMetadata(resourceContainer, language)
                    }
                    .blockingGet()

                directoryProvider.newZipFileReader(resourceContainer).use { zipFileReader ->

                    importSources(zipFileReader)

                    val createdDerivedProject: Collection = createDerivedProject(metadata)

                    importTakes(zipFileReader, metadata, createdDerivedProject)

                    ImportResult.SUCCESS
                }
            } catch (e: Exception) {
                ImportResult.IMPORT_ERROR
            }
        }
    }

    private fun importTakes(
        zipFileReader: IZipFileReader,
        metadata: ResourceMetadata,
        project: Collection
    ) {
        val audioDir = directoryProvider.getProjectAudioDirectory(metadata, project)

        val selectedTakes = zipFileReader
            .bufferedReader(RcConstants.SELECTED_TAKES_FILE)
            .useLines { it.toSet() }

        val tryInsertTake: (String) -> Boolean = { filepath ->
            val file = File(filepath)
            if (isAudioFile(file)) {
                parseNumbers(filepath)?.let { (chapterVerse, takeNumber) ->
                    getContent(chapterVerse, project)?.let { chunk ->
                        val now = LocalDate.now()
                        val take = Take(file.name, file, takeNumber, now, null, false, listOf())
                        take.id = takeRepository.insertForContent(take, chunk).blockingGet()
                        if (filepath in selectedTakes) {
                            chunk.selectedTake = take
                            contentRepository.update(chunk).blockingAwait()
                        }
                    }
                }
                true
            } else {
                false
            }
        }

        zipFileReader.copyDirectory(RcConstants.TAKE_DIR, audioDir, tryInsertTake)
        zipFileReader.copyDirectory(RcConstants.MEDIA_DIR, audioDir, tryInsertTake)
    }

    private fun createDerivedProject(metadata: ResourceMetadata): Collection {
        val sourceLookup = collectionRepository
            .getRootSources()
            .flattenAsObservable { it }
            .filter {
                it.resourceContainer?.run {
                    language.slug == metadata.language.slug && identifier == metadata.identifier
                } ?: false
            }
            .firstOrError()

        return sourceLookup
            .flatMap { sourceCollection ->
                CreateProject(collectionRepository).create(sourceCollection, metadata.language)
            }
            .blockingGet()
    }

    private fun hasInProgressMarker(resourceContainer: File): Boolean {
        return directoryProvider.newZipFileReader(resourceContainer).use {
            it.exists(RcConstants.SELECTED_TAKES_FILE)
        }
    }

    private fun importSources(zipFileReader: IZipFileReader) {
        val sourceFiles = zipFileReader
            .list(RcConstants.SOURCE_DIR)
            .filter { it.extension.toLowerCase() == "zip" }

        val firstTry = sourceFiles
            .map { importSource(it, zipFileReader) }
            .toMap()

        // If our first try results contain both an UNMATCHED_HELP and a SUCCESS, then a retry might help.
        if (firstTry.containsValue(ImportResult.SUCCESS)) {
            firstTry
                .filter { (_, result) -> result == ImportResult.UNMATCHED_HELP }
                .forEach { (file, _) -> importSource(file, zipFileReader) }
        }
    }

    private fun importSource(fileInZip: File, zipFileReader: IZipFileReader): Pair<File, ImportResult> {
        val name = fileInZip.nameWithoutExtension
        val result = resourceContainerImporter
            .import(name, zipFileReader.stream(fileInZip.path))
            .blockingGet()
        // TODO: Log.info("Import source resource container $name result $result")
        return fileInZip to result
    }

    private fun isAudioFile(file: File) = file.extension.let { it == "wav" || it == "mp3" }

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
                .firstElement()

            content.blockingGet(null)
        }
    }

    fun parseNumbers(filename: String): ChapterVerseTake? {
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
