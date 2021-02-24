package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TakeEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.TakeMapper
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class TakeRepository @Inject constructor(
    private val database: AppDatabase,
    private val takeMapper: TakeMapper,
    private val markerMapper: MarkerMapper,
    private val collectionMapper: CollectionMapper
) : ITakeRepository {

    private val logger = LoggerFactory.getLogger(TakeRepository::class.java)

    private val takeDao = database.takeDao
    private val markerDao = database.markerDao
    private val contentDao = database.contentDao

    /** Delete the DB record. Instead of this, consider using {@see markDeleted} to set the deleted timestamp. */
    override fun delete(obj: Take): Completable {
        return Completable
            .fromAction {
                takeDao.delete(takeMapper.mapToEntity(obj))
            }
            .doOnError { e ->
                logger.error("Error in delete with take: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    /** Set the deleted timestamp to now. */
    override fun markDeleted(take: Take): Completable {
        return Completable
            .fromAction {
                val withDeletionFlag = take.copy(deleted = LocalDate.now())
                takeDao.update(takeMapper.mapToEntity(withDeletionFlag))
            }
            .doOnError { e ->
                logger.error("Error in markDeleted with take: $take", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Take>> {
        return Single
            .fromCallable {
                takeDao
                    .fetchAll()
                    .map(this::buildTake)
            }
            .doOnError { e ->
                logger.error("Error in getAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getByContent(content: Content, includeDeleted: Boolean): Single<List<Take>> {
        return Single
            .fromCallable {
                takeDao
                    .fetchByContentId(content.id, includeDeleted)
                    .map(this::buildTake)
            }
            .doOnError { e ->
                logger.error("Error in getByContent for content: $content, includeDeleted: $includeDeleted", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insertForContent(take: Take, content: Content): Single<Int> {
        return Single
            .fromCallable {
                val takeId = takeDao.insert(takeMapper.mapToEntity(take, content.id))
                // Insert the markers
                take.markers.forEach {
                    val entity = markerMapper.mapToEntity(it, takeId)
                    entity.id = 0
                    markerDao.insert(entity)
                }
                takeId
            }
            .doOnError { e ->
                logger.error("Error in insertForContent with take: $take and content $content", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Take): Completable {
        return Completable
            .fromAction {
                val existing = takeDao.fetchById(obj.id)
                val entity = takeMapper.mapToEntity(obj, existing.contentFk)
                takeDao.update(entity)

                // Delete and replace markers
                markerDao
                    .fetchByTakeId(obj.id)
                    .forEach {
                        markerDao.delete(it)
                    }

                obj.markers.forEach {
                    val markerEntity = markerMapper.mapToEntity(it, obj.id)
                    markerEntity.id = 0
                    markerDao.insert(markerEntity)
                }
            }
            .doOnError { e ->
                logger.error("Error in update for take: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun removeNonExistentTakes(): Completable {
        return Completable
            .fromAction {
                database.transaction { dsl ->
                    val takes = takeDao.fetchAll(dsl)
                    for (take in takes) {
                        val takeFile = File(take.filepath)
                        if (!takeFile.exists()) {
                            // Take does not exist anymore
                            // Reset the selected take if necessary to satisfy foreign key constraints
                            val content = contentDao.fetchById(take.contentFk, dsl)
                            if (content.selectedTakeFk == take.id) content.selectedTakeFk = null
                            contentDao.update(content, dsl)
                            // Remove the take from the database
                            takeDao.delete(take, dsl)
                        }
                    }
                }
            }
            .doOnError { e ->
                logger.error("Error in removeNonExistentTakes", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getSoftDeletedTakes(project: Collection): Single<List<Take>> {
        return Single
            .fromCallable {
                takeDao.fetchSoftDeletedTakes(collectionMapper.mapToEntity(project))
                    .map(this::buildTake)
            }
            .doOnError { e ->
                logger.error("Error in getSoftDeletedTakes for collection: $project", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun deleteExpiredTakes(expiry: Int): Completable {
        return Completable
            .fromAction {
                val takes = takeDao.fetchSoftDeletedTakes()
                val expired = LocalDate.now().minusDays(expiry.toLong())
                logger.info("Deleting soft deleted takes on or before: $expired")
                takes.forEach { take ->
                    take.deletedTs?.let { timestamp ->
                        val parsed = LocalDate.parse(timestamp)
                        if (parsed.isBefore(expired) || parsed.isEqual(expired)) {
                            logger.info("Deleting: ${take.filename}, soft delete date is: ${take.deletedTs}")
                            File(take.filepath).delete()
                            takeDao.delete(take)
                        }
                    }
                }
            }
            .subscribeOn(Schedulers.io())
    }

    private fun buildTake(entity: TakeEntity): Take {
        val markers = markerDao
            .fetchByTakeId(entity.id)
            .map { markerMapper.mapFromEntity(it) }
        return takeMapper.mapFromEntity(entity, markers)
    }
}
