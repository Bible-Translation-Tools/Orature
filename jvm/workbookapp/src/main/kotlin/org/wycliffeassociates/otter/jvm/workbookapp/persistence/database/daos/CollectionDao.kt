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

import jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.DSL.count
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity

class CollectionDao(
    private val instanceDsl: DSLContext
) {
    fun fetchChildren(entity: CollectionEntity, dsl: DSLContext = instanceDsl): List<CollectionEntity> {
        return dsl
            .select()
            .from(COLLECTION_ENTITY)
            .where(COLLECTION_ENTITY.PARENT_FK.eq(entity.id))
            .orderBy(COLLECTION_ENTITY.SORT)
            .fetch(RecordMappers.Companion::mapToCollectionEntity)
    }

    fun fetchSource(entity: CollectionEntity, dsl: DSLContext = instanceDsl): CollectionEntity {
        return dsl
            .select()
            .from(COLLECTION_ENTITY)
            .where(COLLECTION_ENTITY.ID.eq(entity.sourceFk))
            .fetchOne {
                RecordMappers.mapToCollectionEntity(it)
            }
    }

    /**
     * Fetches the collection by slug, container id, and label. If a label is not provided,
     * assume it is the project level collection (book)
     */
    fun fetch(
        slug: String,
        containerId: Int,
        label: String = "project",
        dsl: DSLContext = instanceDsl
    ): CollectionEntity? {
        return dsl
            .select()
            .from(COLLECTION_ENTITY)
            .where(
                COLLECTION_ENTITY.SLUG.eq(slug)
                    .and(COLLECTION_ENTITY.DUBLIN_CORE_FK.eq(containerId))
                    .and(COLLECTION_ENTITY.LABEL.eq(label))
            )
            .fetchOne()
            ?.let {
                RecordMappers.mapToCollectionEntity(it)
            }
    }

    @Synchronized
    fun insert(entity: CollectionEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the collection entity
        dsl
            .insertInto(
                COLLECTION_ENTITY,
                COLLECTION_ENTITY.PARENT_FK,
                COLLECTION_ENTITY.SOURCE_FK,
                COLLECTION_ENTITY.SLUG,
                COLLECTION_ENTITY.TITLE,
                COLLECTION_ENTITY.LABEL,
                COLLECTION_ENTITY.SORT,
                COLLECTION_ENTITY.DUBLIN_CORE_FK,
                COLLECTION_ENTITY.MODIFIED_TS
            )
            .values(
                entity.parentFk,
                entity.sourceFk,
                entity.slug,
                entity.title,
                entity.label,
                entity.sort,
                entity.dublinCoreFk,
                entity.modifiedTs
            )
            .execute()

        // Grab the resulting id (assumed largest value)
        return dsl
            .select(max(COLLECTION_ENTITY.ID))
            .from(COLLECTION_ENTITY)
            .fetchOne {
                it.getValue(max(COLLECTION_ENTITY.ID))
            }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): CollectionEntity {
        return dsl
            .select()
            .from(COLLECTION_ENTITY)
            .where(COLLECTION_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToCollectionEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<CollectionEntity> {
        return dsl
            .select()
            .from(COLLECTION_ENTITY)
            .fetch {
                RecordMappers.mapToCollectionEntity(it)
            }
    }

    fun fetchByLabel(label: String, dsl: DSLContext = instanceDsl): List<CollectionEntity> {
        return dsl
            .select()
            .from(COLLECTION_ENTITY)
            .where(COLLECTION_ENTITY.LABEL.eq(label))
            .fetch {
                RecordMappers.mapToCollectionEntity(it)
            }
    }

    fun update(entity: CollectionEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(COLLECTION_ENTITY)
            .set(COLLECTION_ENTITY.PARENT_FK, entity.parentFk)
            .set(COLLECTION_ENTITY.SOURCE_FK, entity.sourceFk)
            .set(COLLECTION_ENTITY.SLUG, entity.slug)
            .set(COLLECTION_ENTITY.TITLE, entity.title)
            .set(COLLECTION_ENTITY.LABEL, entity.label)
            .set(COLLECTION_ENTITY.SORT, entity.sort)
            .set(COLLECTION_ENTITY.DUBLIN_CORE_FK, entity.dublinCoreFk)
            .set(COLLECTION_ENTITY.MODIFIED_TS, entity.modifiedTs)
            .where(COLLECTION_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: CollectionEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(COLLECTION_ENTITY)
            .where(COLLECTION_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun collectionsWithoutTakes(
        projectEntity: CollectionEntity,
        dsl: DSLContext = instanceDsl
    ): List<CollectionEntity> {
        val id = COLLECTION_ENTITY.ID.`as`("id")
        val contentid = CONTENT_ENTITY.ID.`as`("contentid")
        val chapterid = CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid")
        val takecount = count(TAKE_ENTITY.FILENAME).`as`("takecount")

        val chapterIdsInBook = dsl
            .select(COLLECTION_ENTITY.ID)
            .from(COLLECTION_ENTITY)
            .where(COLLECTION_ENTITY.PARENT_FK.eq(projectEntity.id))

        val contentAndChapterIdsInBook = dsl
            .select(contentid, chapterid)
            .from(CONTENT_ENTITY)
            .where(
                CONTENT_ENTITY.COLLECTION_FK.`in`(chapterIdsInBook).and(CONTENT_ENTITY.TYPE_FK.eq(1))
            )

        val contentAndTakeCountInBook = dsl
            .select(takecount, asterisk())
            .from(
                contentAndChapterIdsInBook.asTable()
                    .leftJoin(TAKE_ENTITY).on(TAKE_ENTITY.CONTENT_FK.eq(contentid))
            )
            .groupBy(contentid)

        val collectionsWithTakes = dsl.select(id)
            .from(
                COLLECTION_ENTITY
                    .leftJoin(
                        contentAndTakeCountInBook
                    ).on(COLLECTION_ENTITY.ID.eq(chapterid))
            )
            .where(takecount.greaterThan(0))
            .groupBy(chapterid)

        val query = dsl
            .select()
            .from(COLLECTION_ENTITY)
            .where(
                COLLECTION_ENTITY.ID.`notIn`(
                    collectionsWithTakes
                ).and(COLLECTION_ENTITY.PARENT_FK.eq(projectEntity.id))
            )
        return query.fetch {
            println(it)
            RecordMappers.mapToCollectionEntity(it)
        }
    }
//    select * from collection_entity where collection_entity.id
//    in (select id from  collection_entity left join (select contentid, chapterid, count(filename) as takecount
//    from (select id as contentid, collection_fk as chapterid from content_entity
//    where content_entity.collection_fk in
//    (select collection_entity.id from collection_entity
//    where collection_entity.parent_fk == 1257) and type_fk == 1)
//    left join take_entity on take_entity.content_fk == contentid
//    group by contentid) on collection_entity.id == chapterid where takecount > 0 group by chapterid)

}
