package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function3
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.persistence.EMPTY_WAVE_FILE_SIZE
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.IWaveFileCreator
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.io.File
import java.time.LocalDate

class RecordTake(
        private val collectionRepository: ICollectionRepository,
        private val contentRepository: IContentRepository,
        private val takeRepository: ITakeRepository,
        private val directoryProvider: IDirectoryProvider,
        private val waveFileCreator: IWaveFileCreator,
        private val launchPlugin: LaunchPlugin
) {
    enum class Result {
        SUCCESS,
        NO_RECORDER,
        NO_AUDIO
    }

    private fun getContentCount(collection: Collection, filter: (Content) -> Boolean): Single<Int> = contentRepository
            .getByCollection(collection)
            .map { retrieved -> retrieved.filter(filter).size }

    private fun getNumberOfSubcollections(collection: Collection): Single<Int> = collectionRepository
            .getChildren(collection)
            .map { it.size }

    private fun getMaxTakeNumber(content: Content): Single<Int> = takeRepository
            .getByContent(content)
            .map { takes ->
                takes.maxBy { it.number }?.number ?: 0
            }

    private fun generateFilename(
            project: Collection,
            chapter: Collection,
            content: Content,
            number: Int,
            chapterCount: Int,
            chunkCount: Int
    ): String {
        // Get the correct format specifiers
        val chapterFormat = if (chapterCount > 99) "%03d" else "%02d"
        val verseFormat = if (chunkCount > 99) "%03d" else "%02d"

        // Format each piece of the filename
        val languageSlug = project.resourceContainer?.language?.slug ?: ""
        val rcSlug = project.resourceContainer?.identifier ?: ""
        val bookNumber = "%02d".format(
                // Handle book number offset (only for Bibles)
                if (project.resourceContainer?.subject?.toLowerCase() == "bible"
                        && project.sort > 39) project.sort + 1 else project.sort
        )
        val bookSlug = project.slug
        val chapterNumber = chapterFormat.format(chapter.titleKey.toIntOrNull() ?: chapter.sort)
        val verseNumber = if (content.start == content.end) {
            verseFormat.format(content.start)
        } else {
            "$verseFormat-$verseFormat".format(content.start, content.end)
        }
        val takeNumber = "%02d".format(number)

        // Compile the complete filename

        return if (content.labelKey == "chapter") {
            listOf(
                    languageSlug,
                    rcSlug,
                    "b$bookNumber",
                    bookSlug,
                    "c$chapterNumber",
                    "t$takeNumber"
            ).joinToString("_", postfix = ".wav")
        } else {
            listOf(
                    languageSlug,
                    rcSlug,
                    "b$bookNumber",
                    bookSlug,
                    "c$chapterNumber",
                    "v$verseNumber",
                    "t$takeNumber"
            ).joinToString("_", postfix = ".wav")
        }
    }

    private fun create(project: Collection, chapter: Collection, content: Content): Single<Take> = Single
            .zip(
                    getMaxTakeNumber(content),
                    getNumberOfSubcollections(project),
                    getContentCount(chapter) { it.labelKey != "chapter" },
                    Function3 { highest, chapterCount, verseCount ->
                        val filename = generateFilename(
                                project,
                                chapter,
                                content,
                                highest + 1,
                                chapterCount,
                                verseCount
                        )

                        // Create a file for this take
                        val chapterFormat = if (chapterCount > 99) "%03d" else "%02d"

                        val takeFile = directoryProvider
                                .getProjectAudioDirectory(project, chapterFormat.format(chapter.titleKey.toInt()))
                                .resolve(File(filename))

                        val newTake = Take(
                                takeFile.name,
                                takeFile,
                                highest + 1,
                                LocalDate.now(),
                                false,
                                listOf() // No markers
                        )

                        // Create an empty WAV file
                        waveFileCreator.createEmpty(newTake.path)
                        return@Function3 newTake
                    }
            )

    private fun insert(take: Take, content: Content): Completable = takeRepository
            .insertForContent(take, content)
            .toCompletable()

    fun record(project: Collection, chapter: Collection, content: Content): Single<Result> {
        return create(project, chapter, content)
                .flatMap { take ->
                    launchPlugin
                            .launchRecorder(take.path)
                            .flatMap {
                                when (it) {
                                    LaunchPlugin.Result.SUCCESS -> {
                                        if (take.path.length() == EMPTY_WAVE_FILE_SIZE) {
                                            take.path.delete()
                                            Single.just(Result.NO_AUDIO)
                                        } else {
                                            insert(take, content).toSingle { Result.SUCCESS }
                                        }
                                    }
                                    LaunchPlugin.Result.NO_PLUGIN -> {
                                        take.path.delete()
                                        Single.just(Result.NO_RECORDER)
                                    }
                                }
                            }
                }
    }
}