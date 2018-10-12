package org.wycliffeassociates.otter.common.domain

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

class ViewTakesActions(
        private val chunkRepo: IChunkRepository,
        private val takeRepo: ITakeRepository
) {
    fun getTakes(chunk: Chunk): Single<List<Take>> {
        return takeRepo.getByChunk(chunk)
    }

    fun updateChunkSelectedTake(chunk: Chunk, selectedTake: Take?): Completable {
        chunk.selectedTake = selectedTake
        return chunkRepo.update(chunk)
    }

    fun updateTakePlayed(take: Take, played: Boolean): Completable {
        take.played = played
        return takeRepo.update(take)
    }
}