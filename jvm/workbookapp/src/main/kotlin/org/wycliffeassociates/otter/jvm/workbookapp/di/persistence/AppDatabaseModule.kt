package org.wycliffeassociates.otter.jvm.workbookapp.di.persistence

import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase

@Module
class AppDatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(directoryProvider: IDirectoryProvider): AppDatabase =
        AppDatabase(
            directoryProvider.getAppDataDirectory().resolve(File("content.sqlite"))
        )
}
