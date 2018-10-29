package org.wycliffeassociates.otter.jvm.device.audioplugin.injection

import dagger.Component
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import javax.inject.Singleton

@Component(modules = [AudioPluginModule::class])
@Singleton
interface AudioPluginComponent {
    fun injectRegistrar(): IAudioPluginRegistrar
}