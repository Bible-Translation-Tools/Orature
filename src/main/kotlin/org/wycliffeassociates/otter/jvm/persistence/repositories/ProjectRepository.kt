package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.common.persistence.repositories.IProjectRepository
import org.wycliffeassociates.otter.jvm.persistence.database.IAppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper


class ProjectRepository(
        database: IAppDatabase,
        private val collectionMapper: CollectionMapper = CollectionMapper(),
        private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
        private val languageMapper: LanguageMapper = LanguageMapper()
) : IProjectRepository {
    private val collectionDao = database.getCollectionDao()
    private val metadataDao = database.getResourceMetadataDao()
    private val languageDao = database.getLanguageDao()

    override fun delete(obj: ProjectCollection): Completable {
        return Completable
                .fromAction {
                    collectionDao.delete(collectionMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<ProjectCollection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchAll()
                            .map(this::buildProjectCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAllRoot(): Single<List<ProjectCollection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchAll()
                            .filter { it.parentFk == null && it.sourceFk != null }
                            .map(this::buildProjectCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getChildren(project: ProjectCollection): Single<List<ProjectCollection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchChildren(collectionMapper.mapToEntity(project))
                            .map(this::buildProjectCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateSource(project: ProjectCollection, newSource: ProjectCollection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(project.id)
                    entity.sourceFk = newSource.id
                    collectionDao.update(entity)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateParent(project: ProjectCollection, newParent: ProjectCollection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(project.id)
                    entity.parentFk = newParent.id
                    collectionDao.update(entity)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun insert(obj: ProjectCollection): Single<Int> {
        return Single
                .fromCallable {
                    collectionDao.insert(collectionMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun update(obj: ProjectCollection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(obj.id)
                    val newEntity = collectionMapper.mapToEntity(obj)
                    // Don't overwrite existing
                    newEntity.parentFk = entity.parentFk
                    newEntity.sourceFk = entity.sourceFk
                    collectionDao.update(newEntity)
                }
                .subscribeOn(Schedulers.io())
    }

    private fun buildProjectCollection(entity: CollectionEntity): ProjectCollection {
        val metadataEntity = metadataDao
                .fetchById(entity.metadataFk)
        val language = languageMapper.mapFromEntity(
                languageDao.fetchById(metadataEntity.languageFk)
        )
        return collectionMapper.mapFromEntity(entity, metadataMapper.mapFromEntity(metadataEntity, language))
    }
}