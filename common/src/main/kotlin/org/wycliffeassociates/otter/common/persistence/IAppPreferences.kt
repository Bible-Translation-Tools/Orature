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
package org.wycliffeassociates.otter.common.persistence

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType

// interface to getting user-independent workbookapp preferences
interface IAppPreferences {
    fun currentUserId(): Single<Int>
    fun setCurrentUserId(userId: Int): Completable
    fun appInitialized(): Single<Boolean>
    fun setAppInitialized(initialized: Boolean): Completable
    fun pluginId(type: PluginType): Single<Int>
    fun setPluginId(type: PluginType, id: Int): Completable
    fun resumeBookId(): Single<Int>
    fun setResumeBookId(id: Int): Completable
    fun lastResource(): Single<String>
    fun setLastResource(resource: String): Completable
    fun audioOutputDevice(): Single<String>
    fun setAudioOutputDevice(name: String): Completable
    fun audioInputDevice(): Single<String>
    fun setAudioInputDevice(name: String): Completable
    fun locale(): Single<String>
    fun setLocale(locale: String): Completable
}
