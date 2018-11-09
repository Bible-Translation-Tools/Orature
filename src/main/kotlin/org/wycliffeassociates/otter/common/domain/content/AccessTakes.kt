package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.IWaveFileCreator
import org.wycliffeassociates.otter.common.persistence.repositories.IChunkRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.io.File
import java.time.LocalDate

class AccessTakes(
        private val chunkRepo: IChunkRepository,
        private val takeRepo: ITakeRepository
) {
    fun getByChunk(chunk: Chunk): Single<List<Take>> {
        return takeRepo.getByChunk(chunk)
    }

    fun setSelectedTake(chunk: Chunk, selectedTake: Take?): Completable {
        chunk.selectedTake = selectedTake
        return chunkRepo.update(chunk)
    }

    fun setTakePlayed(take: Take, played: Boolean): Completable {
        take.played = played
        return takeRepo.update(take)
    }

    fun delete(take: Take): Completable {
        return takeRepo.delete(take)
    }
}