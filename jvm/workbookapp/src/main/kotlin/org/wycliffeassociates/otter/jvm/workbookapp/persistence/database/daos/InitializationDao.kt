package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables
import jooq.Tables.INITIALIZATION
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException

class InitializationDao(
    private val instanceDsl: DSLContext
) {
    fun fetchAll(dsl: DSLContext = instanceDsl): List<Initialization> {
        return dsl
            .select()
            .from(Tables.INITIALIZATION)
            .fetch {
                RecordMappers.mapToInitialization(it)
            }
    }

    @Synchronized
    fun insert(entity: Initialization, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID was not 0")

        dsl
            .insertInto(
                INITIALIZATION,
                INITIALIZATION.NAME,
                INITIALIZATION.VERSION,
                INITIALIZATION.INITIALIZED
            )
            .values(
                entity.name,
                entity.version,
                if (entity.initialized) 1 else 0
            )
            .execute()

        return dsl
            .select(DSL.max(INITIALIZATION.ID))
            .from(Tables.CONTENT_ENTITY)
            .fetchOne {
                it.getValue(DSL.max(INITIALIZATION.ID))
            }
    }

    fun update(entity: Initialization, dsl: DSLContext = instanceDsl) {
        dsl
            .update(INITIALIZATION)
            .set(INITIALIZATION.NAME, entity.name)
            .set(INITIALIZATION.VERSION, entity.version)
            .set(INITIALIZATION.INITIALIZED, if (entity.initialized) 1 else 0)
            .where(INITIALIZATION.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: Initialization, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(INITIALIZATION)
            .where(INITIALIZATION.ID.eq(entity.id))
            .execute()
    }
}