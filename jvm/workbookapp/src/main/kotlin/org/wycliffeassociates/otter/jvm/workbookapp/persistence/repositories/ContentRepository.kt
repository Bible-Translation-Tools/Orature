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

import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.TakeMapper
import java.lang.IllegalStateException
import javax.inject.Inject
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.CollectionMapper

class ContentRepository @Inject constructor(
    database: AppDatabase
) : IContentRepository {
    private val logger = LoggerFactory.getLogger(ContentRepository::class.java)

    private val activeConnections = mutableMapOf<Collection, ReplayRelay<Content>>()

    private val contentDao = database.contentDao
    private val takeDao = database.takeDao
    private val markerDao = database.markerDao
    private val contentTypeDao = database.contentTypeDao
    private val contentMapper: ContentMapper = ContentMapper(contentTypeDao)
    private val collectionMapper = CollectionMapper()
    private val takeMapper: TakeMapper = TakeMapper()
    private val markerMapper: MarkerMapper = MarkerMapper()

    override fun getByCollection(collection: Collection): Single<List<Content>> {
        return Single
            .fromCallable {
                contentDao
                    .fetchByCollectionId(collection.id)
                    .map(this::buildContent)
                    .filter { !it.bridged }
            }
            .doOnError { e ->
                logger.error("Error in getByCollection for collection: $collection", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getByCollectionWithPersistentConnection(collection: Collection): Observable<Content> {
        activeConnections.getOrDefault(collection, null)?.let { return it }

        val connection = ReplayRelay.create<Content>()
        activeConnections[collection] = connection
        getByCollection(collection)
            .map {
                it.forEach { connection.accept(it) }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()

        return connection
    }

    override fun getCollectionMetaContent(collection: Collection): Single<Content> {
        return Single
            .fromCallable {
                contentDao
                    .fetchByCollectionIdAndType(collection.id, ContentType.META)
                    .map(this::buildContent)
                    .minByOrNull { it.start }
                    ?: throw IllegalStateException("Missing meta info for chapter.")
            }
            .doOnError { e ->
                logger.error("Error in getByCollectionMetaContent for collection: $collection", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getSources(content: Content): Single<List<Content>> {
        return Single
            .fromCallable {
                contentDao
                    .fetchSources(contentMapper.mapToEntity(content))
                    .map(this::buildContent)
            }
            .doOnError { e ->
                logger.error("Error in getSources for content: $content", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun updateSources(content: Content, sourceContents: List<Content>): Completable {
        return Completable
            .fromAction {
                contentDao.updateSources(
                    contentMapper.mapToEntity(content),
                    sourceContents.map { contentMapper.mapToEntity(it) }
                )
            }
            .doOnError { e ->
                logger.error("Error in updateSources for content: $content")
                logger.error("Source Content, Begin:")
                sourceContents.forEach {
                    logger.error("$it")
                }
                logger.error("End source content", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun deleteForCollection(
        chapterCollection: Collection,
        typeFilter: ContentType?
    ): Completable {
        val typeId = typeFilter?.let {
            contentTypeDao.fetchId(typeFilter)
        }

        activeConnections.getOrDefault(chapterCollection, null)
            ?.let { it.getValues(emptyArray()).forEach { it.draftNumber = -1 } }

        return Completable.fromCallable {
            contentDao.deleteForCollection(
                collectionMapper.mapToEntity(chapterCollection),
                typeId
            )
        }
    }

    override fun delete(obj: Content): Completable {
        return Completable
            .fromAction {
                contentDao.delete(contentMapper.mapToEntity(obj))
            }
            .doOnError { e ->
                logger.error("Error in delete for content: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Content>> {
        return Single
            .fromCallable {
                contentDao
                    .fetchAll()
                    .map(this::buildContent)
                    .filter {
                        if (it.bridged) {
                            logger.info("Ignoring bridged content: ${it}")
                        }
                        !it.bridged
                    }
            }
            .doOnError { e ->
                logger.error("Error in getAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insertForCollection(content: Content, collection: Collection): Single<Int> {
        return Single
            .fromCallable {
                val id = contentDao.insert(
                    contentMapper
                        .mapToEntity(content, collection.id)
                        .apply { collectionFk = collection.id }
                )
                content.id = id
                activeConnections.getOrDefault(collection, null)?.let { it.accept(content) }
                id
            }
            .doOnError { e ->
                logger.error("Error in insertForCollection for content: $content, collection: $collection", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun updateAll(content: List<Content>): Completable {
        return Completable
            .fromAction {
                val entities = content.map { obj ->
                    val entity = contentMapper.mapToEntity(obj)
                    entity
                }
                contentDao.updateAll(entities)
            }.subscribeOn(Schedulers.io())
    }

    override fun update(obj: Content): Completable {
        return Completable
            .fromAction {
                val existing = contentDao.fetchById(obj.id)
                val entity = contentMapper.mapToEntity(obj)
                // Make sure we don't over write the collection relationship
                entity.collectionFk = existing.collectionFk
                contentDao.update(entity)

                updateConnection(obj, entity.collectionFk)
            }
            .doOnError { e ->
                logger.error("Error in update for content: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    /**
     * Updates the content stored inside the active connections.
     * Calls this method when making a change to the content in the database
     * to avoid out-of-sync between the database and connections.
     */
    private fun updateConnection(
        newContent: Content,
        collectionId: Int
    ) {
        activeConnections.keys.find { it.id == collectionId }?.let { collection ->
            activeConnections[collection]?.let { connection ->
                connection.getValues(emptyArray()).find {
                    it.id == newContent.id
                }?.let { contentInRelay ->
                    contentInRelay.apply {
                        sort = newContent.sort
                        labelKey = newContent.labelKey
                        start = newContent.start
                        end = newContent.end
                        selectedTake = newContent.selectedTake
                        text = newContent.text
                        format = newContent.format
                        type = newContent.type
                        draftNumber = newContent.draftNumber
                        bridged = newContent.bridged
                    }
                }
            }
        }
    }

    override fun linkDerivedToSource(
        derivedContents: List<Content>,
        sourceContents: List<Content>
    ): Completable {
        if (sourceContents.isEmpty()) {
            return Completable.complete()
        }

        return Completable.fromAction {
            derivedContents.forEach { content ->
                for (verse in content.start..content.end) {
                    val sourceId = sourceContents.firstOrNull { it.sort == verse }?.id
                    if (sourceId != null) {
                        contentDao.linkDerivative(content.id, sourceId)
                    }
                }
            }
        }
    }

    private fun buildContent(entity: ContentEntity): Content {
        // Check for sources
        val sources = contentDao.fetchSources(entity)
        val selectedTake = entity
            .selectedTakeFk?.let { selectedTakeFk ->
                // Retrieve the markers
                val markers = markerDao
                    .fetchByTakeId(selectedTakeFk)
                    .map(markerMapper::mapFromEntity)
                takeMapper.mapFromEntity(takeDao.fetchById(selectedTakeFk), markers)
            }
        return contentMapper.mapFromEntity(entity, selectedTake)
    }
}
