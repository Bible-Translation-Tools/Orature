package org.wycliffeassociates.otter.common.data.persistence

import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.dao.LanguageDao
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.audioplugin.IAudioPlugin

interface AppDatabase {
    fun getUserDao(): Dao<User>
    fun getLanguageDao(): LanguageDao
    fun getProjectDao(): Dao<Project>
    fun getAnthologyDao(): Dao<Anthology>
    fun getBookDao(): Dao<Book>
    fun getChapterDao(): Dao<Chapter>
    fun getChunkDao(): Dao<Chunk>
    fun getTakesDao(): Dao<Take>
    fun getAudioPluginDao(): Dao<IAudioPlugin>
    fun getAudioPluginDataDao(): Dao<AudioPluginData>
}