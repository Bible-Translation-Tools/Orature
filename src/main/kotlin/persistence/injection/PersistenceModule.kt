package persistence.injection

import dagger.Module
import dagger.Provides
import data.persistence.AppDatabase
import data.persistence.AppPreferences
import persistence.AppDatabaseImpl
import persistence.AppPreferencesImpl
import persistence.DirectoryProvider
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