/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
