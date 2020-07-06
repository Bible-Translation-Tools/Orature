package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper

class LanguageRepository(
    database: AppDatabase,
    private val mapper: LanguageMapper = LanguageMapper()
) : ILanguageRepository {
    private val languageDao = database.languageDao

    override fun insert(language: Language): Single<Int> {
        return Single
            .fromCallable {
                languageDao.insert(mapper.mapToEntity(language))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insertAll(languages: List<Language>): Single<List<Int>> {
        return Single
            .fromCallable {
                languageDao.insertAll(languages.map(mapper::mapToEntity))
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
            .subscribeOn(Schedulers.io())
    }

    override fun getGateway(): Single<List<Language>> {
        return Single
            .fromCallable {
                languageDao
                    .fetchGateway()
                    .map { mapper.mapFromEntity(it) }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getTargets(): Single<List<Language>> {
        return Single
            .fromCallable {
                languageDao
                    .fetchTargets()
                    .map { mapper.mapFromEntity(it) }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getBySlug(slug: String): Single<Language> {
        return Single
            .fromCallable {
                mapper.mapFromEntity(languageDao.fetchBySlug(slug))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Language): Completable {
        return Completable
            .fromAction {
                languageDao.update(mapper.mapToEntity(obj))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: Language): Completable {
        return Completable
            .fromAction {
                languageDao.delete(mapper.mapToEntity(obj))
            }
            .subscribeOn(Schedulers.io())
    }
}
