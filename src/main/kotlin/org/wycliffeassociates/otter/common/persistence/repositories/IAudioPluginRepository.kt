package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin

interface IAudioPluginRepository : IRepository<AudioPluginData> {
    fun insert(data: AudioPluginData): Single<Int>
    fun getAllPlugins(): Single<List<IAudioPlugin>>
    fun setEditorData(default: AudioPluginData): Completable
    fun getEditorData(): Maybe<AudioPluginData>
    fun getEditor(): Maybe<IAudioPlugin>
    fun setRecorderData(default: AudioPluginData): Completable
    fun getRecorderData(): Maybe<AudioPluginData>
    fun getRecorder(): Maybe<IAudioPlugin>
    fun initSelected(): Completable
}