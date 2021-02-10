package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorder
import org.wycliffeassociates.otter.jvm.workbookapp.io.wav.WaveFileCreator

@Module
class AudioModule {
    @Provides
    fun providesRecorder(): IAudioRecorder = AudioRecorder()

    @Provides
    fun providesPlayer(): IAudioPlayer = AudioBufferPlayer()

    @Provides
    fun providesWavCreator(): IWaveFileCreator = WaveFileCreator()
}
