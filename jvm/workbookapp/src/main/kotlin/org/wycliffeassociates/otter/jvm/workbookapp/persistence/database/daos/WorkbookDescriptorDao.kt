package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.WORKBOOK_DESCRIPTOR_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.WorkbookDescriptorEntity

class WorkbookDescriptorDao(
    private val instanceDsl: DSLContext,
) {
    fun fetch(
        sourceId: Int,
        targetId: Int,
        typeId: Int,
        dsl: DSLContext = instanceDsl,
    ): WorkbookDescriptorEntity? {
        return dsl
            .select()
            .from(WORKBOOK_DESCRIPTOR_ENTITY)
            .where(WORKBOOK_DESCRIPTOR_ENTITY.SOURCE_FK.eq(sourceId))
            .and(WORKBOOK_DESCRIPTOR_ENTITY.TARGET_FK.eq(targetId))
            .and(WORKBOOK_DESCRIPTOR_ENTITY.TYPE_FK.eq(typeId))
            .fetchOne()
            ?.let {
                RecordMappers.mapToWorkbookDescriptorEntity(it)
            }
    }

    fun fetchById(
        id: Int,
        dsl: DSLContext = instanceDsl,
    ): WorkbookDescriptorEntity? {
        return dsl
            .select()
            .from(WORKBOOK_DESCRIPTOR_ENTITY)
            .where(WORKBOOK_DESCRIPTOR_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToWorkbookDescriptorEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<WorkbookDescriptorEntity> {
        return dsl
            .select()
            .from(WORKBOOK_DESCRIPTOR_ENTITY)
            .fetch {
                RecordMappers.mapToWorkbookDescriptorEntity(it)
            }
    }

    @Synchronized
    fun insert(
        entity: WorkbookDescriptorEntity,
        dsl: DSLContext = instanceDsl,
    ): Int {
        if (entity.id != 0) throw InsertionException("Entity ID must be 0. Found ${entity.id}")

        dsl
            .insertInto(
                WORKBOOK_DESCRIPTOR_ENTITY,
                WORKBOOK_DESCRIPTOR_ENTITY.SOURCE_FK,
                WORKBOOK_DESCRIPTOR_ENTITY.TARGET_FK,
                WORKBOOK_DESCRIPTOR_ENTITY.TYPE_FK,
            )
            .values(
                entity.sourceFk,
                entity.targetFk,
                entity.typeFk,
            )
            .execute()

        return dsl
            .select(max(WORKBOOK_DESCRIPTOR_ENTITY.ID))
            .from(WORKBOOK_DESCRIPTOR_ENTITY)
            .fetchOne {
                it.getValue(max(WORKBOOK_DESCRIPTOR_ENTITY.ID))
            }!!
    }

    fun update(
        entity: WorkbookDescriptorEntity,
        dsl: DSLContext = instanceDsl,
    ) {
        dsl
            .update(WORKBOOK_DESCRIPTOR_ENTITY)
            .set(WORKBOOK_DESCRIPTOR_ENTITY.SOURCE_FK, entity.sourceFk)
            .set(WORKBOOK_DESCRIPTOR_ENTITY.TARGET_FK, entity.targetFk)
            .set(WORKBOOK_DESCRIPTOR_ENTITY.TYPE_FK, entity.typeFk)
            .where(WORKBOOK_DESCRIPTOR_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(
        entity: WorkbookDescriptorEntity,
        dsl: DSLContext = instanceDsl,
    ) {
        dsl
            .deleteFrom(WORKBOOK_DESCRIPTOR_ENTITY)
            .where(WORKBOOK_DESCRIPTOR_ENTITY.ID.eq(entity.id))
            .execute()
    }
}
