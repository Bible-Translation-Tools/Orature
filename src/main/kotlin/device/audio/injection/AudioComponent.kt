package device.audio.injection

import dagger.Component
import javax.inject.Singleton
import device.IAudioRecorder
import device.IAudioPlayer

@Component(modules = [AudioModule::class])
@Singleton
interface AudioComponent {
    fun injectRecorder(): IAudioRecorder
    fun injectPlayer(): IAudioPlayer
}