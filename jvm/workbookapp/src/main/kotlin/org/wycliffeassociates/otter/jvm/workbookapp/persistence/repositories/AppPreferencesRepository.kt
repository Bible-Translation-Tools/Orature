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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import java.util.*
import javax.inject.Inject

class AppPreferencesRepository @Inject constructor(
    private val preferences: IAppPreferences
) : IAppPreferencesRepository {

    override fun resumeProjectId(): Single<Int> {
        return preferences.resumeBookId()
    }

    override fun setResumeProjectId(id: Int): Completable {
        return preferences.setResumeBookId(id)
    }

    override fun lastResource(): Single<String> {
        return preferences.lastResource()
    }

    override fun setLastResource(resource: String): Completable {
        return preferences.setLastResource(resource)
    }

    override fun actualLocale(): Single<Locale> {
        return preferences
            .locale()
            .map {
                val parts = it.split("_", limit = 2)
                val language = parts[0]
                val country = parts.getOrNull(1) ?: ""
                Locale(language, country)
            }
    }

    override fun setActualLocale(locale: Locale): Completable {
        return preferences.setLocale(locale.toString())
    }
}
