package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Translation

interface ILanguageRepository : IRepository<Language> {
    fun insert(language: Language): Single<Int>
    fun insertAll(languages: List<Language>): Single<List<Int>>
    fun updateRegions(languages: List<Language>): Completable
    fun getBySlug(slug: String): Single<Language>
    fun getGateway(): Single<List<Language>>
    fun getTargets(): Single<List<Language>>
    fun getAllTranslations(): Single<List<Translation>>
    fun insertTranslation(translation: Translation): Single<Int>
}
