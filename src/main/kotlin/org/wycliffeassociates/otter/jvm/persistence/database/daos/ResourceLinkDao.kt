package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.RESOURCE_LINK
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity

class ResourceLinkDao(
        private val dsl: DSLContext
) : IResourceLinkDao {
    override fun fetchByChunkId(id: Int): List<ResourceLinkEntity> {
        return dsl
                .select()
                .from(RESOURCE_LINK)
                .where(RESOURCE_LINK.CONTENT_FK.eq(id))
                .fetch {
                    RecordMappers.mapToResourceLinkEntity(it)
                }
    }

    override fun fetchByCollectionId(id: Int): List<ResourceLinkEntity> {
        return dsl
                .select()
                .from(RESOURCE_LINK)
                .where(RESOURCE_LINK.COLLECTION_FK.eq(id))
                .fetch {
                    RecordMappers.mapToResourceLinkEntity(it)
                }
    }

    @Synchronized
    override fun insert(entity: ResourceLinkEntity): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the resource link entity
        dsl
                .insertInto(
                        RESOURCE_LINK,
                        RESOURCE_LINK.RESOURCE_CONTENT_FK,
                        RESOURCE_LINK.CONTENT_FK,
                        RESOURCE_LINK.COLLECTION_FK
                )
                .values(
                        entity.resourceChunkFk,
                        entity.chunkFk,
                        entity.collectionFk
                )
                .execute()

        // Fetch and return the resulting ID
        return dsl
                .select(max(RESOURCE_LINK.ID))
                .from(RESOURCE_LINK)
                .fetchOne {
                    it.getValue(max(RESOURCE_LINK.ID))
                }
    }

    override fun fetchById(id: Int): ResourceLinkEntity {
        return dsl
                .select()
                .from(RESOURCE_LINK)
                .where(RESOURCE_LINK.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToResourceLinkEntity(it)
                }
    }

    override fun fetchAll(): List<ResourceLinkEntity> {
        return dsl
                .select()
                .from(RESOURCE_LINK)
                .fetch {
                    RecordMappers.mapToResourceLinkEntity(it)
                }
    }

    override fun update(entity: ResourceLinkEntity) {
        dsl
                .update(RESOURCE_LINK)
                .set(RESOURCE_LINK.RESOURCE_CONTENT_FK, entity.resourceChunkFk)
                .set(RESOURCE_LINK.CONTENT_FK, entity.chunkFk)
                .set(RESOURCE_LINK.COLLECTION_FK, entity.collectionFk)
                .where(RESOURCE_LINK.ID.eq(entity.id))
                .execute()
    }

    override fun delete(entity: ResourceLinkEntity) {
        dsl
                .deleteFrom(RESOURCE_LINK)
                .where(RESOURCE_LINK.ID.eq(entity.id))
                .execute()
    }
}