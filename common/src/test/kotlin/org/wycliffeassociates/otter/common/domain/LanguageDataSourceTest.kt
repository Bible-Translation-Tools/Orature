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
import io.reactivex.Observable
import org.junit.Test
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.ILanguageDataSource

class LanguageDataSourceTest {

    private val languageDataSource = mock<ILanguageDataSource>()

    private val english = Language("en", "", "", "", true, "")

    @Test
    fun fetchSuccessful() {
        Mockito.`when`(languageDataSource.fetchLanguageNames(any()))
            .thenReturn(Observable.just(listOf(english)))

        val result = languageDataSource
            .fetchLanguageNames("https://langnames.bibleineverylanguage.org/langnames.json")
            .test()

        result.assertComplete()
        result.assertNoErrors()
        result.assertValue(listOf(english))
    }

    @Test
    fun fetchFailed() {
        Mockito.`when`(languageDataSource.fetchLanguageNames(any()))
            .thenReturn(Observable.error(Exception("Invalid Url")))

        val result = languageDataSource
            .fetchLanguageNames("https://invalid.url")
            .test()

        result.assertError(Exception::class.java)
        result.assertErrorMessage("Invalid Url")
        result.assertNotComplete()
    }
}