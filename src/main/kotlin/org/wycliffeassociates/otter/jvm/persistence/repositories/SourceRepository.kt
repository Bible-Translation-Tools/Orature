package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.model.SourceCollection
import org.wycliffeassociates.otter.common.persistence.repositories.ISourceRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper


class SourceRepository(
        database: AppDatabase,
        private val collectionMapper: CollectionMapper = CollectionMapper(),
        private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
        private val languageMapper: LanguageMapper = LanguageMapper()
) : ISourceRepository {
    private val collectionDao = database.getCollectionDao()
    private val metadataDao = database.getResourceMetadataDao()
    private val languageDao = database.getLanguageDao()

    override fun delete(obj: SourceCollection): Completable {
        return Completable
                .fromAction {
                    collectionDao.delete(collectionMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<SourceCollection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchAll()
                            .map(this::buildSourceCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAllRoot(): Single<List<SourceCollection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchAll()
                            .filter { it.parentFk == null && it.sourceFk == null }
                            .map(this::buildSourceCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getByProjectCollection(project: ProjectCollection): Maybe<SourceCollection> {
        return Maybe
                .fromCallable {
                    buildSourceCollection(
                            collectionDao.fetchSource(collectionDao.fetchById(project.id))
                    )
                }
                .onErrorComplete()
                .subscribeOn(Schedulers.io())
    }

    override fun getChildren(source: SourceCollection): Single<List<SourceCollection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchChildren(collectionMapper.mapToEntity(source))
                            .map(this::buildSourceCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateParent(source: SourceCollection, newParent: SourceCollection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(source.id)
                    entity.parentFk = newParent.id
                    collectionDao.update(entity)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun insert(obj: SourceCollection): Single<Int> {
        return Single
                .fromCallable {
                    collectionDao.insert(collectionMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun update(obj: SourceCollection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(obj.id)
                    val newEntity = collectionMapper.mapToEntity(obj, entity.parentFk, entity.sourceFk)
                    collectionDao.update(newEntity)
                }
                .subscribeOn(Schedulers.io())
    }

    private fun buildSourceCollection(entity: CollectionEntity): SourceCollection {
        var metadata: ResourceMetadata? = null
        entity.metadataFk?.let {
            val metadataEntity = metadataDao.fetchById(it)
            val language = languageMapper.mapFromEntity(languageDao.fetchById(metadataEntity.languageFk))
            metadata = metadataMapper.mapFromEntity(metadataEntity, language)
        }

        return collectionMapper.mapFromEntity(entity, metadata)
    }
}