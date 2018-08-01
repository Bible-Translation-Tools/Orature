package persistence.repo

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jooq.Configuration
import jooq.tables.pojos.UserLanguagesEntity
import jooq.tables.daos.UserLanguagesEntityDao

class UserLanguageRepo(private val config: Configuration) {
    private val userLanguageDao = UserLanguagesEntityDao(config)

    // note that this returns the userEntityId
    fun insert(userLanguageEntity: UserLanguagesEntity): Observable<Int> {
        return Observable.create<Int> {
            userLanguageDao.insert(userLanguageEntity)
            /*
            attempt to get the inserted language by fetch all languages related to user id
            then filters them to match the give userLanguageEntity
            throws no such element exception
             */
            try {
                it.onNext(
                    userLanguageDao
                        .fetchByUserfk(userLanguageEntity.userfk)
                        .filter {
                            it.languagefk == userLanguageEntity.languagefk
                                    && it.issource == userLanguageEntity.issource
                        }
                        .first()
                        .userfk
                )
            } catch (e: NoSuchElementException) {
                // rethrows exception with a more relevant message
                throw NoSuchElementException("User Language was not inserted into database")
            }
        }.subscribeOn(Schedulers.io())
    }

    // not inheriting from Dao<IUserLanguage> since a getById function
    // doesn't make sense in this context. reference table row has only composite key
    fun getByUserId(userId: Int): Observable<List<UserLanguagesEntity>> {
        return Observable.fromCallable {
            userLanguageDao.fetchByUserfk(userId)
        }.subscribeOn(Schedulers.io())
    }

    fun getAll(): Observable<List<UserLanguagesEntity>> {
        return Observable.fromCallable {
            userLanguageDao.findAll()
        }.subscribeOn(Schedulers.io())
    }

    // no update since user language table has all columns as part of composite key
    fun delete(userLanguageEntity: UserLanguagesEntity): Completable {
        return Completable.fromAction {
            userLanguageDao.delete(userLanguageEntity)
        }.subscribeOn(Schedulers.io())
    }
}