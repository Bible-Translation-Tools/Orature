package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Binds
import dagger.Module
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.AppPreferences

@Module
abstract class AppPreferencesModule {
    @Binds
    abstract fun inject(preferences: AppPreferences): IAppPreferences
}
