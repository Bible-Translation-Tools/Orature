package org.wycliffeassociates.otter.jvm.persistence.database

import org.wycliffeassociates.otter.jvm.persistence.database.daos.*
import org.wycliffeassociates.otter.jvm.persistence.entities.AudioPluginEntity

// interface for a particular app database implementation
interface IAppDatabase {
    fun getLanguageDao(): ILanguageDao
    fun getResourceMetadataDao(): IResourceMetadataDao
    fun getCollectionDao(): ICollectionDao
    fun getChunkDao(): IChunkDao
    fun getResourceLinkDao(): IResourceLinkDao
    fun getTakeDao(): ITakeDao
    fun getMarkerDao(): IMarkerDao
    fun getAudioPluginDao(): IDao<AudioPluginEntity>
}