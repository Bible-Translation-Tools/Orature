package device.audio.injection

import dagger.Module
import dagger.Provides
import device.audio.AudioRecorderImpl
import device.IAudioRecorder

@Module
class AudioModule {
    @Provides
    fun providesRecorder() : IAudioRecorder = AudioRecorderImpl()
}