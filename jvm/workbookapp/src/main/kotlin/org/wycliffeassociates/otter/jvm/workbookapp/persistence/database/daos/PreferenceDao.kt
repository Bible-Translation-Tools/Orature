package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.PREFERENCES
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.PreferenceEntity

class PreferenceDao(
    private val instanceDsl: DSLContext
) {

    fun fetchByKey(key: String, dsl: DSLContext = instanceDsl): PreferenceEntity {
        return dsl
            .select()
            .from(PREFERENCES)
            .where(PREFERENCES.KEY.eq(key))
            .fetchOne {
                RecordMappers.mapToPreferencesEntity(it)
            }
    }

    @Synchronized
    fun insert(entity: PreferenceEntity, dsl: DSLContext = instanceDsl) {
        // Insert the language entity
        dsl
            .insertInto(
                PREFERENCES,
                PREFERENCES.KEY,
                PREFERENCES.VALUE
            )
            .values(
                entity.key,
                entity.value
            )
            .execute()
    }

    fun update(entity: PreferenceEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(PREFERENCES)
            .set(PREFERENCES.VALUE, entity.value)
            .where(PREFERENCES.KEY.eq(entity.key))
            .execute()
    }

    fun upsert(entity: PreferenceEntity, dsl: DSLContext = instanceDsl) {
        try {
            insert(entity, dsl)
        } catch (e: DataAccessException) {
            update(entity, dsl)
        }
    }

    fun delete(entity: PreferenceEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(PREFERENCES)
            .where(PREFERENCES.KEY.eq(entity.key))
            .execute()
    }
}
