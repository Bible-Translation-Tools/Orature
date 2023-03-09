package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables
import org.jooq.DSLContext

class VersificationDao(
    private val instanceDsl: DSLContext
) {

    fun fetchVersificationFile(slug: String, dsl: DSLContext = instanceDsl): String? {
        return dsl
            .select(Tables.VERSIFICATION_ENTITY.PATH)
            .from(Tables.VERSIFICATION_ENTITY)
            .where(Tables.VERSIFICATION_ENTITY.SLUG.eq(slug))
            .fetchOne()
            ?.get(Tables.VERSIFICATION_ENTITY.PATH)
    }

    fun insertVersification(slug: String, path: String, dsl: DSLContext = instanceDsl) {
        dsl
            .insertInto(
                Tables.VERSIFICATION_ENTITY,
                Tables.VERSIFICATION_ENTITY.SLUG,
                Tables.VERSIFICATION_ENTITY.PATH
            )
            .values(slug, path)
            .execute()
    }
}