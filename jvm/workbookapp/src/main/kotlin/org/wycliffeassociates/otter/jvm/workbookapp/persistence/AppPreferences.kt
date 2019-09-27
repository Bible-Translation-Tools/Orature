package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.PreferenceEntity

// preferences object that stores user-independent preference data
class AppPreferences(database: AppDatabase) : IAppPreferences {
    companion object {
        val NO_ID = -1
    }
    private val preferenceDao = database.preferenceDao
    private val CURRENT_USER_ID_KEY = "currentUserId"
    private val APP_INIT_KEY = "appInitialized"
    private val EDITOR_PLUGIN_ID_KEY = "editorPluginId"
    private val RECORDER_PLUGIN_ID_KEY = "recorderPluginId"

    private fun putInt(key: String, value: Int): Completable =
            Completable.fromAction {
                preferenceDao.upsert(PreferenceEntity(key, value.toString()))
            }.subscribeOn(Schedulers.io())

    private fun putBoolean(key: String, value: Boolean): Completable =
            Completable.fromAction {
                preferenceDao.upsert(PreferenceEntity(key, value.toString()))
            }.subscribeOn(Schedulers.io())

    private fun getInt(key: String, def: Int): Single<Int> =
            Single.fromCallable {
                var value = def
                try {
                    value = preferenceDao.fetchByKey(key).value.toInt()
                } catch (e: RuntimeException) {
                    // do nothing
                }
                return@fromCallable value
            }.subscribeOn(Schedulers.io())

    private fun getBoolean(key: String, def: Boolean): Single<Boolean> =
            Single.fromCallable {
                var value = def
                try {
                    value = preferenceDao.fetchByKey(key).value.toBoolean()
                } catch (e: RuntimeException) {
                    // do nothing
                }
                return@fromCallable value
            }.subscribeOn(Schedulers.io())

    override fun currentUserId(): Single<Int> = getInt(CURRENT_USER_ID_KEY, -1)

    override fun setCurrentUserId(userId: Int): Completable = putInt(CURRENT_USER_ID_KEY, userId)

    override fun appInitialized(): Single<Boolean> = getBoolean(APP_INIT_KEY, false)

    override fun setAppInitialized(initialized: Boolean): Completable = putBoolean(APP_INIT_KEY, initialized)

    override fun editorPluginId(): Single<Int> = getInt(EDITOR_PLUGIN_ID_KEY, -1)

    override fun setEditorPluginId(id: Int): Completable = putInt(EDITOR_PLUGIN_ID_KEY, id)

    override fun recorderPluginId(): Single<Int> = getInt(RECORDER_PLUGIN_ID_KEY, -1)

    override fun setRecorderPluginId(id: Int): Completable = putInt(RECORDER_PLUGIN_ID_KEY, id)
}