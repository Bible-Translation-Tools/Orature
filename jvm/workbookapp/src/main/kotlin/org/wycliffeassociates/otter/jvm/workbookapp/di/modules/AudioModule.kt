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
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorder
import org.wycliffeassociates.otter.jvm.workbookapp.io.wav.WaveFileCreator

@Module
class AudioModule {

    companion object {
        val audioConnectionFactory = AudioConnectionFactory()
    }

    @Provides
    fun providesRecorder(): IAudioRecorder = AudioRecorder()

    @Provides
    fun providesPlayer(): IAudioPlayer = audioConnectionFactory.getPlayer()

    @Provides
    fun providesWavCreator(): IWaveFileCreator = WaveFileCreator()
}
