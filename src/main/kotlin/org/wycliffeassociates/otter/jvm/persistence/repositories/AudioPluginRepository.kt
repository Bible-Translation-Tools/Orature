package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.device.audioplugin.AudioPlugin
import org.wycliffeassociates.otter.jvm.persistence.AppPreferences
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.AudioPluginDataMapper

class AudioPluginRepository(
        database: AppDatabase,
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
                    audioPluginDao.delete(mapper.mapToEntity(obj))
                    // Update the preferences if necessary
                    if (AppPreferences.getRecorderPluginId() == obj.id) AppPreferences.setRecorderPluginId(-1)
                    if (AppPreferences.getEditorPluginId() == obj.id) AppPreferences.setEditorPluginId(-1)
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
                        if (AppPreferences.getEditorPluginId() == null && editPlugins.isNotEmpty()) {
                            AppPreferences.setEditorPluginId(editPlugins.first().id)
                        }
                        if (AppPreferences.getRecorderPluginId() == null && recordPlugins.isNotEmpty()) {
                            AppPreferences.setRecorderPluginId(recordPlugins.first().id)
                        }
                    }
                }
    }

    override fun getEditorData(): Maybe<AudioPluginData> {
        val editorId = AppPreferences.getEditorPluginId()
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
                    if (default.canEdit) AppPreferences.setEditorPluginId(default.id)
                }
    }

    override fun getRecorderData(): Maybe<AudioPluginData> {
        val recorderId = AppPreferences.getRecorderPluginId()
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
                    if (default.canRecord) AppPreferences.setRecorderPluginId(default.id)
                }
    }
}