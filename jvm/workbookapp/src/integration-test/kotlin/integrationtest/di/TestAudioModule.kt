/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
