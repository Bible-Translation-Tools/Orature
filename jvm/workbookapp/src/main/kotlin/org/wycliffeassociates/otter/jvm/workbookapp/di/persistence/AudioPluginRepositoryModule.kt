package org.wycliffeassociates.otter.jvm.workbookapp.di.persistence

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.AudioPluginRepository

//@Module
//class AudioPluginRepositoryModule {
//    @Provides
//    fun providesAudioPluginRepository(database: AppDatabase, preferences: IAppPreferences): IAudioPluginRepository =
//        AudioPluginRepository(database, preferences)
//}
