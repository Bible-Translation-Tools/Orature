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
