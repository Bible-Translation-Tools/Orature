package org.wycliffeassociates.otter.jvm.persistence.repositories

import org.wycliffeassociates.otter.common.data.model.Language
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.device.audioplugin.AudioPlugin
import org.wycliffeassociates.otter.jvm.persistence.database.IAppDatabase
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.AudioPluginDataMapper

class AudioPluginRepository(
        database: IAppDatabase,
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
                }
                .subscribeOn(Schedulers.io())
    }
}