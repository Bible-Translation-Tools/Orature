package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.TakeMapper

class ContentRepository(
    database: AppDatabase
) : IContentRepository {
    private val contentDao = database.contentDao
    private val takeDao = database.takeDao
    private val markerDao = database.markerDao
    private val contentTypeDao = database.contentTypeDao
    private val contentMapper: ContentMapper = ContentMapper(contentTypeDao)
    private val takeMapper: TakeMapper = TakeMapper()
    private val markerMapper: MarkerMapper = MarkerMapper()

    override fun getByCollection(collection: Collection): Single<List<Content>> {
        return Single
            .fromCallable {
                contentDao
                    .fetchByCollectionId(collection.id)
                    .map(this::buildContent)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getCollectionMetaContent(collection: Collection): Single<Content> {
        return Single
            .fromCallable {
                contentDao
                    .fetchByCollectionIdAndStart(collection.id, 1, listOf(ContentType.META))
                    .map(this::buildContent)
                    .single()
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
            .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: Content): Completable {
        return Completable
            .fromAction {
                contentDao.delete(contentMapper.mapToEntity(obj))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Content>> {
        return Single
            .fromCallable {
                contentDao
                    .fetchAll()
                    .map(this::buildContent)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insertForCollection(content: Content, collection: Collection): Single<Int> {
        return Single
            .fromCallable {
                contentDao.insert(contentMapper.mapToEntity(content).apply { collectionFk = collection.id })
            }
            .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Content): Completable {
        return Completable
            .fromAction {
                val existing = contentDao.fetchById(obj.id)
                val entity = contentMapper.mapToEntity(obj)
                // Make sure we don't over write the collection relationship
                entity.collectionFk = existing.collectionFk
                contentDao.update(entity)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun buildContent(entity: ContentEntity): Content {
        // Check for sources
        val sources = contentDao.fetchSources(entity)
        val contentEnd = sources.map { it.start }.max() ?: entity.start
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

}