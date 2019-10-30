package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.common.persistence.repositories.IInitializationRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase

class InitializationRepository(
    private val database: AppDatabase
) : IInitializationRepository {

    private val initializationDao = database.initializationDao

    override fun getAll(): Single<List<Initialization>> {
        return Single
            .fromCallable {
                initializationDao.fetchAll()
            }
            .subscribeOn(Schedulers.io())
    }

    override fun insert(initialization: Initialization): Single<Int> {
        return Single
            .fromCallable {
                initializationDao.insert(initialization)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Initialization): Completable {
        return Single
            .fromCallable {
                initializationDao.insert(obj)
            }
            .ignoreElement()
            .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: Initialization): Completable {
        return Single
            .fromCallable {
                initializationDao.delete(obj)
            }
            .ignoreElement()
            .subscribeOn(Schedulers.io())
    }
}