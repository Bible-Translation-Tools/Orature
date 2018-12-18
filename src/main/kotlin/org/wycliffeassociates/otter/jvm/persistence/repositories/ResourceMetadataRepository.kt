package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceMetadataEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper
import java.sql.DatabaseMetaData

class ResourceMetadataRepository(
        database: AppDatabase,
        private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
        private val languageMapper: LanguageMapper = LanguageMapper()
) : IResourceMetadataRepository {
    private val resourceMetadataDao = database.getResourceMetadataDao()
    private val languageDao = database.getLanguageDao()

    override fun insert(obj: ResourceMetadata): Single<Int> {
        return Single
                .fromCallable {
                    resourceMetadataDao.insert(metadataMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<ResourceMetadata>> {
        return Single
                .fromCallable {
                    resourceMetadataDao
                            .fetchAll()
                            .map(this::buildMetadata)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getSource(metadata: ResourceMetadata): Maybe<ResourceMetadata> {
        return Maybe
                .fromCallable {
                    resourceMetadataDao.fetchById(metadata.id).derivedFromFk
                }
                .map { buildMetadata(resourceMetadataDao.fetchById(it)) }
                .subscribeOn(Schedulers.io())
    }

    override fun getLinked(metadata: ResourceMetadata): Single<List<ResourceMetadata>> {
        return Single
                .fromCallable {
                    resourceMetadataDao
                            .fetchLinks(metadata.id)
                            .map(this::buildMetadata)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateSource(metadata: ResourceMetadata, source: ResourceMetadata?): Completable {
        return Completable
                .fromAction {
                    val updated = metadataMapper.mapToEntity(metadata, source?.id)
                    resourceMetadataDao.update(updated)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun update(obj: ResourceMetadata): Completable {
        return Completable
                .fromAction {
                    val existing = resourceMetadataDao.fetchById(obj.id)
                    val updated = metadataMapper.mapToEntity(obj, existing.derivedFromFk)
                    resourceMetadataDao.update(updated)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun addLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable {
        return Completable
                .fromAction {
                    resourceMetadataDao.addLink(firstMetadata.id, secondMetadata.id)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun removeLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable {
        return Completable
                .fromAction {
                    resourceMetadataDao.removeLink(firstMetadata.id, secondMetadata.id)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: ResourceMetadata): Completable {
        return Completable
                .fromAction {
                    resourceMetadataDao.delete(metadataMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    private fun buildMetadata(entity: ResourceMetadataEntity): ResourceMetadata {
        val language = languageMapper
                .mapFromEntity(languageDao.fetchById(entity.languageFk))
        return metadataMapper.mapFromEntity(entity, language)
    }
}