package org.wycliffeassociates.otter.jvm.device.audioplugin.injection

import dagger.Component
import org.wycliffeassociates.otter.common.domain.IAudioPluginRegistrar

@Component(modules = [AudioPluginModule::class])
interface AudioPluginComponent {
    fun injectRegistrar(): IAudioPluginRegistrar
}