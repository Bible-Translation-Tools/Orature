package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
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

class ContentRepository @Inject constructor(
    database: AppDatabase
) : IContentRepository {
    private val logger = LoggerFactory.getLogger(ContentRepository::class.java)

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
            .doOnError { e ->
                logger.error("Error in getByCollection for collection: $collection", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getCollectionMetaContent(collection: Collection): Single<Content> {
        return Single
            .fromCallable {
                contentDao
                    .fetchByCollectionIdAndType(collection.id, ContentType.META)
                    .map(this::buildContent)
                    .minBy { it.start }
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
            }
            .doOnError { e ->
                logger.error("Error in getAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insertForCollection(content: Content, collection: Collection): Single<Int> {
        return Single
            .fromCallable {
                contentDao.insert(contentMapper.mapToEntity(content).apply { collectionFk = collection.id })
            }
            .doOnError { e ->
                logger.error("Error in insertForCollection for content: $content, collection: $collection", e)
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
            .doOnError { e ->
                logger.error("Error in update for content: $obj", e)
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
