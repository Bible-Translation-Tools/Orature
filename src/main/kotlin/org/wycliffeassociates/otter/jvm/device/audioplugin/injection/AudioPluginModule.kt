package org.wycliffeassociates.otter.jvm.device.audioplugin.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.device.audioplugin.AudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.persistence.injection.PersistenceModule

@Module(includes = [PersistenceModule::class])
class AudioPluginModule {
    @Provides
    fun providesRegistrar(audioPluginRepository: IAudioPluginRepository): IAudioPluginRegistrar = AudioPluginRegistrar(
            audioPluginRepository
    )
}