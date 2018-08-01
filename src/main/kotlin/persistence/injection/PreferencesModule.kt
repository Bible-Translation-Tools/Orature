package persistence.injection

import dagger.Module
import dagger.Provides
import data.persistence.AppPreferences
import persistence.AppPreferencesImpl

@Module
class PreferencesModule {
    @Provides
    fun providesAppPreferences() : AppPreferences = AppPreferencesImpl
}