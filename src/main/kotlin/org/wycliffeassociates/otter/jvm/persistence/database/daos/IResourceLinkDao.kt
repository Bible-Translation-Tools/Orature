package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity

interface IResourceLinkDao : IDao<ResourceLinkEntity> {
    fun fetchByChunkId(id: Int): List<ResourceLinkEntity>
    fun fetchByCollectionId(id: Int): List<ResourceLinkEntity>
}