package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.device.audioplugin.AudioPlugin
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.AudioPluginDataMapper

class AudioPluginRepository(
        database: AppDatabase,
        private val preferences: IAppPreferences,
        private val mapper: AudioPluginDataMapper = AudioPluginDataMapper()
) : IAudioPluginRepository {
    private val audioPluginDao = database.getAudioPluginDao()

    override fun insert(obj: AudioPluginData): Single<Int> {
        return Single
                .fromCallable {
                    audioPluginDao.insert(mapper.mapToEntity(obj))
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
                    // Update the preferences if necessary
                    if (preferences.recorderPluginId() == obj.id) preferences.setRecorderPluginId(-1)
                    if (preferences.editorPluginId() == obj.id) preferences.setEditorPluginId(-1)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun initSelected(): Completable {
        return Completable
                .fromAction {
                    val allPlugins = audioPluginDao.fetchAll()
                    if (allPlugins.isNotEmpty()) {
                        val editPlugins = allPlugins.filter { it.edit == 1 }
                        val recordPlugins = allPlugins.filter { it.record == 1 }
                        if (preferences.editorPluginId() == null && editPlugins.isNotEmpty()) {
                            preferences.setEditorPluginId(editPlugins.first().id)
                        }
                        if (preferences.recorderPluginId() == null && recordPlugins.isNotEmpty()) {
                            preferences.setRecorderPluginId(recordPlugins.first().id)
                        }
                    }
                }
    }

    override fun getEditorData(): Maybe<AudioPluginData> {
        val editorId = preferences.editorPluginId()
        return if (editorId == null)
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

    override fun setEditorData(default: AudioPluginData): Completable {
        return Completable
                .fromAction {
                    if (default.canEdit) preferences.setEditorPluginId(default.id)
                }
    }

    override fun getRecorderData(): Maybe<AudioPluginData> {
        val recorderId = preferences.recorderPluginId()
        return if (recorderId == null)
            Maybe.empty()
        else {
            Maybe.fromCallable {
                mapper.mapFromEntity(audioPluginDao.fetchById(recorderId))
            }
                    .onErrorComplete()
                    .subscribeOn(Schedulers.io())
        }
    }

    override fun getRecorder(): Maybe<IAudioPlugin> = getRecorderData().map { AudioPlugin(it) }

    override fun setRecorderData(default: AudioPluginData): Completable {
        return Completable
                .fromAction {
                    if (default.canRecord) preferences.setRecorderPluginId(default.id)
                }
    }
}