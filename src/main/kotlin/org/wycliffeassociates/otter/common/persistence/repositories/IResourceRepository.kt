package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Resource
import org.wycliffeassociates.otter.common.data.model.Collection

interface IResourceRepository : IRepository<Resource> {
    // Get resources for a chunk
    fun getByChunk(chunk: Chunk): Single<List<Resource>>
    // Get resources for a collection
    fun getByCollection(collection: Collection): Single<List<Resource>>
    // Link/Unlink
    fun linkToChunk(resource: Resource, chunk: Chunk): Completable
    fun unlinkFromChunk(resource: Resource, chunk: Chunk): Completable
    fun linkToCollection(resource: Resource, collection: Collection): Completable
    fun unlinkFromCollection(resource: Resource, collection: Collection): Completable
}