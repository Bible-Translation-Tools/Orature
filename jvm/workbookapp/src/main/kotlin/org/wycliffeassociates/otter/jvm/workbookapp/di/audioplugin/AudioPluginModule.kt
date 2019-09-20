package org.wycliffeassociates.otter.jvm.workbookapp.di.audioplugin

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.AudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AudioPluginRepositoryModule

@Module(includes = [AudioPluginRepositoryModule::class])
class AudioPluginModule {
    @Provides
    fun providesRegistrar(audioPluginRepository: IAudioPluginRepository): IAudioPluginRegistrar =
        AudioPluginRegistrar(audioPluginRepository)
}
