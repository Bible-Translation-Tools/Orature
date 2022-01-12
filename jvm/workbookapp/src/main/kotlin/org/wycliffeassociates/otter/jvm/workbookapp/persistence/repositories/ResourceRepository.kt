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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import jooq.Tables.*
import org.jooq.SelectConditionStep
import org.jooq.Record
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.collections.MultiMap
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.ContentEntityTable
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.RecordMappers
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceLinkEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.*
import javax.inject.Inject

class ResourceRepository @Inject constructor(private val database: AppDatabase) : IResourceRepository {
    private val logger = LoggerFactory.getLogger(ResourceRepository::class.java)

    private val contentDao = database.contentDao
    private val contentTypeDao = database.contentTypeDao
    private val collectionDao = database.collectionDao
    private val takeDao = database.takeDao
    private val markerDao = database.markerDao
    private val resourceLinkDao = database.resourceLinkDao
    private val subtreeHasResourceDao = database.subtreeHasResourceDao
    private val languageDao = database.languageDao
    private val contentMapper: ContentMapper = ContentMapper(contentTypeDao)
    private val takeMapper: TakeMapper = TakeMapper()
    private val markerMapper: MarkerMapper = MarkerMapper()
    private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper()
    private val languageMapper: LanguageMapper = LanguageMapper()

