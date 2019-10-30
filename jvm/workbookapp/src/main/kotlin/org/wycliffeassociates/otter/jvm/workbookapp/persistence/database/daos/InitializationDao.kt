package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.INITIALIZATION
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.wycliffeassociates.otter.common.persistence.config.Installable

class InitializationDao(
    private val instanceDsl: DSLContext
) {
    private fun update(entity: Installable, dsl: DSLContext = instanceDsl) {
        dsl
            .update(INITIALIZATION)
            .set(INITIALIZATION.VERSION, entity.version)
            .where(INITIALIZATION.NAME.eq(entity.name))
            .execute()
    }

    private fun insert(entity: Installable, dsl: DSLContext = instanceDsl) {
        dsl
            .insertInto(
                INITIALIZATION,
                INITIALIZATION.NAME,
                INITIALIZATION.VERSION
            )
            .values(
                entity.name,
                entity.version
            )
            .execute()
    }

    fun upsert(entity: Installable, dsl: DSLContext = instanceDsl) {
        try {
            insert(entity, dsl)
        } catch (e: DataAccessException) {
            update(entity, dsl)
        }
    }

    fun fetchVersion(name: String, dsl: DSLContext = instanceDsl): Int? {
        val set = dsl
            .select(INITIALIZATION.VERSION)
            .from(INITIALIZATION)
            .where(INITIALIZATION.NAME.eq(name))
            .fetchResultSet()
        
    }
}