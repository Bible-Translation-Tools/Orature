/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import jooq.tables.ContentDerivative
import org.jooq.*
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ContentEntity


class ContentDao(
    private val instanceDsl: DSLContext,
    private val contentTypeDao: ContentTypeDao
) {
    fun fetchByCollectionId(collectionId: Int, dsl: DSLContext = instanceDsl): List<ContentEntity> {
        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
            .orderBy(CONTENT_ENTITY.SORT)
            .fetch { RecordMappers.mapToContentEntity(it) }
    }

    fun fetchByCollectionIdAndStart(
        collectionId: Int,
        start: Int,
        types: Collection<ContentType>,
        dsl: DSLContext = instanceDsl
    ): List<ContentEntity> {
        val typeIds = types.map(contentTypeDao::fetchId)
        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
            .and(CONTENT_ENTITY.START.eq(start))
            .and(CONTENT_ENTITY.TYPE_FK.`in`(typeIds))
            .orderBy(CONTENT_ENTITY.SORT)
            .fetch { RecordMappers.mapToContentEntity(it) }
    }

    fun fetchByCollectionIdAndType(
        collectionId: Int,
        type: ContentType,
        dsl: DSLContext = instanceDsl
    ): List<ContentEntity> {
        val typeId = contentTypeDao.fetchId(type)
        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
            .and(CONTENT_ENTITY.TYPE_FK.eq(typeId))
            .orderBy(CONTENT_ENTITY.SORT)
            .fetch { RecordMappers.mapToContentEntity(it) }
    }

    fun selectVerseByCollectionIdAndStart(
        collectionId: Int,
        start: Int,
        vararg extraFields: SelectFieldOrAsterisk,
        dsl: DSLContext = instanceDsl
    ): Select<Record> {
        val textTypeId = contentTypeDao.fetchId(ContentType.TEXT)
        return dsl
            .select(CONTENT_ENTITY.ID, *extraFields)
            .from(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
            .and(CONTENT_ENTITY.START.eq(start))
            .and(CONTENT_ENTITY.TYPE_FK.eq(textTypeId))
            .limit(1)
    }

    /**
     *  Build a JOOQ select statement that locates linkable verse/resource pairs within the given collection.
     *  Resources that are already linked are skipped.
     *  For convenience, [extraFields] will be appended in columns to the right of each row.
     */
    fun selectLinkableVerses(
        mainTypes: Collection<ContentType>,
        helpTypes: Collection<ContentType>,
        parentCollectionId: Int,
        vararg extraFields: SelectFieldOrAsterisk,
        dsl: DSLContext = instanceDsl
    ): Select<Record> {
        val mainTypeIds = mainTypes.map(contentTypeDao::fetchId)
        val helpTypeIds = helpTypes.map(contentTypeDao::fetchId)

        val main = CONTENT_ENTITY.`as`("main")
        val help = CONTENT_ENTITY.`as`("help")
        val existingLink = RESOURCE_LINK.`as`("existingLink")
        val existingLinkSubquery = dsl.select(existingLink.RESOURCE_CONTENT_FK).from(existingLink)

        return dsl
            .select(main.ID, help.ID, *extraFields)
            .from(main)
            .join(help)
            .using(CONTENT_ENTITY.COLLECTION_FK, CONTENT_ENTITY.START)
            .where(main.COLLECTION_FK.eq(parentCollectionId))
            .and(main.TYPE_FK.`in`(mainTypeIds))
            .and(help.TYPE_FK.`in`(helpTypeIds))
            // Skip already-linked resources.
            .and(help.ID.notIn(existingLinkSubquery))
    }

    /**
     *  Build a JOOQ select statement that locates linkable chapter/resource pairs within the given collection.
     *  Resources that are already linked are skipped.
     *  For convenience, [extraFields] will be appended in columns to the right of each row.
     */
    fun selectLinkableChapters(
        helpTypes: Collection<ContentType>,
        collectionId: Int,
        vararg extraFields: SelectFieldOrAsterisk,
        dsl: DSLContext = instanceDsl
    ): Select<Record> {
        val helpTypeIds = helpTypes.map(contentTypeDao::fetchId)

        val main = COLLECTION_ENTITY.`as`("main")
        val help = CONTENT_ENTITY.`as`("help")
        val existingLink = RESOURCE_LINK.`as`("existingLink")
        val existingLinkSubquery = dsl.select(existingLink.RESOURCE_CONTENT_FK).from(existingLink)

        return dsl
            .select(main.ID, help.ID, *extraFields)
            .from(main)
            .join(help)
            .onKey()
            .where(main.ID.eq(collectionId))
            .and(help.TYPE_FK.`in`(helpTypeIds))
            .and(help.START.eq(0))
            // Skip already-linked resources.
            .and(help.ID.notIn(existingLinkSubquery))
    }

    fun fetchSources(entity: ContentEntity, dsl: DSLContext = instanceDsl): List<ContentEntity> {
        val sourceIds = dsl
            .select(CONTENT_DERIVATIVE.SOURCE_FK)
            .from(CONTENT_DERIVATIVE)
            .where(CONTENT_DERIVATIVE.CONTENT_FK.eq(entity.id))
            .fetch {
                it.getValue(CONTENT_DERIVATIVE.SOURCE_FK)
            }

        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.ID.`in`(sourceIds))
            .orderBy(CONTENT_ENTITY.SORT)
            .fetch { RecordMappers.mapToContentEntity(it) }
    }

    fun fetchContentByProjectSlug(
        projectSlug: String,
        dsl: DSLContext = instanceDsl
    ): SelectConditionStep<Record> {
        return dsl.select(CONTENT_ENTITY.asterisk())
            .from(CONTENT_ENTITY)
            .where(
                CONTENT_ENTITY.COLLECTION_FK.`in`(
                    // Look up the chapter collection the resource content belongs to
                    dsl.select(COLLECTION_ENTITY.ID)
                        .from(COLLECTION_ENTITY)
                        .where(
                            COLLECTION_ENTITY.PARENT_FK.`in`(
                                // Look up the project the chapter collection belongs to
                                dsl.select(COLLECTION_ENTITY.ID)
                                    .from(COLLECTION_ENTITY)
                                    .where(
                                        // We need the source, not the derived,
                                        // just use the slug. It will result in derived results
                                        // in addition to source, but resources aren't attached
                                        // to the derived anyway
                                        COLLECTION_ENTITY.SLUG.eq(projectSlug)
                                    )
                            )
                        )
                )
            )
    }

    fun updateSources(entity: ContentEntity, sources: List<ContentEntity>, dsl: DSLContext = instanceDsl) {
        // Delete the existing sources
        dsl
            .deleteFrom(CONTENT_DERIVATIVE)
            .where(CONTENT_DERIVATIVE.CONTENT_FK.eq(entity.id))
            .execute()

        // Add the sources
        sources.forEach {
            dsl
                .insertInto(CONTENT_DERIVATIVE, CONTENT_DERIVATIVE.CONTENT_FK, CONTENT_DERIVATIVE.SOURCE_FK)
                .values(entity.id, it.id)
                .execute()
        }
    }

    @Synchronized
    fun insert(entity: ContentEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID was not 0")

        // Insert the new content entity
        dsl
            .insertInto(
                CONTENT_ENTITY,
                CONTENT_ENTITY.COLLECTION_FK,
                CONTENT_ENTITY.SORT,
                CONTENT_ENTITY.START,
                CONTENT_ENTITY.V_END,
                CONTENT_ENTITY.LABEL,
                CONTENT_ENTITY.SELECTED_TAKE_FK,
                CONTENT_ENTITY.TEXT,
                CONTENT_ENTITY.FORMAT,
                CONTENT_ENTITY.TYPE_FK,
                CONTENT_ENTITY.DRAFT_NUMBER,
                CONTENT_ENTITY.BRIDGED
            )
            .values(
                entity.collectionFk,
                entity.sort,
                entity.start,
                entity.end,
                entity.labelKey,
                entity.selectedTakeFk,
                entity.text,
                entity.format,
                entity.type_fk,
                entity.draftNumber,
                if (entity.bridged) 1 else 0
            )
            .execute()

        // Get the ID
        return dsl
            .select(max(CONTENT_ENTITY.ID))
            .from(CONTENT_ENTITY)
            .fetchOne {
                it.getValue(max(CONTENT_ENTITY.ID))
            }!!
    }

    @Synchronized
    fun insertNoReturn(vararg entities: ContentEntity, dsl: DSLContext = instanceDsl) {
        val bareInsert = dsl
            .insertInto(
                CONTENT_ENTITY,
                CONTENT_ENTITY.COLLECTION_FK,
                CONTENT_ENTITY.SORT,
                CONTENT_ENTITY.START,
                CONTENT_ENTITY.V_END,
                CONTENT_ENTITY.LABEL,
                CONTENT_ENTITY.SELECTED_TAKE_FK,
                CONTENT_ENTITY.TEXT,
                CONTENT_ENTITY.FORMAT,
                CONTENT_ENTITY.TYPE_FK,
                CONTENT_ENTITY.DRAFT_NUMBER,
                CONTENT_ENTITY.BRIDGED
            )
        val insertWithValues = entities.fold(bareInsert) { q, e ->
            if (e.id != 0) throw InsertionException("Entity ID was not 0")
            q.values(
                e.collectionFk,
                e.sort,
                e.start,
                e.end,
                e.labelKey,
                e.selectedTakeFk,
                e.text,
                e.format,
                e.type_fk,
                e.draftNumber,
                if (e.bridged) 1 else 0
            )
        }
        insertWithValues.execute()
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): ContentEntity {
        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToContentEntity(it)
            }!!
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<ContentEntity> {
        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .fetch {
                RecordMappers.mapToContentEntity(it)
            }
    }

    /**
     * Updates all content in the list.
     * Updates will not update the ID or collection foreign key.
     */
    fun updateAll(entities: List<ContentEntity>, dsl: DSLContext = instanceDsl) {
        dsl.transaction { config ->
            entities.forEach { entity ->
                config.dsl().update(CONTENT_ENTITY)
                    .set(CONTENT_ENTITY.SORT, entity.sort)
                    .set(CONTENT_ENTITY.LABEL, entity.labelKey)
                    .set(CONTENT_ENTITY.START, entity.start)
                    .set(CONTENT_ENTITY.V_END, entity.end)
                    .set(CONTENT_ENTITY.SELECTED_TAKE_FK, entity.selectedTakeFk)
                    .set(CONTENT_ENTITY.TEXT, entity.text)
                    .set(CONTENT_ENTITY.FORMAT, entity.format)
                    .set(CONTENT_ENTITY.TYPE_FK, entity.type_fk)
                    .set(CONTENT_ENTITY.DRAFT_NUMBER, entity.draftNumber)
                    .set(CONTENT_ENTITY.BRIDGED, if (entity.bridged) 1 else 0)
                    .where(CONTENT_ENTITY.ID.eq(entity.id))
                    .execute()
            }
        }
    }

    fun update(entity: ContentEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(CONTENT_ENTITY)
            .set(CONTENT_ENTITY.SORT, entity.sort)
            .set(CONTENT_ENTITY.LABEL, entity.labelKey)
            .set(CONTENT_ENTITY.START, entity.start)
            .set(CONTENT_ENTITY.V_END, entity.end)
            .set(CONTENT_ENTITY.COLLECTION_FK, entity.collectionFk)
            .set(CONTENT_ENTITY.SELECTED_TAKE_FK, entity.selectedTakeFk)
            .set(CONTENT_ENTITY.TEXT, entity.text)
            .set(CONTENT_ENTITY.FORMAT, entity.format)
            .set(CONTENT_ENTITY.DRAFT_NUMBER, entity.draftNumber)
            .set(CONTENT_ENTITY.BRIDGED, if (entity.bridged) 1 else 0)
            .where(CONTENT_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: ContentEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun deleteForCollection(
        chapterCollection: CollectionEntity,
        contentTypeId: Int? = null,
        dsl: DSLContext = instanceDsl
    ) {
        dsl.deleteFrom(CONTENT_ENTITY)
            .where(
                CONTENT_ENTITY.COLLECTION_FK.eq(chapterCollection.id)
                    .and((CONTENT_ENTITY.TYPE_FK).eq(contentTypeId ?: 1))
            )
            .execute()
    }

    fun linkDerivative(
        contentId: Int,
        sourceContentId: Int,
        dsl: DSLContext = instanceDsl
    ) {
        dsl
            .insertInto(
                ContentDerivative.CONTENT_DERIVATIVE,
                ContentDerivative.CONTENT_DERIVATIVE.CONTENT_FK,
                ContentDerivative.CONTENT_DERIVATIVE.SOURCE_FK
            )
            .values(contentId, sourceContentId)
            .execute()
    }
}
