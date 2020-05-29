package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.data.config.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.AudioPlugin
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.AppPreferences
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.AudioPluginDataMapper

class AudioPluginRepository(
    database: AppDatabase,
    private val preferences: IAppPreferences,
    private val mapper: AudioPluginDataMapper = AudioPluginDataMapper()
) : IAudioPluginRepository {
    private val audioPluginDao = database.audioPluginDao

    override fun insert(data: AudioPluginData): Single<Int> {
        return Single
            .fromCallable {
                audioPluginDao.insert(mapper.mapToEntity(data))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<AudioPluginData>> {
        return Single
            .fromCallable {
                audioPluginDao
                    .fetchAll()
                    .map { mapper.mapFromEntity(it) }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getAllPlugins(): Single<List<IAudioPlugin>> {
        return getAll()
            .map {
                it.map { AudioPlugin(it) }
            }
    }

    override fun update(obj: AudioPluginData): Completable {
        return Completable
            .fromAction {
                audioPluginDao.update(mapper.mapToEntity(obj))
            }
            .subscribeOn(Schedulers.io())
    }

    override fun delete(obj: AudioPluginData): Completable {
        return Completable
            .fromAction {
                obj.pluginFile?.let { if (it.exists()) it.delete() }
                audioPluginDao.delete(mapper.mapToEntity(obj))
            }
            // Update the preferences if necessary
            .andThen(preferences.recorderPluginId())
            .flatMapCompletable {
                if (it == obj.id)
                    return@flatMapCompletable preferences.setRecorderPluginId(-1)
                else
                    return@flatMapCompletable Completable.complete()
            }
            .andThen(preferences.editorPluginId())
            .flatMapCompletable {
                if (it == obj.id)
                    return@flatMapCompletable preferences.setEditorPluginId(-1)
                else
                    return@flatMapCompletable Completable.complete()
            }
            .subscribeOn(Schedulers.io())
    }

    override fun initSelected(): Completable =
        Single
            .fromCallable {
                audioPluginDao.fetchAll()
            }
            .flatMapCompletable { allPlugins ->
                if (allPlugins.isEmpty()) {
                    Completable.complete()
                } else {
                    preferences.editorPluginId()
                        .flatMapCompletable { editorId ->
                            val editPlugins = allPlugins.filter { it.edit == 1 }
                            if (editorId == AppPreferences.NO_ID && editPlugins.isNotEmpty()) {
                                preferences.setEditorPluginId(editPlugins.first().id)
                            } else {
                                Completable.complete()
                            }
                        }
                        .andThen(preferences.recorderPluginId())
                        .flatMapCompletable { recorderId ->
                            val recordPlugins = allPlugins.filter { it.record == 1 }
                            if (recorderId == AppPreferences.NO_ID && recordPlugins.isNotEmpty()) {
                                preferences.setRecorderPluginId(recordPlugins.first().id)
                            } else {
                                Completable.complete()
                            }
                        }
                }
            }
            .subscribeOn(Schedulers.io())

    override fun getEditorData(): Maybe<AudioPluginData> =
        preferences.editorPluginId()
            .flatMapMaybe { editorId ->
                if (editorId == AppPreferences.NO_ID)
                    Maybe.empty()
                else {
                    Maybe.fromCallable {
                        mapper.mapFromEntity(audioPluginDao.fetchById(editorId))
                    }
                        .onErrorComplete()
                        .subscribeOn(Schedulers.io())
                }
            }

    override fun getEditor(): Maybe<IAudioPlugin> = getEditorData().map { AudioPlugin(it) }

    override fun setEditorData(default: AudioPluginData): Completable =
        if (default.canEdit) preferences.setEditorPluginId(default.id) else Completable.complete()

    override fun getRecorderData(): Maybe<AudioPluginData> =
        preferences.recorderPluginId()
            .flatMapMaybe { recorderId ->
                if (recorderId == AppPreferences.NO_ID)
                    return@flatMapMaybe Maybe.empty<AudioPluginData>()
                else {
                    return@flatMapMaybe Maybe.fromCallable {
                        mapper.mapFromEntity(audioPluginDao.fetchById(recorderId))
                    }
                        .onErrorComplete()
                        .subscribeOn(Schedulers.io())
                }
            }

    override fun getRecorder(): Maybe<IAudioPlugin> = getRecorderData().map { AudioPlugin(it) }

    override fun setRecorderData(default: AudioPluginData): Completable =
        if (default.canRecord) preferences.setRecorderPluginId(default.id) else Completable.complete()
}