package org.wycliffeassociates.otter.jvm.device.audio.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorderImpl
import javax.inject.Singleton

@Module
class AudioModule {
    @Provides
    fun providesRecorder(): IAudioRecorder = AudioRecorderImpl()

    @Provides
    fun providesPlayer(): IAudioPlayer = AudioPlayer()
}