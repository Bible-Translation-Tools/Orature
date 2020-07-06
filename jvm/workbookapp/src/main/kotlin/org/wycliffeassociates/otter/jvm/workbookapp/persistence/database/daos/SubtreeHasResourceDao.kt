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
