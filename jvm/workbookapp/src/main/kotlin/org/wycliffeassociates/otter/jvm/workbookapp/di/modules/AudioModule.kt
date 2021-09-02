/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorder
import org.wycliffeassociates.otter.jvm.workbookapp.io.wav.WaveFileCreator

@Module
class AudioModule {

    companion object {
        private val audioDeviceProvider = AudioDeviceProvider()
        private val defaultFormat = AudioFormat(
            44100F,
            16,
            1,
            true,
            false
        )
        private val line = AudioSystem.getSourceDataLine(defaultFormat)
        val audioConnectionFactory = AudioConnectionFactory(line)
        init {
            audioDeviceProvider.activeOutputDevice.subscribe { mixer ->
                println(mixer.name)
                val newLine = AudioSystem.getSourceDataLine(defaultFormat, mixer)
                audioConnectionFactory.replaceLine(newLine)
            }
        }
    }

    @Provides
    fun providesRecorder(): IAudioRecorder = AudioRecorder()

    @Provides
    fun providesPlayer(): IAudioPlayer = audioConnectionFactory.getPlayer()

    @Provides
    fun providesWavCreator(): IWaveFileCreator = WaveFileCreator()

    @Provides
    fun providesAudioDevice(): AudioDeviceProvider = audioDeviceProvider
}
