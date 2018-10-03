package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection

interface IChunkRepository : IRepository<Chunk> {
    // Insert for a collection
    fun insertForCollection(chunk: Chunk, collection: Collection): Single<Int>
    // Get all the chunks for a collection
    fun getByCollection(collection: Collection): Single<List<Chunk>>
    // Get sources this chunk is derived from
    fun getSources(chunk: Chunk): Single<List<Chunk>>
    // Update the sources for a chunk
    fun updateSources(chunk: Chunk, sourceChunks: List<Chunk>): Completable
}