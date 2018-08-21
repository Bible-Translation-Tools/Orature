package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Module
import dagger.Provides
import data.persistence.AppDatabase
import data.persistence.AppPreferences
import org.wycliffeassociates.otter.jvm.persistence.AppDatabaseImpl
import org.wycliffeassociates.otter.jvm.persistence.AppPreferencesImpl
import org.wycliffeassociates.otter.jvm.persistence.DirectoryProvider
import persistence.IDirectoryProvider

@Module
class PersistenceModule {
    @Provides
    fun providesAppDatabase() : AppDatabase = AppDatabaseImpl

    @Provides
    fun providesAppPreferences() : AppPreferences = AppPreferencesImpl

    @Provides
    fun providesDirectoryProvider() : IDirectoryProvider = DirectoryProvider("TranslationRecorder")
}