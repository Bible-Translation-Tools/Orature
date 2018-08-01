package persistence.repo

import data.model.User
import data.dao.Dao
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import persistence.mapping.UserMapper
import persistence.mapping.UserPreferencesMapper
import jooq.tables.daos.UserEntityDao
import jooq.tables.daos.UserLanguagesEntityDao
import jooq.tables.daos.UserPreferencesEntityDao
import jooq.tables.pojos.UserLanguagesEntity
import org.jooq.Configuration

class UserRepo(
    config: Configuration,
    private val userMapper: UserMapper,
    private val userPreferencesMapper: UserPreferencesMapper
) : Dao<User> {
    // uses generated dao to interact with database
    private val userEntityDao = UserEntityDao(config)
    private val userPreferencesEntityDao = UserPreferencesEntityDao(config)
    private val userLanguageEntityDao = UserLanguagesEntityDao(config)

    /**
     * function to create and insert a user into the database
     * takes in a audioHash and a path to a recording to creaete
     */
    override fun insert(user: User): Observable<Int> {
        // creates observable to return generated int
        return Observable.fromCallable {
            userMapper.mapToEntity(Observable.just(user))
        }.flatMap {
            it
        }.map {
            userEntityDao.insert(it)
            userEntityDao.fetchByAudiohash(it.audiohash).first()
        }.flatMap {
            user.id = it.id
            updateUserLanguageReferences(user, it.id)
            userPreferencesMapper.mapToEntity(Observable.just(user.userPreferences))
        }.map {
            userPreferencesEntityDao.insert(it)
            user.id
        }.subscribeOn(Schedulers.io())
    }

    /**
     * gets user by Id
     */
    override fun getById(id: Int): Observable<User> {
        return userMapper
            .mapFromEntity(Observable.fromCallable {
                userEntityDao.fetchById(id).first()
            }).subscribeOn(Schedulers.io())
    }

    /**
     * gets all the users currently stored in db
     */
    override fun getAll(): Observable<List<User>> {
        return Observable.fromCallable {
            userEntityDao.findAll().toList()
        }.flatMap {
            val userList = it.map { userMapper.mapFromEntity(Observable.just(it)) }
            Observable.zip(userList) {
                it.toList() as List<User>
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun update(user: User): Completable {
        return Completable.fromObservable(
            userMapper
                .mapToEntity(Observable.just(user))
                .flatMap {
                    userEntityDao.update(it)
                    updateUserLanguageReferences(user, user.id)
                    userPreferencesMapper.mapToEntity(Observable.just(user.userPreferences))
                }.doOnNext {
                    userPreferencesEntityDao.update(it)
                }
        ).subscribeOn(Schedulers.io())
    }

    /**
     * deletes user by id
     */
    override fun delete(user: User): Completable {
        return Completable.fromObservable(
            userMapper
                .mapToEntity(Observable.just(user))
                .map {
                    userEntityDao.delete(it)
                }
        ).subscribeOn(Schedulers.io())
    }

    /**
     * This function should be unnecessary because we have add and remove function
     * this then enforces clients to add and remove languages through the database
     * keeping just in case we want this
     */

    private fun updateUserLanguageReferences(user: User, userId: Int) {
        // inserts source and target languages into user language relationship table
        val newSourceUserLanguages = user
            .sourceLanguages
            .map {
                UserLanguagesEntity(
                    userId,
                    it.id,
                    1
                )
            }
        val newTargetUserLanguages = user
            .targetLanguages
            .map {
                UserLanguagesEntity(
                    userId,
                    it.id,
                    0
                )

            }
        val newUserLanguages = newTargetUserLanguages.union(newSourceUserLanguages)
        // blocking first might be okay since this entire function is used in a observable doOnNext
        // may be able to be refactored to avoid this
        val userLanguages = userLanguageEntityDao.fetchByUserfk(userId)
        newUserLanguages.forEach { newUserLanguage ->
            // only insert the userlanguage into the junction table if the row doesn't already exist
            if (userLanguages.all {
                    it.languagefk != newUserLanguage.languagefk || it.issource != newUserLanguage.issource
                }
            ) {
                // inserting language reference
                userLanguageEntityDao.insert(newUserLanguage)
            }
        }

        userLanguages.forEach { userLanguage ->
            if (newUserLanguages.all {
                    it.languagefk != userLanguage.languagefk || it.issource != userLanguage.issource
                }
            ) {
                userLanguageEntityDao.delete(userLanguage)
            }
        }
    }

}