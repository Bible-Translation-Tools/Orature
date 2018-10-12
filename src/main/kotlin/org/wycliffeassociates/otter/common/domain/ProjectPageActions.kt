package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.IWaveFileCreator
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IChunkRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.io.File
import java.time.LocalDate

class ProjectPageActions(
        private val directoryProvider: IDirectoryProvider,
        private val waveFileCreator: IWaveFileCreator,
        private val collectionRepo: ICollectionRepository,
        private val chunkRepo: IChunkRepository,
        private val takeRepo: ITakeRepository,
        private val pluginRepo: IAudioPluginRepository
) {
    fun getChildren(projectRoot: Collection): Single<List<Collection>> {
        return collectionRepo.getChildren(projectRoot)
    }

    fun getChunks(collection: Collection): Single<List<Chunk>> {
        return chunkRepo.getByCollection(collection)
    }

    fun getTakeCount(chunk: Chunk): Single<Int> {
        return takeRepo
                .getByChunk(chunk)
                .map {
                    it.size
                }
    }

    fun insertTake(take: Take, chunk: Chunk): Single<Int> {
        return takeRepo.insertForChunk(take, chunk)
    }

    fun updateTake(take: Take): Completable {
        return takeRepo.update(take)
    }

    fun createNewTake(chunk: Chunk, project: Collection, chapter: Collection): Single<Take> {
        return getTakeCount(chunk)
                .map { numOfTakes ->
                    // Create a file for this take
                    val takeFile = directoryProvider
                            .getProjectAudioDirectory(project, listOf(chapter))
                            .resolve(File("chunk${chunk.sort}_take${numOfTakes + 1}.wav"))

                    val newTake = Take(
                            takeFile.name,
                            takeFile,
                            numOfTakes + 1,
                            LocalDate.now(),
                            false,
                            listOf() // No markers
                    )

                    // Create an empty WAV file
                    waveFileCreator.createEmpty(newTake.path)

                    newTake
                }
    }

    fun launchDefaultPluginForTake(take: Take): Completable {
        return pluginRepo
                .getDefaultPlugin()
                .flatMapCompletable {
                    it.launch(take.path)
                }
    }
}