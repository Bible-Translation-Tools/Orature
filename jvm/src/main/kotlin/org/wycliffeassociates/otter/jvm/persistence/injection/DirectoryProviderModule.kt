package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.persistence.DirectoryProvider

@Module
class DirectoryProviderModule {
    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider("TranslationRecorder")
}
