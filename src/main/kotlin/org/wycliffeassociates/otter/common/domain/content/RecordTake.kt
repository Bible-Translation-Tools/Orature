package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function3
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.IWaveFileCreator
import org.wycliffeassociates.otter.common.persistence.repositories.IChunkRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.io.File
import java.time.LocalDate

class RecordTake(
        private val collectionRepository: ICollectionRepository,
        private val chunkRepository: IChunkRepository,
        private val takeRepository: ITakeRepository,
        private val directoryProvider: IDirectoryProvider,
        private val waveFileCreator: IWaveFileCreator,
        private val launchPlugin: LaunchPlugin
) {
    private fun getChunkCount(collection: Collection): Single<Int> = chunkRepository
            .getByCollection(collection)
            .map { it.size }

    private fun getNumberOfSubcollections(collection: Collection): Single<Int> = collectionRepository
            .getChildren(collection)
            .map { it.size }

    private fun getMaxTakeNumber(chunk: Chunk): Single<Int> = takeRepository
            .getByChunk(chunk)
            .map { takes ->
                takes.maxBy { it.number }?.number ?: 0
            }

    private fun generateFilename(
            project: Collection,
            chapter: Collection,
            chunk: Chunk,
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
        val chapterNumber = chapterFormat.format(chapter.titleKey.toInt())
        val verseNumber = if (chunk.start == chunk.end) {
            verseFormat.format(chunk.start)
        } else {
            "$verseFormat-$verseFormat".format(chunk.start, chunk.end)
        }
        val takeNumber = "%02d".format(number)

        // Compile the complete filename
        return listOf(
                languageSlug,
                rcSlug,
                "b$bookNumber",
                bookSlug,
                "c$chapterNumber",
                "v$verseNumber",
                "t$takeNumber"
        ).joinToString("_", postfix = ".wav")
    }

    private fun create(project: Collection, chapter: Collection, chunk: Chunk): Single<Take> = Single
            .zip(
                    getMaxTakeNumber(chunk),
                    getNumberOfSubcollections(project),
                    getChunkCount(chapter),
                    Function3 { highest, chapterCount, verseCount ->
                        val filename = generateFilename(
                                project,
                                chapter,
                                chunk,
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

    private fun insert(take: Take, chunk: Chunk): Completable = takeRepository
            .insertForChunk(take, chunk)
            .toCompletable()

    fun record(project: Collection, chapter: Collection, chunk: Chunk): Completable {
        return create(project, chapter, chunk)
                .flatMap { take ->
                    launchPlugin
                            .launchRecorder(take.path)
                            .toSingle { take }
                }
                .flatMapCompletable { take ->
                    insert(take, chunk)
                }
    }
}