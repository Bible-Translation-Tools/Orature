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
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.data.primitives.Language

/**
 * Provides an API to access app preferences.
 *
 * @see org.wycliffeassociates.otter.common.persistence.IAppPreferences
 */
interface IAppPreferencesRepository {
    fun resumeProjectId(): Single<Int>
    fun setResumeProjectId(id: Int): Completable
    fun lastResource(): Single<String>
    fun setLastResource(resource: String): Completable
    fun getOutputDevice(): Single<String>
    fun setOutputDevice(mixer: String): Completable
    fun getInputDevice(): Single<String>
    fun setInputDevice(mixer: String): Completable
    fun localeLanguage(): Maybe<Language>
    fun setLocaleLanguage(language: Language): Completable
    fun appTheme(): Single<ColorTheme>
    fun setAppTheme(theme: ColorTheme): Completable
    fun sourceTextZoomRate(): Single<Int>
    fun setSourceTextZoomRate(rate: Int): Completable
    fun languageNamesUrl(): Single<String>
    fun setLanguageNamesUrl(server: String): Completable
    fun defaultLanguageNamesUrl(): Single<String>
    fun resetLanguageNamesUrl(): Single<String>

}
