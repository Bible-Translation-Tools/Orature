package org.wycliffeassociates.otter.jvm.workbookapp.di.persistence

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider

@Module
class DirectoryProviderModule {
    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider("ProjectOtter")
}
