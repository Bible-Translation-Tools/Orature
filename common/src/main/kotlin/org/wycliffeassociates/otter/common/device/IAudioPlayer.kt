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
package org.wycliffeassociates.otter.common.device

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.io.File

interface IAudioPlayer {
    val frameStart: Int
    val frameEnd: Int
    fun addEventListener(listener: IAudioPlayerListener)
    fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit)
    fun load(file: File)
    fun loadSection(file: File, frameStart: Int, frameEnd: Int)
    fun getAudioReader(): AudioFileReader?
    fun changeRate(rate: Double)
    fun play()
    fun pause()
    fun stop()
    fun close()
    fun seek(position: Int)
    fun isPlaying(): Boolean
    fun getDurationInFrames(): Int
    fun getDurationMs(): Int
    fun getLocationInFrames(): Int
    fun getLocationMs(): Int
}
