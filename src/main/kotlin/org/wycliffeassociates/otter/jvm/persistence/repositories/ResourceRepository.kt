package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ChunkEntity
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ChunkMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.TakeMapper

class ResourceRepository(
        database: AppDatabase,
        private val chunkMapper: ChunkMapper = ChunkMapper(),
        private val takeMapper: TakeMapper = TakeMapper(),
        private val markerMapper: MarkerMapper = MarkerMapper()
) : IResourceRepository {
    private val chunkDao = database.getChunkDao()
    private val takeDao = database.getTakeDao()
    private val markerDao = database.getMarkerDao()
    private val resourceLinkDao = database.getResourceLinkDao()

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
                            .map(this::buildResource)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getByCollection(collection: Collection): Single<List<Chunk>> {
        return Single
                .fromCallable {
                    resourceLinkDao
                            .fetchByCollectionId(collection.id)
                            .map {
                                chunkDao.fetchById(it.resourceChunkFk)
                            }
                            .map(this::buildResource)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getByChunk(chunk: Chunk): Single<List<Chunk>> {
        return Single
                .fromCallable {
                    resourceLinkDao
                            .fetchByChunkId(chunk.id)
                            .map {
                                chunkDao.fetchById(it.resourceChunkFk)
                            }
                            .map(this::buildResource)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun linkToChunk(resource: Chunk, chunk: Chunk): Completable {
        return Completable
                .fromAction {
                    // Check if already exists
                    val alreadyExists = resourceLinkDao
                            .fetchByChunkId(chunk.id)
                            .filter {
                                // Check for this link
                                it.resourceChunkFk == resource.id
                            }.isNotEmpty()

                    if (!alreadyExists) {
                        // Add the resource link
                        val entity = ResourceLinkEntity(
                                0,
                                resource.id,
                                chunk.id,
                                null
                        )
                        resourceLinkDao.insert(entity)
                    }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun linkToCollection(resource: Chunk, collection: Collection): Completable {
        return Completable
                .fromAction {
                    // Check if already exists
                    val alreadyExists = resourceLinkDao
                            .fetchByCollectionId(collection.id)
                            .filter {
                                // Check for this link
                                it.resourceChunkFk == resource.id
                            }.isNotEmpty()

                    if (!alreadyExists) {
                        // Add the resource link
                        val entity = ResourceLinkEntity(
                                0,
                                resource.id,
                                null,
                                collection.id
                        )
                        resourceLinkDao.insert(entity)
                    }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun unlinkFromChunk(resource: Chunk, chunk: Chunk): Completable {
        return Completable
                .fromAction {
                    // Check if exists
                    resourceLinkDao
                            .fetchByChunkId(chunk.id)
                            .filter {
                                // Check for this link
                                it.resourceChunkFk == resource.id
                            }
                            .forEach {
                                // Delete the link
                                resourceLinkDao.delete(it)
                            }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun unlinkFromCollection(resource: Chunk, collection: Collection): Completable {
        return Completable
                .fromAction {
                    // Check if exists
                    resourceLinkDao
                            .fetchByCollectionId(collection.id)
                            .filter {
                                // Check for this link
                                it.resourceChunkFk == resource.id
                            }
                            .forEach {
                                // Delete the link
                                resourceLinkDao.delete(it)
                            }
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

    private fun buildResource(entity: ChunkEntity): Chunk {
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