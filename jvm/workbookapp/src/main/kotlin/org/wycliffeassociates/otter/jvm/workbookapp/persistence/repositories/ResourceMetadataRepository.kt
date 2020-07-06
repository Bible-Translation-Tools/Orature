package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ResourceMetadataMapper

class ResourceMetadataRepository(
    database: AppDatabase,
    private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
    private val languageMapper: LanguageMapper = LanguageMapper()
) : IResourceMetadataRepository {
    private val resourceMetadataDao = database.resourceMetadataDao
    private val languageDao = database.languageDao

    override fun exists(metadata: ResourceMetadata): Single<Boolean> {
        return Single.fromCallable {
            val languageFk = languageDao.fetchBySlug(metadata.language.slug).id
            resourceMetadataDao.exists(languageFk, metadata.identifier, metadata.version, metadata.creator)
        }.subscribeOn(Schedulers.io())
    }

    override fun get(metadata: ResourceMetadata): Single<ResourceMetadata> {
        return Single.fromCallable {
            val languageEntity = languageDao.fetchBySlug(metadata.language.slug)
            val metadataEntity = resourceMetadataDao.fetch(
                languageEntity.id,
                metadata.identifier,
                metadata.version,
                metadata.creator
            )
            metadataMapper.mapFromEntity(metadataEntity, languageMapper.mapFromEntity(languageEntity))
        }.subscribeOn(Schedulers.io())
    }

    override fun insert(metadata: ResourceMetadata): Single<Int> {
        return Single
            .fromCallable {
                resourceMetadataDao.insert(metadataMapper.mapToEntity(metadata))
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
