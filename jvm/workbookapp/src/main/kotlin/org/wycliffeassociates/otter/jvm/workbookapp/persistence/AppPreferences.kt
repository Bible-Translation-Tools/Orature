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

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.PreferenceEntity
import javax.inject.Inject

// preferences object that stores user-independent preference data
class AppPreferences @Inject constructor(database: AppDatabase) : IAppPreferences {
    companion object {
        val NO_ID = -1
    }

    private val logger = LoggerFactory.getLogger(AppPreferences::class.java)

    private val preferenceDao = database.preferenceDao
    private val CURRENT_USER_ID_KEY = "currentUserId"
    private val APP_INIT_KEY = "appInitialized"
    private val EDITOR_PLUGIN_ID_KEY = "editorPluginId"
    private val RECORDER_PLUGIN_ID_KEY = "recorderPluginId"
    private val MARKER_PLUGIN_ID_KEY = "markerPluginId"
    private val RESUME_BOOK_ID_KEY = "resumeBookId"
    private val LAST_RESOURCE_KEY = "lastResource"

    private fun putInt(key: String, value: Int): Completable {
        return Completable
            .fromAction {
                preferenceDao.upsert(PreferenceEntity(key, value.toString()))
            }
            .doOnError { e ->
                logger.error("Error in putInt for key: $key, value: $value", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun putBoolean(key: String, value: Boolean): Completable {
        return Completable
            .fromAction {
                preferenceDao.upsert(PreferenceEntity(key, value.toString()))
            }
            .doOnError { e ->
                logger.error("Error in putBoolean for key: $key, value: $value", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun putString(key: String, value: String): Completable {
        return Completable
            .fromAction {
                preferenceDao.upsert(PreferenceEntity(key, value))
            }
            .doOnError { e ->
                logger.error("Error in putString for key: $key, value: $value", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun getInt(key: String, def: Int): Single<Int> {
        return Single
            .fromCallable {
                var value = def
                try {
                    value = preferenceDao.fetchByKey(key).value.toInt()
                } catch (e: RuntimeException) {
                    // do nothing
                }
                return@fromCallable value
            }
            .doOnError { e ->
                logger.error("Error in getInt for $key, default: $def", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun getBoolean(key: String, def: Boolean): Single<Boolean> {
        return Single
            .fromCallable {
                var value = def
                try {
                    value = preferenceDao.fetchByKey(key).value.toBoolean()
                } catch (e: RuntimeException) {
                    // do nothing
                }
                return@fromCallable value
            }
            .doOnError { e ->
                logger.error("Error in getBoolean for key: $key, default: $def", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun getString(key: String, def: String): Single<String> {
        return Single
            .fromCallable {
                var value = def
                try {
                    value = preferenceDao.fetchByKey(key).value
                } catch (e: RuntimeException) {
                    // do nothing
                }
                return@fromCallable value
            }
            .doOnError { e ->
                logger.error("Error in getString for key: $key, default: $def", e)
            }
            .subscribeOn(Schedulers.io())
    }

    override fun currentUserId(): Single<Int> = getInt(CURRENT_USER_ID_KEY, -1)

    override fun setCurrentUserId(userId: Int): Completable = putInt(CURRENT_USER_ID_KEY, userId)

    override fun appInitialized(): Single<Boolean> = getBoolean(APP_INIT_KEY, false)

    override fun setAppInitialized(initialized: Boolean): Completable = putBoolean(APP_INIT_KEY, initialized)

    override fun pluginId(type: PluginType): Single<Int> {
        return getInt(getPluginKeyByType(type), -1)
    }

    override fun setPluginId(type: PluginType, id: Int): Completable {
        return putInt(getPluginKeyByType(type), id)
    }

    override fun resumeBookId(): Single<Int> {
        return getInt(RESUME_BOOK_ID_KEY, -1)
    }

    override fun setResumeBookId(id: Int): Completable {
        return putInt(RESUME_BOOK_ID_KEY, id)
    }

    private fun getPluginKeyByType(type: PluginType): String {
        return when (type) {
            PluginType.RECORDER -> RECORDER_PLUGIN_ID_KEY
            PluginType.EDITOR -> EDITOR_PLUGIN_ID_KEY
            PluginType.MARKER -> MARKER_PLUGIN_ID_KEY
        }
    }

    override fun lastResource(): Single<String> {
        return getString(LAST_RESOURCE_KEY, "")
    }

    override fun setLastResource(resource: String): Completable {
        return putString(LAST_RESOURCE_KEY, resource)
    }
}
