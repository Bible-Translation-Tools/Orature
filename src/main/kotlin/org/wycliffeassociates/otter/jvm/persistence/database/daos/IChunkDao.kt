package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.ChunkEntity

interface IChunkDao : IDao<ChunkEntity> {
    fun fetchSources(entity: ChunkEntity): List<ChunkEntity>
    fun updateSources(entity: ChunkEntity, sources: List<ChunkEntity>)
    fun fetchByCollectionId(collectionId: Int): List<ChunkEntity>
}