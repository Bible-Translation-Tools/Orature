package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.domain.LocaleDataSource
import java.util.*

class LocaleDataSourceTest {

    var localeDataSource = LocaleDataSource()

    @Test
    fun testHasSupportedLocales() {
        val supportedLocales = localeDataSource.getSupportedLocales()
        Assert.assertTrue(supportedLocales.isNotEmpty())
    }

    @Test
    fun testDefaultLocale() {
        val systemLocale = Locale.getDefault()
        val defaultLocaleLanguage = localeDataSource.getDefaultLocale()

        Assert.assertEquals(defaultLocaleLanguage, systemLocale.language)
    }
}
