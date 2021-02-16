package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File
import javax.inject.Singleton

@Module
class AppDatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(
        directoryProvider: IDirectoryProvider
    ): AppDatabase {
        return AppDatabase(
            directoryProvider
                .getAppDataDirectory()
                .resolve(File("content.sqlite"))
        )
    }
}
