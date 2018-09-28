package org.wycliffeassociates.otter.common.domain.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata

interface IResourceMetadataRepository : IRepository<ResourceMetadata> {
    fun getAll(): Single<List<ResourceMetadata>>
}