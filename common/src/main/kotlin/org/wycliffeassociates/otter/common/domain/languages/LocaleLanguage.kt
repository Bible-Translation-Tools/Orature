/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.languages

import io.reactivex.Completable
import io.reactivex.Maybe
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.ILocaleDataStore
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject

class LocaleLanguage @Inject constructor(
    private val appPrefRepo: IAppPreferencesRepository,
    private val langRepo: ILanguageRepository,
    private val localeDataStore: ILocaleDataStore
) {

    val preferredLanguage: Language?
        get() = preferredLanguage()

    val defaultLanguage: Language?
        get() = defaultLanguage()

    val supportedLanguages: List<Language>
        get() = supportedLanguages()

    private fun preferredLanguage(): Language? {
        val language = getLanguageFromPrefs()
        return when {
            supportedLanguages.contains(language) -> language
            else -> defaultLanguage
        }
    }

    fun setPreferredLanguage(language: Language): Completable {
        return appPrefRepo.setLocaleLanguage(language)
    }

    private fun defaultLanguage(): Language? {
        val systemLocale = localeDataStore.getDefaultLocale()
        val systemLanguage = getLanguageBySlug(systemLocale)
        return when {
            supportedLanguages.contains(systemLanguage) -> systemLanguage
            else -> getLanguageBySlug("en")
        }
    }

    private fun supportedLanguages(): List<Language> {
        val locales = localeDataStore.getSupportedLocales()
        return langRepo.getAll().blockingGet()
            .filter {
                locales.contains(it.slug)
            }
            .sortedBy { it.slug }
    }

    private fun getLanguageBySlug(slug: String): Language? {
        return langRepo.getBySlug(slug)
            .flatMapMaybe { Maybe.just(it) }
            .onErrorComplete()
            .blockingGet()
    }

    private fun getLanguageFromPrefs(): Language? {
        return appPrefRepo.localeLanguage()
            .onErrorComplete()
            .blockingGet()
    }
}
