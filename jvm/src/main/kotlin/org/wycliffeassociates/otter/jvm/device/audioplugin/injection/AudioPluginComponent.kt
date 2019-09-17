package org.wycliffeassociates.otter.jvm.device.audioplugin.injection

import dagger.Component
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.persistence.injection.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.persistence.injection.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.persistence.injection.DirectoryProviderModule
import javax.inject.Singleton

@Component(
    modules = [
        AudioPluginModule::class,
        AppDatabaseModule::class,
        DirectoryProviderModule::class,
        AppPreferencesModule::class
    ]
)
@Singleton
interface AudioPluginComponent {
    fun injectRegistrar(): IAudioPluginRegistrar
}
