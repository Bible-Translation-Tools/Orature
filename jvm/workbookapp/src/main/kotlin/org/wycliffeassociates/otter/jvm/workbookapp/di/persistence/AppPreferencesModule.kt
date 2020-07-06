package org.wycliffeassociates.otter.jvm.workbookapp.di.persistence

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.AppPreferences
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase

@Module
class AppPreferencesModule {
    @Provides
    @Singleton
    fun providesAppPreferences(database: AppDatabase): IAppPreferences = AppPreferences(database)
}
