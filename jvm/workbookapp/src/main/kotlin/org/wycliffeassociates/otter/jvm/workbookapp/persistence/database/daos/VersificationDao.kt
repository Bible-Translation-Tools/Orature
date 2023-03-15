package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables
import jooq.tables.VersificationEntity.VERSIFICATION_ENTITY
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.PreferenceEntity

class VersificationDao(
    private val instanceDsl: DSLContext
) {

    fun fetchVersificationFile(slug: String, dsl: DSLContext = instanceDsl): String? {
        return dsl
            .select(VERSIFICATION_ENTITY.PATH)
            .from(VERSIFICATION_ENTITY)
            .where(VERSIFICATION_ENTITY.SLUG.eq(slug))
            .fetchOne()
            ?.get(VERSIFICATION_ENTITY.PATH)
    }

    @Synchronized
    fun insert(slug: String, path: String, dsl: DSLContext = instanceDsl) {
        dsl
            .insertInto(
                Tables.VERSIFICATION_ENTITY,
                Tables.VERSIFICATION_ENTITY.SLUG,
                Tables.VERSIFICATION_ENTITY.PATH
            )
            .values(
                slug,
                path
            )
            .execute()
    }

    @Synchronized
    fun update(slug: String, path: String, dsl: DSLContext = instanceDsl) {
        dsl
            .update(Tables.VERSIFICATION_ENTITY)
            .set(Tables.VERSIFICATION_ENTITY.PATH, path)
            .where(Tables.VERSIFICATION_ENTITY.SLUG.eq(slug))
            .execute()
    }

    fun upsert(slug: String, path: String, dsl: DSLContext = instanceDsl) {
        try {
            insert(slug, path)
        } catch (e: DataAccessException) {
            update(slug, path)
        }
    }
}