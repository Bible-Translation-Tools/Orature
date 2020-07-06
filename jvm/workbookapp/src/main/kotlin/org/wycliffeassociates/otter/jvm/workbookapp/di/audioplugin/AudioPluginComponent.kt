package org.wycliffeassociates.otter.jvm.workbookapp.di.audioplugin

import dagger.Component
import javax.inject.Singleton
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.DirectoryProviderModule

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
