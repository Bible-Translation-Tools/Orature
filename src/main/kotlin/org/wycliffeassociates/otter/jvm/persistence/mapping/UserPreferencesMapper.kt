package org.wycliffeassociates.otter.jvm.persistence.mapping

import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.UserPreferences
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.mapping.Mapper
import jooq.tables.pojos.UserPreferencesEntity
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class UserPreferencesMapper(private val languageRepo: Dao<Language>) :
    Mapper<Observable<UserPreferencesEntity>, Observable<UserPreferences>> {

    override fun mapFromEntity(type: Observable<UserPreferencesEntity>): Observable<UserPreferences> {
        // gets from database and maps preferred source and target language
        return type.flatMap {
            val preferredSourceLanguage = languageRepo.getById(it.sourcelanguagefk)
            val preferredTargetLanguage = languageRepo.getById(it.targetlanguagefk)
            Observable.zip(preferredSourceLanguage, preferredTargetLanguage,
                BiFunction<Language, Language, UserPreferences> { src, tar ->
                    UserPreferences(
                        it.userfk,
                        src,
                        tar
                    )
                })
        }
    }

    override fun mapToEntity(type: Observable<UserPreferences>): Observable<UserPreferencesEntity> {
        return type.map {
            UserPreferencesEntity(
                it.id,
                it.sourceLanguage.id,
                it.targetLanguage.id
            )
        }
    }
}