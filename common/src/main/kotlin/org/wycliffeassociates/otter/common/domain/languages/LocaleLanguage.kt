/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import java.util.*
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.ILocaleDataSource
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject
import org.wycliffeassociates.otter.common.persistence.IAppPreferences

private const val DEFAULT_LANGUAGE_SLUG = "en"

/**
 * ISO 639 language codes that were changed,
 * but Locale api still returns old values
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Locale.html">Legacy language codes section</a>
 * @see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes">ISO 639 language codes</a>
 */
private val EXCEPTIONAL_LANGUAGES = mapOf(
    "id" to "in",
    "he" to "iw",
    "yi" to "ji"
)

class LocaleLanguage @Inject constructor(
    private val appPrefRepo: IAppPreferences,
    private val langRepo: ILanguageRepository,
    private val localeDataSource: ILocaleDataSource
) {

    val preferredLanguage: Language?
        get() = preferredLanguage()

    val defaultLanguage: Language?
        get() = defaultLanguage()

    val supportedLanguages: List<Language>
        get() = supportedLanguages()

    fun preferredLocale(): Locale {
        val prefLocale = getLanguageFromPrefs()
        if (prefLocale.isNotEmpty()) {
            return Locale(prefLocale)
        } else {
            val defaultLocale = localeDataSource.getDefaultLocale()
            if (localeDataSource.getSupportedLocales().contains(defaultLocale)) {
                appPrefRepo.setLocaleLanguage(defaultLocale)
                return Locale(defaultLocale)
            } else {
                appPrefRepo.setLocaleLanguage(DEFAULT_LANGUAGE_SLUG)
                return Locale(DEFAULT_LANGUAGE_SLUG)
            }
        }
    }

    private fun preferredLanguage(): Language? {
        val language = preferredLocale().language
        val normalized = normalizeLocale(language)
        return when {
            supportedLanguages.map { it.slug }.contains(normalized) -> langRepo.getBySlug(normalized).blockingGet()
            else -> defaultLanguage
        }
    }

    fun setPreferredLanguage(language: Language): Completable {
        return appPrefRepo.setLocaleLanguage(language.slug)
    }

    private fun defaultLanguage(): Language? {
        val systemLocale = localeDataSource.getDefaultLocale()
        val normalized = normalizeLocale(systemLocale)
        val systemLanguage = langRepo.getBySlug(normalized).blockingGet()
        return when {
            supportedLanguages.contains(systemLanguage) -> systemLanguage
            else -> langRepo.getBySlug(DEFAULT_LANGUAGE_SLUG).blockingGet()
        }
    }

    private fun supportedLanguages(): List<Language> {
        return langRepo.getAll().blockingGet()
            .filter(::filterExceptionalLanguages)
            .sortedBy { it.slug }
    }

    private fun getLanguageFromPrefs(): String {
        return appPrefRepo.localeLanguage().blockingGet()
    }

    private fun filterExceptionalLanguages(lang: Language): Boolean {
        val locales = localeDataSource.getSupportedLocales()
        return locales.contains(lang.slug) || locales.contains(EXCEPTIONAL_LANGUAGES[lang.slug])
    }

    private fun normalizeLocale(old: String): String {
        return EXCEPTIONAL_LANGUAGES.entries.singleOrNull { it.value == old }?.key ?: old
    }
}
