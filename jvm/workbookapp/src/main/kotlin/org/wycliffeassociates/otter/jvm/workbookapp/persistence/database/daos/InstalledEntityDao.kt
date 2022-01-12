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
