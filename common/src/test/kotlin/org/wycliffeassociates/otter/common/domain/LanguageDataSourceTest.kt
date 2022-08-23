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
            .fetchLanguageNames("https://td.unfoldingword.org/exports/langnames.json")
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