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

import jooq.Tables.RESOURCE_LINK
import org.jooq.DSLContext
import org.jooq.Record3
import org.jooq.Select
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceLinkEntity

class ResourceLinkDao(
    private val instanceDsl: DSLContext
) {
    fun fetchByContentId(id: Int, dsl: DSLContext = instanceDsl): List<ResourceLinkEntity> {
        return dsl
            .select()
            .from(RESOURCE_LINK)
            .where(RESOURCE_LINK.CONTENT_FK.eq(id))
            .fetch {
                RecordMappers.mapToResourceLinkEntity(it)
            }
    }

    fun fetchByCollectionId(id: Int, dsl: DSLContext = instanceDsl): List<ResourceLinkEntity> {
        return dsl
            .select()
            .from(RESOURCE_LINK)
            .where(RESOURCE_LINK.COLLECTION_FK.eq(id))
            .fetch {
                RecordMappers.mapToResourceLinkEntity(it)
            }
    }

    @Synchronized
    fun insert(entity: ResourceLinkEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the resource link entity
        dsl
            .insertInto(
                RESOURCE_LINK,
                RESOURCE_LINK.RESOURCE_CONTENT_FK,
                RESOURCE_LINK.CONTENT_FK,
                RESOURCE_LINK.COLLECTION_FK,
                RESOURCE_LINK.DUBLIN_CORE_FK
            )
            .values(
                entity.resourceContentFk,
                entity.contentFk,
                entity.collectionFk,
                entity.dublinCoreFk
            )
            .execute()

        // Fetch and return the resulting ID
        return dsl
            .select(max(RESOURCE_LINK.ID))
            .from(RESOURCE_LINK)
            .fetchOne {
                it.getValue(max(RESOURCE_LINK.ID))
            }
    }

    @Synchronized
    fun insertNoReturn(vararg entities: ResourceLinkEntity, dsl: DSLContext = instanceDsl) {
        if (entities.any { it.id != 0 }) throw InsertionException("Entity ID is not 0")
        val bareInsert = dsl
            .insertInto(
                RESOURCE_LINK,
                RESOURCE_LINK.RESOURCE_CONTENT_FK,
                RESOURCE_LINK.CONTENT_FK,
                RESOURCE_LINK.COLLECTION_FK,
                RESOURCE_LINK.DUBLIN_CORE_FK
            )
        val insertWithValues = entities.fold(bareInsert) { query, entity ->
            query.values(
                entity.resourceContentFk,
                entity.contentFk,
                entity.collectionFk,
                entity.dublinCoreFk
            )
        }
        insertWithValues.execute()
    }

    /** @param select a triple record containing values for main content ID, resource content ID, dublinCore ID */
    @Synchronized
    fun insertContentResourceNoReturn(select: Select<Record3<Int, Int, Int>>, dsl: DSLContext = instanceDsl) {
        dsl
            .insertInto(
                RESOURCE_LINK,
                RESOURCE_LINK.CONTENT_FK,
                RESOURCE_LINK.RESOURCE_CONTENT_FK,
                RESOURCE_LINK.DUBLIN_CORE_FK
            )
            .select(select)
            .execute()
    }

    /** @param select a triple record containing values for collection ID, resource content ID, dublinCore ID */
    @Synchronized
    fun insertCollectionResourceNoReturn(select: Select<Record3<Int, Int, Int>>, dsl: DSLContext = instanceDsl) {
        dsl
            .insertInto(
                RESOURCE_LINK,
                RESOURCE_LINK.COLLECTION_FK,
                RESOURCE_LINK.RESOURCE_CONTENT_FK,
                RESOURCE_LINK.DUBLIN_CORE_FK
            )
            .select(select)
            .execute()
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): ResourceLinkEntity {
        return dsl
            .select()
            .from(RESOURCE_LINK)
            .where(RESOURCE_LINK.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToResourceLinkEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<ResourceLinkEntity> {
        return dsl
            .select()
            .from(RESOURCE_LINK)
            .fetch {
                RecordMappers.mapToResourceLinkEntity(it)
            }
    }

    fun update(entity: ResourceLinkEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(RESOURCE_LINK)
            .set(RESOURCE_LINK.RESOURCE_CONTENT_FK, entity.resourceContentFk)
            .set(RESOURCE_LINK.CONTENT_FK, entity.contentFk)
            .set(RESOURCE_LINK.COLLECTION_FK, entity.collectionFk)
            .set(RESOURCE_LINK.DUBLIN_CORE_FK, entity.dublinCoreFk)
            .where(RESOURCE_LINK.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: ResourceLinkEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(RESOURCE_LINK)
            .where(RESOURCE_LINK.ID.eq(entity.id))
            .execute()
    }
}
