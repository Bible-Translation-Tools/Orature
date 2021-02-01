package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.data.config.IAudioPlugin

enum class PluginType {
    MARKER,
    RECORDER,
    EDITOR
}

interface IAudioPluginRepository : IRepository<AudioPluginData> {
    fun insert(data: AudioPluginData): Single<Int>
    fun getAllPlugins(): Single<List<IAudioPlugin>>
    fun getPlugin(type: PluginType): Maybe<IAudioPlugin>
    fun getPluginData(type: PluginType): Maybe<AudioPluginData>
    fun setPluginData(type: PluginType, default: AudioPluginData): Completable
    fun initSelected(): Completable
}
