package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.TranslationMapper
import javax.inject.Inject

class LanguageRepository @Inject constructor(
    database: AppDatabase,
    private val mapper: LanguageMapper,
    private val translationMapper: TranslationMapper
) : ILanguageRepository {
    private val logger = LoggerFactory.getLogger(LanguageRepository::class.java)

    private val languageDao = database.languageDao
    private val translationDao = database.translationDao

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

    override fun updateRegions(languages: List<Language>): Completable {
        return Completable
            .fromCallable {
                languageDao.updateRegions(languages.map(mapper::mapToEntity))
            }
            .doOnError { e ->
                logger.error("Error in updateRegions", e)
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

    override fun getAllTranslations(): Single<List<Translation>> {
        return Single.fromCallable {
            translationDao.fetchAll()
                .map {
                    val source = mapper
                        .mapFromEntity(languageDao.fetchById(it.sourceFk))
                    val target = mapper
                        .mapFromEntity(languageDao.fetchById(it.targetFk))
                    translationMapper.mapFromEntity(it, source, target)
                }
        }
    }

    override fun insertTranslation(translation: Translation): Single<Int> {
        return Single
            .fromCallable {
                translationDao.insert(translationMapper.mapToEntity(translation))
            }
            .doOnError { e ->
                logger.error("Error in insert for translation: $translation", e)
            }
            .subscribeOn(Schedulers.io())
    }
}
