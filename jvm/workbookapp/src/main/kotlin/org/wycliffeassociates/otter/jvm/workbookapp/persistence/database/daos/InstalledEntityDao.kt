package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.INSTALLED_ENTITY
import org.jooq.DSLContext
import org.wycliffeassociates.otter.common.persistence.config.Installable

class InstalledEntityDao(
    private val instanceDsl: DSLContext
) {
    fun upsert(entity: Installable, dsl: DSLContext = instanceDsl) {
        dsl
            .insertInto(
                INSTALLED_ENTITY,
                INSTALLED_ENTITY.NAME,
                INSTALLED_ENTITY.VERSION
            )
            .values(
                entity.name,
                entity.version
            )
            .onDuplicateKeyUpdate()
            .set(INSTALLED_ENTITY.VERSION, entity.version)
            .execute()
    }

    fun fetchVersion(entity: Installable, dsl: DSLContext = instanceDsl): Int? {
        val set =
            dsl
                .select(INSTALLED_ENTITY.VERSION)
                .from(INSTALLED_ENTITY)
                .where(INSTALLED_ENTITY.NAME.eq(entity.name))
                .fetch { record -> record.get(INSTALLED_ENTITY.VERSION) }
        return set.firstOrNull()
    }
}