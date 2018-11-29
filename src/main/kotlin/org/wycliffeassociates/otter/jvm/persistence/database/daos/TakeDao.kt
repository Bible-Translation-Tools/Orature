package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.TAKE_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.TakeEntity

class TakeDao(
        private val instanceDsl: DSLContext
) {
    fun fetchByContentId(id: Int, dsl: DSLContext = instanceDsl): List<TakeEntity> {
        return dsl
                .select()
                .from(TAKE_ENTITY)
                .where(TAKE_ENTITY.CONTENT_FK.eq(id))
                .fetch {
                    RecordMappers.mapToTakeEntity(it)
                }
    }

    @Synchronized
    fun insert(entity: TakeEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the take entity
        dsl
                .insertInto(
                        TAKE_ENTITY,
                        TAKE_ENTITY.CONTENT_FK,
                        TAKE_ENTITY.FILENAME,
                        TAKE_ENTITY.PATH,
                        TAKE_ENTITY.NUMBER,
                        TAKE_ENTITY.TIMESTAMP,
                        TAKE_ENTITY.PLAYED
                )
                .values(
                        entity.contentFk,
                        entity.filename,
                        entity.filepath,
                        entity.number,
                        entity.timestamp,
                        entity.played
                )
                .execute()

        // Fetch and return the resulting ID
        return dsl
                .select(max(TAKE_ENTITY.ID))
                .from(TAKE_ENTITY)
                .fetchOne {
                    it.getValue(max(TAKE_ENTITY.ID))
                }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): TakeEntity {
        return dsl
                .select()
                .from(TAKE_ENTITY)
                .where(TAKE_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToTakeEntity(it)
                }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<TakeEntity> {
        return dsl
                .select()
                .from(TAKE_ENTITY)
                .fetch {
                    RecordMappers.mapToTakeEntity(it)
                }
    }

    fun update(entity: TakeEntity, dsl: DSLContext = instanceDsl) {
        dsl
                .update(TAKE_ENTITY)
                .set(TAKE_ENTITY.CONTENT_FK, entity.contentFk)
                .set(TAKE_ENTITY.FILENAME, entity.filename)
                .set(TAKE_ENTITY.PATH, entity.filepath)
                .set(TAKE_ENTITY.NUMBER, entity.number)
                .set(TAKE_ENTITY.TIMESTAMP, entity.timestamp)
                .set(TAKE_ENTITY.PLAYED, entity.played)
                .where(TAKE_ENTITY.ID.eq(entity.id))
                .execute()
    }

    fun delete(entity: TakeEntity, dsl: DSLContext = instanceDsl) {
        dsl
                .deleteFrom(TAKE_ENTITY)
                .where(TAKE_ENTITY.ID.eq(entity.id))
                .execute()
    }

}