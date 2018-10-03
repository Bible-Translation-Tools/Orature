package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.data.persistence.IAppPreferences
import org.wycliffeassociates.otter.jvm.persistence.AppPreferences
import org.wycliffeassociates.otter.jvm.persistence.DirectoryProvider
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.database.IAppDatabase
import java.io.File
import javax.inject.Singleton

@Module
class PersistenceModule {
    @Provides
    @Singleton
    fun providesAppDatabase(directoryProvider: IDirectoryProvider) : IAppDatabase
            = AppDatabase(directoryProvider.getAppDataDirectory().resolve(File("content.sqlite")))

    @Provides
    fun providesAppPreferences() : IAppPreferences = AppPreferences

    @Provides
    fun providesDirectoryProvider() : IDirectoryProvider = DirectoryProvider("TranslationRecorder")
}