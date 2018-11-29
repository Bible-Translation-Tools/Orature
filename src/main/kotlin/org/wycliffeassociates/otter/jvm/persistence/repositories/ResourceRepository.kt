package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.TakeMapper

class ResourceRepository(
        database: AppDatabase,
        private val contentMapper: ContentMapper = ContentMapper(),
        private val takeMapper: TakeMapper = TakeMapper(),
        private val markerMapper: MarkerMapper = MarkerMapper()
) : IResourceRepository {
    private val contentDao = database.getContentDao()
    private val takeDao = database.getTakeDao()
    private val markerDao = database.getMarkerDao()
    private val resourceLinkDao = database.getResourceLinkDao()

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
                            .map(this::buildResource)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getByCollection(collection: Collection): Single<List<Content>> {
        return Single
                .fromCallable {
                    resourceLinkDao
                            .fetchByCollectionId(collection.id)
                            .map {
                                contentDao.fetchById(it.resourceContentFk)
                            }
                            .map(this::buildResource)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getByContent(content: Content): Single<List<Content>> {
        return Single
                .fromCallable {
                    resourceLinkDao
                            .fetchByContentId(content.id)
                            .map {
                                contentDao.fetchById(it.resourceContentFk)
                            }
                            .map(this::buildResource)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun linkToContent(resource: Content, content: Content): Completable {
        return Completable
                .fromAction {
                    // Check if already exists
                    val alreadyExists = resourceLinkDao
                            .fetchByContentId(content.id)
                            .filter {
                                // Check for this link
                                it.resourceContentFk == resource.id
                            }.isNotEmpty()

                    if (!alreadyExists) {
                        // Add the resource link
                        val entity = ResourceLinkEntity(
                                0,
                                resource.id,
                                content.id,
                                null
                        )
                        resourceLinkDao.insert(entity)
                    }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun linkToCollection(resource: Content, collection: Collection): Completable {
        return Completable
                .fromAction {
                    // Check if already exists
                    val alreadyExists = resourceLinkDao
                            .fetchByCollectionId(collection.id)
                            .filter {
                                // Check for this link
                                it.resourceContentFk == resource.id
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

    override fun unlinkFromContent(resource: Content, content: Content): Completable {
        return Completable
                .fromAction {
                    // Check if exists
                    resourceLinkDao
                            .fetchByContentId(content.id)
                            .filter {
                                // Check for this link
                                it.resourceContentFk == resource.id
                            }
                            .forEach {
                                // Delete the link
                                resourceLinkDao.delete(it)
                            }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun unlinkFromCollection(resource: Content, collection: Collection): Completable {
        return Completable
                .fromAction {
                    // Check if exists
                    resourceLinkDao
                            .fetchByCollectionId(collection.id)
                            .filter {
                                // Check for this link
                                it.resourceContentFk == resource.id
                            }
                            .forEach {
                                // Delete the link
                                resourceLinkDao.delete(it)
                            }
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

    private fun buildResource(entity: ContentEntity): Content {
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