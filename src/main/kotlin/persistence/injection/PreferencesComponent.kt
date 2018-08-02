package persistence.injection

import dagger.Component
import data.persistence.AppPreferences
import javax.inject.Singleton

@Component(modules = [PreferencesModule::class])
@Singleton
interface PreferencesComponent {
    fun inject() : AppPreferences
}