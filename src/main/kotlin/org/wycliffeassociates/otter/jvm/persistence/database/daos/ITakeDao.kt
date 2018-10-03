package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.TakeEntity

interface ITakeDao : IDao<TakeEntity> {
    fun fetchByChunkId(id: Int): List<TakeEntity>
}