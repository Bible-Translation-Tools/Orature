package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.CONTENT_DERIVATIVE
import jooq.Tables.CONTENT_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.ChunkEntity

class ChunkDao(
        private val dsl: DSLContext
) : IChunkDao {
    override fun fetchByCollectionId(collectionId: Int): List<ChunkEntity> {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
                .fetch {
                    RecordMappers.mapToChunkEntity(it)
                }
    }

    override fun fetchSources(entity: ChunkEntity): List<ChunkEntity> {
        val sourceIds = dsl
                .select(CONTENT_DERIVATIVE.SOURCE_FK)
                .from(CONTENT_DERIVATIVE)
                .where(CONTENT_DERIVATIVE.CONTENT_FK.eq(entity.id))
                .fetch {
                    it.getValue(CONTENT_DERIVATIVE.SOURCE_FK)
                }

        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.ID.`in`(sourceIds))
                .fetch {
                    RecordMappers.mapToChunkEntity(it)
                }
    }

    override fun updateSources(entity: ChunkEntity, sources: List<ChunkEntity>) {
        // Delete the existing sources
        dsl
                .deleteFrom(CONTENT_DERIVATIVE)
                .where(CONTENT_DERIVATIVE.CONTENT_FK.eq(entity.id))
                .execute()

        // Add the sources
        if (sources.isNotEmpty()) {
            sources.forEach {
                val insertStatement = dsl
                        .insertInto(CONTENT_DERIVATIVE, CONTENT_DERIVATIVE.CONTENT_FK, CONTENT_DERIVATIVE.SOURCE_FK)
                        .values(entity.id, it.id)
                        .execute()
            }
        }
    }

    override fun insert(entity: ChunkEntity): Int {
        if (entity.id != 0) throw InsertionException("Entity ID was not 0")

        // Insert the new chunk entity
        dsl
                .insertInto(
                        CONTENT_ENTITY,
                        CONTENT_ENTITY.COLLECTION_FK,
                        CONTENT_ENTITY.SORT,
                        CONTENT_ENTITY.START,
                        CONTENT_ENTITY.LABEL,
                        CONTENT_ENTITY.SELECTED_TAKE_FK
                )
                .values(
                        entity.collectionFk,
                        entity.sort,
                        entity.start,
                        entity.labelKey,
                        entity.selectedTakeFk
                )
                .execute()

        // Get the ID
        return dsl
                .select(max(CONTENT_ENTITY.ID))
                .from(CONTENT_ENTITY)
                .fetchOne {
                    it.getValue(max(CONTENT_ENTITY.ID))
                }
    }

    override fun fetchById(id: Int): ChunkEntity {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToChunkEntity(it)
                }
    }

    override fun fetchAll(): List<ChunkEntity> {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .fetch {
                    RecordMappers.mapToChunkEntity(it)
                }
    }

    override fun update(entity: ChunkEntity) {
        dsl
                .update(CONTENT_ENTITY)
                .set(CONTENT_ENTITY.SORT, entity.sort)
                .set(CONTENT_ENTITY.LABEL, entity.labelKey)
                .set(CONTENT_ENTITY.START, entity.start)
                .set(CONTENT_ENTITY.COLLECTION_FK, entity.collectionFk)
                .set(CONTENT_ENTITY.SELECTED_TAKE_FK, entity.selectedTakeFk)
                .where(CONTENT_ENTITY.ID.eq(entity.id))
                .execute()
    }

    override fun delete(entity: ChunkEntity) {
        dsl
                .deleteFrom(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.ID.eq(entity.id))
                .execute()
    }

}