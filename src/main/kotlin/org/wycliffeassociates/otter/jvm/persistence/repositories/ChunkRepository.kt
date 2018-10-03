package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.persistence.repositories.IChunkRepository
import org.wycliffeassociates.otter.jvm.persistence.database.IAppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ChunkEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ChunkMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.TakeMapper

class ChunkRepository(
        database: IAppDatabase,
        private val chunkMapper: ChunkMapper = ChunkMapper(),
        private val takeMapper: TakeMapper = TakeMapper(),
        private val markerMapper: MarkerMapper = MarkerMapper()
) : IChunkRepository {
    private val chunkDao = database.getChunkDao()
    private val takeDao = database.getTakeDao()
    private val markerDao = database.getMarkerDao()

    override fun getByCollection(collection: Collection): Single<List<Chunk>> {
        return Single
                .fromCallable {
                    chunkDao
                            .fetchByCollectionId(collection.id)
                            .map(this::buildChunk)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getSources(chunk: Chunk): Single<List<Chunk>> {
        return Single
                .fromCallable {
                    chunkDao
                            .fetchSources(chunkMapper.mapToEntity(chunk))
                            .map(this::buildChunk)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateSources(chunk: Chunk, sourceChunks: List<Chunk>): Completable {
        return Completable
                .fromAction {
                    chunkDao.updateSources(chunkMapper.mapToEntity(chunk), sourceChunks.map(chunkMapper::mapToEntity))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: Chunk): Completable {
        return Completable
                .fromAction {
                    chunkDao.delete(chunkMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Chunk>> {
        return Single
                .fromCallable {
                    chunkDao
                            .fetchAll()
                            .map(this::buildChunk)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun insertForCollection(chunk: Chunk, collection: Collection): Single<Int> {
        return Single
                .fromCallable {
                    chunkDao.insert(chunkMapper.mapToEntity(chunk).apply { collectionFk = collection.id })
                }
                .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Chunk): Completable {
        return Completable
                .fromAction {
                    val existing = chunkDao.fetchById(obj.id)
                    val entity = chunkMapper.mapToEntity(obj)
                    // Make sure we don't over write the collection relationship
                    entity.collectionFk = existing.collectionFk
                    chunkDao.update(entity)
                }
                .subscribeOn(Schedulers.io())
    }

    private fun buildChunk(entity: ChunkEntity): Chunk {
        // Check for sources
        val sources = chunkDao.fetchSources(entity)
        val chunkEnd = sources.map { it.start }.max() ?: entity.start
        val selectedTake = entity
                .selectedTakeFk?.let { selectedTakeFk ->
            // Retrieve the markers
            val markers = markerDao
                    .fetchByTakeId(selectedTakeFk)
                    .map(markerMapper::mapFromEntity)
            takeMapper.mapFromEntity(takeDao.fetchById(selectedTakeFk), markers)
        }
        return chunkMapper.mapFromEntity(entity, selectedTake, chunkEnd)
    }

}