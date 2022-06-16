/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Translation

interface ILanguageRepository : IRepository<Language> {
    fun insert(language: Language): Single<Int>
    fun insertAll(languages: List<Language>): Single<List<Int>>
    fun upsertAll(languages: List<Language>): Completable
    fun updateRegions(languages: List<Language>): Completable
    fun getBySlug(slug: String): Single<Language>
    fun getGateway(): Single<List<Language>>
    fun getTargets(): Single<List<Language>>
    fun getTranslation(sourceLanguage: Language, targetLanguage: Language): Single<Translation>
    fun getAllTranslations(): Single<List<Translation>>
    fun insertTranslation(translation: Translation): Single<Int>
    fun updateTranslation(translation: Translation): Completable
    fun deleteTranslation(translation: Translation): Completable
}
