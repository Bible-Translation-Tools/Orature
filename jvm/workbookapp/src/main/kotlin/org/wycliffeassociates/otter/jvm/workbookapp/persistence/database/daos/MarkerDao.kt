package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.MARKER_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.MarkerEntity

class MarkerDao(
    private val instanceDsl: DSLContext
) {
    fun fetchByTakeId(id: Int, dsl: DSLContext = instanceDsl): List<MarkerEntity> {
        return dsl
            .select()
            .from(MARKER_ENTITY)
            .where(MARKER_ENTITY.TAKE_FK.eq(id))
            .fetch {
                RecordMappers.mapToMarkerEntity(it)
            }
    }

    @Synchronized
    fun insert(entity: MarkerEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the marker entity
        dsl
            .insertInto(
                MARKER_ENTITY,
                MARKER_ENTITY.TAKE_FK,
                MARKER_ENTITY.NUMBER,
                MARKER_ENTITY.POSITION,
                MARKER_ENTITY.LABEL
            )
            .values(
                entity.takeFk,
                entity.number,
                entity.position,
                entity.label
            )
            .execute()

        // Fetch and return the resulting ID
        return dsl
            .select(max(MARKER_ENTITY.ID))
            .from(MARKER_ENTITY)
            .fetchOne {
                it.getValue(max(MARKER_ENTITY.ID))
            }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): MarkerEntity {
        return dsl
            .select()
            .from(MARKER_ENTITY)
            .where(MARKER_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToMarkerEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<MarkerEntity> {
        return dsl
            .select()
            .from(MARKER_ENTITY)
            .fetch {
                RecordMappers.mapToMarkerEntity(it)
            }
    }

    fun update(entity: MarkerEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(MARKER_ENTITY)
            .set(MARKER_ENTITY.TAKE_FK, entity.takeFk)
            .set(MARKER_ENTITY.NUMBER, entity.number)
            .set(MARKER_ENTITY.POSITION, entity.position)
            .set(MARKER_ENTITY.LABEL, entity.label)
            .where(MARKER_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: MarkerEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(MARKER_ENTITY)
            .where(MARKER_ENTITY.ID.eq(entity.id))
            .execute()
    }
}