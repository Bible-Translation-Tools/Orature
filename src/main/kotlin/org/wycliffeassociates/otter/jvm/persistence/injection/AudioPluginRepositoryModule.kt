package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.repositories.AudioPluginRepository

@Module
class AudioPluginRepositoryModule {
    @Provides
    fun providesAudioPluginRepository(database: AppDatabase, preferences: IAppPreferences): IAudioPluginRepository =
        AudioPluginRepository(database, preferences)
}
