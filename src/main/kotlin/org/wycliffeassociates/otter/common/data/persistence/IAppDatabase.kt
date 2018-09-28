package org.wycliffeassociates.otter.common.data.persistence

import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin
import org.wycliffeassociates.otter.common.data.dao.LanguageDao
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection

interface IAppDatabase {
    fun getLanguageDao(): LanguageDao
    fun getMetadataDao(): Dao<ResourceMetadata>
    fun getCollectionDao(): Dao<Collection>
    fun getChunkDao(): Dao<Chunk>
    fun getTakeDao(): Dao<Take>
    fun getAudioPluginDao(): Dao<IAudioPlugin>
    fun getAudioPluginDataDao(): Dao<AudioPluginData>
}