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
    fun setEditorData(default: AudioPluginData): Completable
    fun getEditorData(): Maybe<AudioPluginData>
    fun setRecorderData(default: AudioPluginData): Completable
    fun getRecorderData(): Maybe<AudioPluginData>
    fun setMarkerData(default: AudioPluginData): Completable
    fun getMarkerData(): Maybe<AudioPluginData>
    fun initSelected(): Completable
}
