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