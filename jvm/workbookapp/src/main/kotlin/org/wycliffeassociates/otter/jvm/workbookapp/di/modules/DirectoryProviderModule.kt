package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider

@Module
class DirectoryProviderModule {
    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider(OratureInfo.SUITE_NAME)
}
