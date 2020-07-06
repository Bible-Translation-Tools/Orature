package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Select
import org.jooq.SelectFieldOrAsterisk
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
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
                CONTENT_ENTITY.LABEL,
                CONTENT_ENTITY.SELECTED_TAKE_FK,
                CONTENT_ENTITY.TEXT,
                CONTENT_ENTITY.FORMAT
            )
            .values(
                entity.collectionFk,
                entity.sort,
                entity.start,
                entity.labelKey,
                entity.selectedTakeFk,
                entity.text,
                entity.format
            )
            .execute()

        // Get the ID
        return dsl
            .select(max(CONTENT_ENTITY.ID))
            .from(CONTENT_ENTITY)
            .fetchOne {
                it.getValue(max(CONTENT_ENTITY.ID))
            }
    }

    @Synchronized
    fun insertNoReturn(vararg entities: ContentEntity, dsl: DSLContext = instanceDsl) {
        val bareInsert = dsl
            .insertInto(
                CONTENT_ENTITY,
                CONTENT_ENTITY.COLLECTION_FK,
                CONTENT_ENTITY.SORT,
                CONTENT_ENTITY.START,
                CONTENT_ENTITY.LABEL,
                CONTENT_ENTITY.SELECTED_TAKE_FK,
                CONTENT_ENTITY.TEXT,
                CONTENT_ENTITY.FORMAT,
                CONTENT_ENTITY.TYPE_FK
            )
        val insertWithValues = entities.fold(bareInsert) { q, e ->
            if (e.id != 0) throw InsertionException("Entity ID was not 0")
            q.values(
                e.collectionFk,
                e.sort,
                e.start,
                e.labelKey,
                e.selectedTakeFk,
                e.text,
                e.format,
                e.type_fk
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
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<ContentEntity> {
        return dsl
            .select()
            .from(CONTENT_ENTITY)
            .fetch {
                RecordMappers.mapToContentEntity(it)
            }
    }

    fun update(entity: ContentEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(CONTENT_ENTITY)
            .set(CONTENT_ENTITY.SORT, entity.sort)
            .set(CONTENT_ENTITY.LABEL, entity.labelKey)
            .set(CONTENT_ENTITY.START, entity.start)
            .set(CONTENT_ENTITY.COLLECTION_FK, entity.collectionFk)
            .set(CONTENT_ENTITY.SELECTED_TAKE_FK, entity.selectedTakeFk)
            .set(CONTENT_ENTITY.TEXT, entity.text)
            .set(CONTENT_ENTITY.FORMAT, entity.format)
            .where(CONTENT_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: ContentEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(CONTENT_ENTITY)
            .where(CONTENT_ENTITY.ID.eq(entity.id))
            .execute()
    }
}