    override fun delete(obj: Content): Completable {
        return Completable
            .fromAction {
                contentDao.delete(contentMapper.mapToEntity(obj))
            }
            .doOnError { e ->
                logger.error("Error in delete with content: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Content>> {
        return Single
            .fromCallable {
                contentDao
                    .fetchAll()
                    .map(this::buildResource)
            }
            .doOnError { e ->
                logger.error("Error in getAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getResourceMetadata(content: Content): List<ResourceMetadata> {
        return database.dsl
            .selectDistinct(DUBLIN_CORE_ENTITY.asterisk())
            .from(RESOURCE_LINK)
            .join(DUBLIN_CORE_ENTITY).on(DUBLIN_CORE_ENTITY.ID.eq(RESOURCE_LINK.DUBLIN_CORE_FK))
            .where(RESOURCE_LINK.CONTENT_FK.eq(content.id))
            .fetch(RecordMappers.Companion::mapToResourceMetadataEntity)
            .map(this::mapToResourceMetadata)
    }

    override fun getResourceMetadata(collection: Collection): List<ResourceMetadata> {
        return database.dsl
            .selectDistinct(DUBLIN_CORE_ENTITY.asterisk())
            .from(RESOURCE_LINK)
            .join(DUBLIN_CORE_ENTITY).on(DUBLIN_CORE_ENTITY.ID.eq(RESOURCE_LINK.DUBLIN_CORE_FK))
            .where(RESOURCE_LINK.COLLECTION_FK.eq(collection.id))
            .fetch(RecordMappers.Companion::mapToResourceMetadataEntity)
            .map(this::mapToResourceMetadata)
    }

    override fun getSubtreeResourceMetadata(collection: Collection): List<ResourceMetadata> {
        return database.dsl
            .select(DUBLIN_CORE_ENTITY.asterisk())
            .from(SUBTREE_HAS_RESOURCE)
            .join(DUBLIN_CORE_ENTITY).on(DUBLIN_CORE_ENTITY.ID.eq(SUBTREE_HAS_RESOURCE.DUBLIN_CORE_FK))
            .where(SUBTREE_HAS_RESOURCE.COLLECTION_FK.eq(collection.id))
            .fetch(RecordMappers.Companion::mapToResourceMetadataEntity)
            .map(this::mapToResourceMetadata)
    }

    override fun getResources(content: Content, resourceMetadata: ResourceMetadata): Observable<Content> {
        val main = CONTENT_ENTITY.`as`("main")
        val help = CONTENT_ENTITY.`as`("help")

        val selectStatement = database.dsl
            .selectDistinct(help.asterisk())
            .from(RESOURCE_LINK)
            .join(main).on(main.ID.eq(RESOURCE_LINK.CONTENT_FK))
            .join(help).on(help.ID.eq(RESOURCE_LINK.RESOURCE_CONTENT_FK))
            .where(RESOURCE_LINK.DUBLIN_CORE_FK.eq(resourceMetadata.id))
            .and(main.ID.eq(content.id))

        return getResources(help, selectStatement)
    }

    /**
     * Returns collection-specific resources (does not return resources about the collection's children.)
     */
    override fun getResources(collection: Collection, resourceMetadata: ResourceMetadata): Observable<Content> {
        val help = CONTENT_ENTITY.`as`("help")

        val selectStatement = database.dsl
            .selectDistinct(help.asterisk())
            .from(RESOURCE_LINK)
            .join(COLLECTION_ENTITY).on(COLLECTION_ENTITY.ID.eq(RESOURCE_LINK.COLLECTION_FK))
            .join(help).on(RESOURCE_LINK.RESOURCE_CONTENT_FK.eq(help.ID))
            .where(RESOURCE_LINK.DUBLIN_CORE_FK.eq(resourceMetadata.id))
            .and(COLLECTION_ENTITY.ID.eq(collection.id))

        return getResources(help, selectStatement)
    }

    private fun getResources(
        help: ContentEntityTable,
        selectStatement: SelectConditionStep<Record>
    ): Observable<Content> {
        val contentStreamObservable = Observable.fromCallable {
            selectStatement
                .orderBy(help.START, help.SORT)
                .fetchStream()
                .map { RecordMappers.mapToContentEntity(it, help) }
                .map(this::buildResource)
        }

        return contentStreamObservable
            .flatMap { it.iterator().toObservable() }
            .doOnError { e ->
                logger.error("Error in getResources for resource: $help, with select: $selectStatement", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun insert(entity: ResourceLinkEntity): Completable {
        return Completable
            .fromAction {
                resourceLinkDao.insertNoReturn(entity)
            }
            .doOnError { e ->
                logger.error("Error in insert for link: $entity", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun linkToContent(resource: Content, content: Content, dublinCoreFk: Int) = insert(
        ResourceLinkEntity(
            id = 0,
            resourceContentFk = resource.id,
            contentFk = content.id,
            collectionFk = null,
            dublinCoreFk = dublinCoreFk
        )
    )

    override fun linkToCollection(resource: Content, collection: Collection, dublinCoreFk: Int) = insert(
        ResourceLinkEntity(
            id = 0,
            resourceContentFk = resource.id,
            contentFk = null,
            collectionFk = collection.id,
            dublinCoreFk = dublinCoreFk
        )
    )

    override fun update(obj: Content): Completable {
        return Completable
            .fromAction {
                val existing = contentDao.fetchById(obj.id)
                val entity = contentMapper.mapToEntity(obj)
                // Make sure we don't over write the collection relationship
                entity.collectionFk = existing.collectionFk
                contentDao.update(entity)
            }
            .doOnError { e ->
                logger.error("Error in update for content: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun calculateAndSetSubtreeHasResources(collectionId: Int) {
        database.transaction { dsl ->
            val collectionEntity = collectionDao.fetchById(collectionId, dsl)
            val accumulator = MultiMap<Int, Int>()
            calculateAndSetSubtreeHasResources(collectionEntity, accumulator, dsl)
            subtreeHasResourceDao.insert(accumulator.kvSequence(), dsl)
        }
    }

    private fun calculateAndSetSubtreeHasResources(
        collection: CollectionEntity,
        mMapCollectionToDublinId: MultiMap<Int, Int>,
        dsl: DSLContext
    ): Set<Int> {
        val childResources = collectionDao
            .fetchChildren(collection, dsl)
            .flatMap { calculateAndSetSubtreeHasResources(it, mMapCollectionToDublinId, dsl) }
        val myCollectionResources = resourceLinkDao
            .fetchByCollectionId(collection.id, dsl)
            .map { it.dublinCoreFk }
        val myContentResources = getContentResourceFksByCollection(collection.id, dsl)
        val union = childResources
            .union(myCollectionResources)
            .union(myContentResources)

        union.forEach {
            mMapCollectionToDublinId[collection.id] = it
        }

        return union
    }

    private fun getContentResourceFksByCollection(collectionId: Int, dsl: DSLContext): List<Int> {
        return dsl
            .selectDistinct(RESOURCE_LINK.DUBLIN_CORE_FK)
            .from(RESOURCE_LINK)
            .join(CONTENT_ENTITY)
            .on(RESOURCE_LINK.CONTENT_FK.eq(CONTENT_ENTITY.ID))
            .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
            .fetch(RESOURCE_LINK.DUBLIN_CORE_FK)
    }

    private fun buildResource(entity: ContentEntity): Content {
        // Check for sources
        val sources = contentDao.fetchSources(entity)
        val contentEnd = sources.map { it.start }.maxOrNull() ?: entity.start
        val selectedTake = entity
            .selectedTakeFk?.let { selectedTakeFk ->
                // Retrieve the markers
                val markers = markerDao
                    .fetchByTakeId(selectedTakeFk)
                    .map(markerMapper::mapFromEntity)
                takeMapper.mapFromEntity(takeDao.fetchById(selectedTakeFk), markers)
            }
        return contentMapper.mapFromEntity(entity, selectedTake, contentEnd)
    }

    private fun mapToResourceMetadata(entity: ResourceMetadataEntity): ResourceMetadata {
        val language = languageMapper
            .mapFromEntity(languageDao.fetchById(entity.languageFk))
        return metadataMapper.mapFromEntity(entity, language)
    }
}
