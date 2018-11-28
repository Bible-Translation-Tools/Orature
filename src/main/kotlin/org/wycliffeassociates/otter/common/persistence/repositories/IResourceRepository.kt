package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection

interface IResourceRepository : IRepository<Chunk> {
    // Get resources for a chunk
    fun getByChunk(chunk: Chunk): Single<List<Chunk>>
    // Get resources for a collection
    fun getByCollection(collection: Collection): Single<List<Chunk>>
    // Link/Unlink
    fun linkToChunk(resource: Chunk, chunk: Chunk): Completable
    fun unlinkFromChunk(resource: Chunk, chunk: Chunk): Completable
    fun linkToCollection(resource: Chunk, collection: Collection): Completable
    fun unlinkFromCollection(resource: Chunk, collection: Collection): Completable
}