package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.persistence.repositories.IChunkRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository

class GetContent(
        private val collectionRepo: ICollectionRepository,
        private val chunkRepo: IChunkRepository,
        private val takeRepo: ITakeRepository
) {
    fun getSubcollections(projectRoot: Collection): Single<List<Collection>> {
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
}