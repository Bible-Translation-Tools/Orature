package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin

interface IAudioPluginRepository : IRepository<AudioPluginData> {
    fun insert(data: AudioPluginData): Single<Int>
    fun getAllPlugins(): Single<List<IAudioPlugin>>
}