package device.audio.injection

import dagger.Component
import javax.inject.Singleton
import device.IAudioRecorder

@Component(modules = [AudioModule::class])
@Singleton
interface AudioComponent {
    fun injectRecorder() : IAudioRecorder
}