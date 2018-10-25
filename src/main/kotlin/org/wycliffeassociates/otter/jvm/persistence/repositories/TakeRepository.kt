package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.TakeEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.TakeMapper

class TakeRepository(
        database: AppDatabase,
        private val takeMapper: TakeMapper = TakeMapper(),
        private val markerMapper: MarkerMapper = MarkerMapper()
) : ITakeRepository {

    private val takeDao = database.getTakeDao()
    private val markerDao = database.getMarkerDao()

    override fun delete(obj: Take): Completable {
        return Completable
                .fromAction {
                    takeDao.delete(takeMapper.mapToEntity(obj))
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
                .subscribeOn(Schedulers.io())
    }

    override fun getByChunk(chunk: Chunk): Single<List<Take>> {
        return Single
                .fromCallable {
                    takeDao
                            .fetchByChunkId(chunk.id)
                            .map(this::buildTake)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun insertForChunk(obj: Take, chunk: Chunk): Single<Int> {
        return Single
                .fromCallable {
                    val takeId = takeDao.insert(takeMapper.mapToEntity(obj).apply { contentFk = chunk.id })
                    // Insert the markers
                    obj.markers.forEach {
                        val entity = markerMapper.mapToEntity(it)
                        entity.id = 0
                        entity.takeFk = takeId
                        markerDao.insert(entity)
                    }
                    takeId
                }
                .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Take): Completable {
        return Completable
                .fromAction {
                    val existing = takeDao.fetchById(obj.id)
                    val entity = takeMapper.mapToEntity(obj)
                    entity.contentFk = existing.contentFk
                    takeDao.update(entity)

                    // Delete and replace markers
                    markerDao
                            .fetchByTakeId(obj.id)
                            .forEach {
                                markerDao.delete(it)
                            }

                    obj.markers.forEach {
                        val markerEntity = markerMapper.mapToEntity(it)
                        markerEntity.id = 0
                        markerEntity.takeFk = obj.id
                        markerDao.insert(markerEntity)
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