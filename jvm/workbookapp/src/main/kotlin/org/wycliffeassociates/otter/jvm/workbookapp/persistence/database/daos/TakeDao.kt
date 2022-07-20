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
import org.jooq.impl.DSL.max
import org.jooq.impl.DSL.select
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TakeEntity

class TakeDao(
    private val instanceDsl: DSLContext
) {
    fun fetchByContentId(
        id: Int,
        includeDeleted: Boolean = false,
        dsl: DSLContext = instanceDsl
    ): List<TakeEntity> {
        val baseQuery = dsl
            .select()
            .from(TAKE_ENTITY)
            .where(TAKE_ENTITY.CONTENT_FK.eq(id))
        val query = when {
            includeDeleted -> baseQuery
            else -> baseQuery.and(TAKE_ENTITY.DELETED_TS.isNull)
        }
        return query.fetch(RecordMappers.Companion::mapToTakeEntity)
    }

    @Synchronized
    fun insert(entity: TakeEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the take entity
        dsl
            .insertInto(
                TAKE_ENTITY,
                TAKE_ENTITY.CONTENT_FK,
                TAKE_ENTITY.FILENAME,
                TAKE_ENTITY.PATH,
                TAKE_ENTITY.NUMBER,
                TAKE_ENTITY.CREATED_TS,
                TAKE_ENTITY.DELETED_TS,
                TAKE_ENTITY.PLAYED
            )
            .values(
                entity.contentFk,
                entity.filename,
                entity.filepath,
                entity.number,
                entity.createdTs,
                entity.deletedTs,
                entity.played
            )
            .execute()

        // Fetch and return the resulting ID
        return dsl
            .select(max(TAKE_ENTITY.ID))
            .from(TAKE_ENTITY)
            .fetchOne {
                it.getValue(max(TAKE_ENTITY.ID))
            }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): TakeEntity {
        return dsl
            .select()
            .from(TAKE_ENTITY)
            .where(TAKE_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToTakeEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<TakeEntity> {
        return dsl
            .select()
            .from(TAKE_ENTITY)
            .fetch {
                RecordMappers.mapToTakeEntity(it)
            }
    }

    fun update(entity: TakeEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(TAKE_ENTITY)
            .set(TAKE_ENTITY.CONTENT_FK, entity.contentFk)
            .set(TAKE_ENTITY.FILENAME, entity.filename)
            .set(TAKE_ENTITY.PATH, entity.filepath)
            .set(TAKE_ENTITY.NUMBER, entity.number)
            .set(TAKE_ENTITY.CREATED_TS, entity.createdTs)
            .set(TAKE_ENTITY.DELETED_TS, entity.deletedTs)
            .set(TAKE_ENTITY.PLAYED, entity.played)
            .where(TAKE_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: TakeEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(TAKE_ENTITY)
            .where(TAKE_ENTITY.ID.eq(entity.id))
            .execute()
    }

    /**
     * Fetches the takes listed for soft delete (but presumably haven't been deleted yet)
     *
     * @param collectionEntity the collection entity representing the project (book) level of which the takes belong
     */
    fun fetchSoftDeletedTakes(
        collectionEntity: CollectionEntity,
        dsl: DSLContext = instanceDsl
    ): List<TakeEntity> {
        return dsl
            .select()
            .from(TAKE_ENTITY)
            .where(
                TAKE_ENTITY.CONTENT_FK.`in`(
                    select(CONTENT_ENTITY.ID)
                        .from(CONTENT_ENTITY)
                        .where(
                            CONTENT_ENTITY.COLLECTION_FK.`in`(
                                select(COLLECTION_ENTITY.ID)
                                    .from(COLLECTION_ENTITY)
                                    .where(COLLECTION_ENTITY.PARENT_FK.eq(collectionEntity.id))
                            )
                        )
                ).and(
                    TAKE_ENTITY.DELETED_TS.isNotNull
                )
            )
            .fetch {
                RecordMappers.mapToTakeEntity(it)
            }
    }

    /**
     * Fetches all takes listed for soft delete (but presumably haven't been deleted yet)
     *
     */
    fun fetchSoftDeletedTakes(
        dsl: DSLContext = instanceDsl
    ): List<TakeEntity> {
        return dsl
            .select()
            .from(TAKE_ENTITY)
            .where(TAKE_ENTITY.DELETED_TS.isNotNull)
            .fetch {
                RecordMappers.mapToTakeEntity(it)
            }
    }

    fun fetchByCollectionId(
        id: Int,
        includeDeleted: Boolean = false,
        dsl: DSLContext = instanceDsl
    ): List<TakeEntity> {
        val baseQuery = dsl
            .select()
            .from(TAKE_ENTITY)
            .where(
                TAKE_ENTITY.CONTENT_FK.`in`(
                    select(CONTENT_ENTITY.ID)
                        .from(CONTENT_ENTITY)
                        .where(CONTENT_ENTITY.COLLECTION_FK.eq(id))
                )
            )
        
        val query = when {
            includeDeleted -> baseQuery
            else -> baseQuery.and(TAKE_ENTITY.DELETED_TS.isNull)
        }
        return query.fetch(RecordMappers.Companion::mapToTakeEntity)
    }
}
