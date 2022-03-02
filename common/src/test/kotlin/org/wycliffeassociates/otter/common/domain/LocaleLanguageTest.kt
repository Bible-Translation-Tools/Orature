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
package org.wycliffeassociates.otter.common.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.ILocaleDataSource
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository

class LocaleLanguageTest {

    private val english = Language("en", "", "", "", true, "")
    private val spanish = Language("es", "", "", "", true, "")
    private val french = Language("fr", "", "", "", true, "")
    private val supportedLanguages = listOf(english, spanish, french)

    private val appPref = mock<IAppPreferences>()
    private val langRepo = mock<ILanguageRepository>()
    private val localeDataSource = mock<ILocaleDataSource>()
    private val localeLanguage = LocaleLanguage(appPref, langRepo, localeDataSource)

    @Before
    fun setup() {
        Mockito.`when`(langRepo.getBySlug(any())).thenAnswer { answer ->
            getLangBySlug(answer.arguments.single().toString())
        }
        Mockito.`when`(langRepo.getAll()).thenReturn(Single.just(supportedLanguages))
        Mockito.`when`(localeDataSource.getSupportedLocales()).thenReturn(supportedLanguages.map { it.slug })
    }

    @Test
    fun `test default language`() {
        Mockito.`when`(localeDataSource.getDefaultLocale()).thenReturn(english.slug)

        Assert.assertEquals(localeLanguage.defaultLanguage, english)
    }

    @Test
    fun `test preferred language defaults to system locale when not set`() {
        Mockito.`when`(localeDataSource.getDefaultLocale()).thenReturn(french.slug)
        Mockito.`when`(appPref.localeLanguage()).thenReturn(Single.just(""))

        Assert.assertEquals(localeLanguage.preferredLanguage, french)
    }

    @Test
    fun `test preferred language`() {
        Mockito.`when`(localeDataSource.getDefaultLocale()).thenReturn(english.slug)
        Mockito.`when`(appPref.localeLanguage()).thenReturn(Single.just(spanish.slug))

        Assert.assertEquals(localeLanguage.preferredLanguage, spanish)
    }

    @Test
    fun `supported languages list is not empty`() {
        Assert.assertTrue(localeLanguage.supportedLanguages.isNotEmpty())
    }

    private fun getLangBySlug(slug: String): Single<Language> {
        return Single.fromCallable {
            supportedLanguages.single { it.slug == slug }
        }
    }
}
