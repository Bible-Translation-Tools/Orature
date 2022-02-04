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

import jooq.Tables.SUBTREE_HAS_RESOURCE
import org.jooq.DSLContext

class SubtreeHasResourceDao(private val instanceDsl: DSLContext) {
    fun insert(collectionId: Int, dublinCoreId: Int, dsl: DSLContext = instanceDsl): Int {
        return dsl
            .insertInto(
                SUBTREE_HAS_RESOURCE,
                SUBTREE_HAS_RESOURCE.COLLECTION_FK,
                SUBTREE_HAS_RESOURCE.DUBLIN_CORE_FK
            )
            .values(
                collectionId,
                dublinCoreId
            )
            .execute()
    }

    fun insert(collectionIdsToDublinCoreIds: Sequence<Pair<Int, Int>>, dsl: DSLContext = instanceDsl): Int {
        val baseQuery = dsl
            .insertInto(
                SUBTREE_HAS_RESOURCE,
                SUBTREE_HAS_RESOURCE.COLLECTION_FK,
                SUBTREE_HAS_RESOURCE.DUBLIN_CORE_FK
            )
        val query = collectionIdsToDublinCoreIds
            .fold(baseQuery) { q, p ->
                q.values(p.first, p.second)
            }
        return query.execute()
    }

    fun fetchDublinCoreIdsByCollectionId(id: Int, dsl: DSLContext = instanceDsl): List<Int> {
        return dsl
            .select(SUBTREE_HAS_RESOURCE.DUBLIN_CORE_FK)
            .from(SUBTREE_HAS_RESOURCE)
            .where(SUBTREE_HAS_RESOURCE.COLLECTION_FK.eq(id))
            .fetch(SUBTREE_HAS_RESOURCE.DUBLIN_CORE_FK)
    }
}
