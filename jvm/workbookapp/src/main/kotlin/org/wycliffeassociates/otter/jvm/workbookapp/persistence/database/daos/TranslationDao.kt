/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TranslationEntity

class TranslationDao(
    private val instanceDsl: DSLContext
) {
    fun fetchBySourceId(id: Int, dsl: DSLContext = instanceDsl): List<TranslationEntity> {
        return dsl
            .select()
            .from(Tables.TRANSLATION_ENTITY)
            .where(Tables.TRANSLATION_ENTITY.SOURCE_FK.eq(id))
            .fetch {
                RecordMappers.mapToTranslationEntity(it)
            }
    }

    fun fetchByTargetId(id: Int, dsl: DSLContext = instanceDsl): List<TranslationEntity> {
        return dsl
            .select()
            .from(Tables.TRANSLATION_ENTITY)
            .where(Tables.TRANSLATION_ENTITY.TARGET_FK.eq(id))
            .fetch {
                RecordMappers.mapToTranslationEntity(it)
            }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): TranslationEntity {
        return dsl
            .select()
            .from(Tables.TRANSLATION_ENTITY)
            .where(Tables.TRANSLATION_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToTranslationEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<TranslationEntity> {
        return dsl
            .select()
            .from(Tables.TRANSLATION_ENTITY)
            .fetch {
                RecordMappers.mapToTranslationEntity(it)
            }
    }

    @Synchronized
    fun insert(entity: TranslationEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the translation entity
        dsl
            .insertInto(
                Tables.TRANSLATION_ENTITY,
                Tables.TRANSLATION_ENTITY.SOURCE_FK,
                Tables.TRANSLATION_ENTITY.TARGET_FK
            )
            .values(
                entity.sourceFk,
                entity.targetFk
            )
            .execute()

        // Fetch and return the resulting ID
        return dsl
            .select(DSL.max(Tables.TRANSLATION_ENTITY.ID))
            .from(Tables.TRANSLATION_ENTITY)
            .fetchOne {
                it.getValue(DSL.max(Tables.TRANSLATION_ENTITY.ID))
            }
    }
}
