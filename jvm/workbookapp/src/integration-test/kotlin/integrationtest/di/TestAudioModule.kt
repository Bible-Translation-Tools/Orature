package integrationtest.di

import dagger.Module
import dagger.Provides
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider

@Module
class TestAudioModule {
    @Provides
    fun providesRecorder(): IAudioRecorder = Mockito.mock(IAudioRecorder::class.java)

    @Provides
    fun providesPlayer(): IAudioPlayer = Mockito.mock(IAudioPlayer::class.java)

    @Provides
    fun providesConnectionFactory(): AudioConnectionFactory = Mockito.mock(AudioConnectionFactory::class.java)

    @Provides
    fun providesWavCreator(): IWaveFileCreator = Mockito.mock(IWaveFileCreator::class.java)

    @Provides
    fun providesAudioDevice(): AudioDeviceProvider = Mockito.mock(AudioDeviceProvider::class.java)
}
