package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.CollectionEntity

class CollectionDao(
        private val dsl: DSLContext
) : ICollectionDao {
    override fun fetchChildren(entity: CollectionEntity): List<CollectionEntity> {
        return dsl
                .select()
                .from(COLLECTION_ENTITY)
                .where(COLLECTION_ENTITY.PARENT_FK.eq(entity.id))
                .fetch {
                    RecordMappers.mapToCollectionEntity(it)
                }
    }

    override fun fetchSource(entity: CollectionEntity): CollectionEntity {
        return dsl
                .select()
                .from(COLLECTION_ENTITY)
                .where(COLLECTION_ENTITY.ID.eq(entity.sourceFk))
                .fetchOne {
                    RecordMappers.mapToCollectionEntity(it)
                }
    }

    override fun fetchBySlugAndContainerId(slug: String, containerId: Int): CollectionEntity {
        return dsl
                .select()
                .from(COLLECTION_ENTITY)
                .where(COLLECTION_ENTITY.SLUG.eq(slug).and(COLLECTION_ENTITY.RC_FK.eq(containerId)))
                .fetchOne {
                    RecordMappers.mapToCollectionEntity(it)
                }
    }

    @Synchronized
    override fun insert(entity: CollectionEntity): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the collection entity
        dsl
                .insertInto(
                        COLLECTION_ENTITY,
                        COLLECTION_ENTITY.PARENT_FK,
                        COLLECTION_ENTITY.SOURCE_FK,
                        COLLECTION_ENTITY.SLUG,
                        COLLECTION_ENTITY.TITLE,
                        COLLECTION_ENTITY.LABEL,
                        COLLECTION_ENTITY.SORT,
                        COLLECTION_ENTITY.RC_FK
                )
                .values(
                        entity.parentFk,
                        entity.sourceFk,
                        entity.slug,
                        entity.title,
                        entity.label,
                        entity.sort,
                        entity.metadataFk
                )
                .execute()

        // Grab the resulting id (assumed largest value)
        return dsl
                .select(max(COLLECTION_ENTITY.ID))
                .from(COLLECTION_ENTITY)
                .fetchOne {
                    it.getValue(max(COLLECTION_ENTITY.ID))
                }
    }

    override fun fetchById(id: Int): CollectionEntity {
        return dsl
                .select()
                .from(COLLECTION_ENTITY)
                .where(COLLECTION_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToCollectionEntity(it)
                }
    }

    override fun fetchAll(): List<CollectionEntity> {
        return dsl
                .select()
                .from(COLLECTION_ENTITY)
                .fetch {
                    RecordMappers.mapToCollectionEntity(it)
                }
    }

    override fun update(entity: CollectionEntity) {
        dsl
                .update(COLLECTION_ENTITY)
                .set(COLLECTION_ENTITY.PARENT_FK, entity.parentFk)
                .set(COLLECTION_ENTITY.SOURCE_FK, entity.sourceFk)
                .set(COLLECTION_ENTITY.SLUG, entity.slug)
                .set(COLLECTION_ENTITY.TITLE, entity.title)
                .set(COLLECTION_ENTITY.LABEL, entity.label)
                .set(COLLECTION_ENTITY.SORT, entity.sort)
                .set(COLLECTION_ENTITY.RC_FK, entity.metadataFk)
                .where(COLLECTION_ENTITY.ID.eq(entity.id))
                .execute()
    }

    override fun delete(entity: CollectionEntity) {
        dsl
                .deleteFrom(COLLECTION_ENTITY)
                .where(COLLECTION_ENTITY.ID.eq(entity.id))
                .execute()
    }

}