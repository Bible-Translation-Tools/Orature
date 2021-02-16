package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import javax.inject.Inject

class LanguageRepository @Inject constructor(
    database: AppDatabase,
    private val mapper: LanguageMapper
) : ILanguageRepository {
    private val logger = LoggerFactory.getLogger(LanguageRepository::class.java)

    private val languageDao = database.languageDao

    override fun insert(language: Language): Single<Int> {
        return Single
            .fromCallable {
                languageDao.insert(mapper.mapToEntity(language))
            }
            .doOnError { e ->
                logger.error("Error in insert for language: $language", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insertAll(languages: List<Language>): Single<List<Int>> {
        return Single
            .fromCallable {
                languageDao.insertAll(languages.map(mapper::mapToEntity))
            }
            .doOnError { e ->
                logger.error("Error in insertAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Language>> {
        return Single
            .fromCallable {
                languageDao
                    .fetchAll()
                    .map { mapper.mapFromEntity(it) }
            }
            .doOnError { e ->
                logger.error("Error in getAll", e)
            }
            .subscribeOn(Schedulers.io())
    }

    /**
     * Gets the list of Gateway (Source) languages
     */
    override fun getGateway(): Single<List<Language>> {
        return Single
            .fromCallable {
                languageDao
                    .fetchGateway()
                    .map { mapper.mapFromEntity(it) }
            }
            .doOnError { e ->
                logger.error("Error in getGateway", e)
            }
            .subscribeOn(Schedulers.io())
    }

    /**
     * Gets the list of Target (Heart) languages
     */
    override fun getTargets(): Single<List<Language>> {
        return Single
            .fromCallable {
                languageDao
                    .fetchTargets()
                    .map { mapper.mapFromEntity(it) }
            }
            .doOnError { e ->
                logger.error("Error in getTargets", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getBySlug(slug: String): Single<Language> {
        return Single
            .fromCallable {
                mapper.mapFromEntity(languageDao.fetchBySlug(slug))
            }
            .doOnError { e ->
                logger.error("Error in getBySlug for slug: $slug", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Language): Completable {
        return Completable
            .fromAction {
                languageDao.update(mapper.mapToEntity(obj))
            }
            .doOnError { e ->
                logger.error("Error in update for language: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: Language): Completable {
        return Completable
            .fromAction {
                languageDao.delete(mapper.mapToEntity(obj))
            }
            .doOnError { e ->
                logger.error("Error in delete for language: $obj", e)
            }
            .subscribeOn(Schedulers.io())
    }
}
