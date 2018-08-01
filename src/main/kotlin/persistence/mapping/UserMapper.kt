package persistence.mapping

import data.model.Language
import data.model.User
import data.dao.Dao
import data.mapping.Mapper
import data.model.UserPreferences
import persistence.repo.UserLanguageRepo
import jooq.tables.daos.UserPreferencesEntityDao
import jooq.tables.pojos.UserEntity
import io.reactivex.Observable
import org.reactfx.util.TriFunction
import io.reactivex.functions.Function3

class UserMapper(
    private val userLanguageRepo: UserLanguageRepo,
    private val languageRepo: Dao<Language>,
    private val userPreferencesEntityDao: UserPreferencesEntityDao
) : Mapper<Observable<UserEntity>, Observable<User>> {

    private val userPreferencesMapper = UserPreferencesMapper(languageRepo)

    override fun mapFromEntity(type: Observable<UserEntity>): Observable<User> {
        // queries to find all the source languages
        return type.flatMap {
            val userPreferences = userPreferencesMapper.mapFromEntity(
                Observable.just(userPreferencesEntityDao.fetchOneByUserfk(it.id))
            )
            val userLanguages = userLanguageRepo.getByUserId(it.id)
            val sourceLanguages = userLanguages.flatMap {
                val listSrcLanguages = it
                    .filter { it.issource == 1 }
                    .map { languageRepo.getById(it.languagefk) }
                Observable.zip(listSrcLanguages) { it.toList() as List<Language> }
            }
            val targetLanguages = userLanguages.flatMap {
                val listTarLanguages = it
                    .filter { it.issource == 0 }
                    .map { languageRepo.getById(it.languagefk) }
                Observable.zip(listTarLanguages) { it.toList() as List<Language> }
            }
            Observable.zip(sourceLanguages, targetLanguages, userPreferences,
                Function3<List<Language>, List<Language>, UserPreferences, User> { src, tar, pref ->
                    User(
                        it.id,
                        it.audiohash,
                        it.audiopath,
                        it.imgpath,
                        src.toMutableList(),
                        tar.toMutableList(),
                        pref
                    )
                })

        }
    }

    override fun mapToEntity(type: Observable<User>): Observable<UserEntity> {
        return type.map {
            UserEntity(
                it.id,
                it.audioHash,
                it.audioPath,
                it.imagePath
            )
        }
    }

}