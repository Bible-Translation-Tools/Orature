package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata

interface IResourceMetadataRepository : IRepository<ResourceMetadata> {
    fun exists(metadata: ResourceMetadata): Single<Boolean>
    fun get(metadata: ResourceMetadata): Single<ResourceMetadata>
    fun insert(metadata: ResourceMetadata): Single<Int>
    fun updateSource(metadata: ResourceMetadata, source: ResourceMetadata?): Completable
    fun getSource(metadata: ResourceMetadata): Maybe<ResourceMetadata>
    // These functions are commutative
    fun addLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable
    fun removeLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable
    fun getLinked(metadata: ResourceMetadata): Single<List<ResourceMetadata>>
}