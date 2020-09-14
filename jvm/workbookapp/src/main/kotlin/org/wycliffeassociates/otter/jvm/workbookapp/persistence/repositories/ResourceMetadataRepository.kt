package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ResourceMetadataMapper
import kotlin.math.log

class ResourceMetadataRepository(
    database: AppDatabase,
    private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
    private val languageMapper: LanguageMapper = LanguageMapper()
) : IResourceMetadataRepository {
    private val logger = LoggerFactory.getLogger(ResourceMetadataRepository::class.java)

    private val resourceMetadataDao = database.resourceMetadataDao
    private val languageDao = database.languageDao

    override fun exists(metadata: ResourceMetadata): Single<Boolean> {
        return Single
            .fromCallable {
                val languageFk = languageDao.fetchBySlug(metadata.language.slug).id
                resourceMetadataDao.exists(languageFk, metadata.identifier, metadata.version, metadata.creator)
            }
            .doOnError { e ->
                logger.error("Error in exists for metadata: $metadata", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun get(metadata: ResourceMetadata): Single<ResourceMetadata> {
        return Single
            .fromCallable {
                val languageEntity = languageDao.fetchBySlug(metadata.language.slug)
                val metadataEntity = resourceMetadataDao.fetch(
                    languageEntity.id,
                    metadata.identifier,
                    metadata.version,
                    metadata.creator
                )
                metadataMapper.mapFromEntity(metadataEntity, languageMapper.mapFromEntity(languageEntity))
            }
            .doOnError { e ->
                logger.error("Error in get for metadata: $metadata", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insert(metadata: ResourceMetadata): Single<Int> {
        return Single
            .fromCallable {
                resourceMetadataDao.insert(metadataMapper.mapToEntity(metadata))
            }
            .doOnError { e ->
                logger.error("Error in insert for metadata: $metadata", e)
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
            .doOnError { e ->
                logger.error("Error in getAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getSource(metadata: ResourceMetadata): Maybe<ResourceMetadata> {
        return Maybe
            .fromCallable {
                resourceMetadataDao.fetchById(metadata.id).derivedFromFk
            }
            .map { buildMetadata(resourceMetadataDao.fetchById(it)) }
            .doOnError { e ->
                logger.error("Error in getSource for metadata: $metadata", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getLinked(metadata: ResourceMetadata): Single<List<ResourceMetadata>> {
        return Single
            .fromCallable {
                resourceMetadataDao
                    .fetchLinks(metadata.id)
                    .map(this::buildMetadata)
            }
            .doOnError { e ->
                logger.error("Error in getLinked for metadata: $metadata", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun updateSource(metadata: ResourceMetadata, source: ResourceMetadata?): Completable {
        return Completable
            .fromAction {
                val updated = metadataMapper.mapToEntity(metadata, source?.id)
                resourceMetadataDao.update(updated)
            }
            .doOnError { e ->
                logger.error("Error in updateSource for metadata: $metadata, source: $source", e)
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
            .doOnError { e ->
                logger.error("Error in update for metadata: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun addLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable {
        return Completable
            .fromAction {
                resourceMetadataDao.addLink(firstMetadata.id, secondMetadata.id)
            }
            .doOnError { e ->
                logger.error("Error in addLink for first: $firstMetadata, second: $secondMetadata", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun removeLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable {
        return Completable
            .fromAction {
                resourceMetadataDao.removeLink(firstMetadata.id, secondMetadata.id)
            }
            .doOnError { e ->
                logger.error("Error in removeLink for first: $firstMetadata, second: $secondMetadata", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: ResourceMetadata): Completable {
        return Completable
            .fromAction {
                resourceMetadataDao.delete(metadataMapper.mapToEntity(obj))
            }
            .doOnError { e ->
                logger.error("Error in delete for metadata: $obj", obj)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun buildMetadata(entity: ResourceMetadataEntity): ResourceMetadata {
        val language = languageMapper
            .mapFromEntity(languageDao.fetchById(entity.languageFk))
        return metadataMapper.mapFromEntity(entity, language)
    }
}
