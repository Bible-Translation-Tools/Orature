package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceMetadataEntity

// Additional convenience queries for dublin core data
interface IResourceMetadataDao : IDao<ResourceMetadataEntity> {
    fun fetchLinks(entityId: Int): List<ResourceMetadataEntity>
    fun addLink(entity1Id: Int, entity2Id: Int)
    fun removeLink(entity1Id: Int, entity2Id: Int)
}
