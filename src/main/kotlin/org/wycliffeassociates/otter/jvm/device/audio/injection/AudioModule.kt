package org.wycliffeassociates.otter.jvm.device.audio.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorder

@Module
class AudioModule {
    @Provides
    fun providesRecorder(): IAudioRecorder = AudioRecorder()

    @Provides
    fun providesPlayer(): IAudioPlayer = AudioPlayer()
}