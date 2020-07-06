package org.wycliffeassociates.otter.jvm.workbookapp.di.audio

import dagger.Component
import javax.inject.Singleton
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder

@Component(modules = [AudioModule::class])
@Singleton
interface AudioComponent {
    fun injectRecorder(): IAudioRecorder
    fun injectPlayer(): IAudioPlayer
}
