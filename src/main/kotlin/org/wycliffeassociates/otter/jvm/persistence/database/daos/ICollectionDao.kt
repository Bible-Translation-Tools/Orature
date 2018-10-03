package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.CollectionEntity

interface ICollectionDao : IDao<CollectionEntity> {
    fun fetchChildren(entity: CollectionEntity): List<CollectionEntity>
    fun fetchSource(entity: CollectionEntity): CollectionEntity
}