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
package org.wycliffeassociates.otter.jvm.device.audio

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder

class AudioConnectionFactory(
    private val errorRelay: PublishRelay<AudioError> = PublishRelay.create()
) {

    private val playerConnectionFactory = AudioPlayerConnectionFactory(errorRelay)
    private val recorderConnectionFactory = AudioRecorderConnectionFactory(errorRelay)

    @Synchronized
    fun setOutputLine(newLine: SourceDataLine?) {
        playerConnectionFactory.setLine(newLine)
    }

    @Synchronized
    fun setInputLine(newLine: TargetDataLine?) {
        recorderConnectionFactory.setLine(newLine)
    }

    fun getPlayer(): IAudioPlayer {
        return playerConnectionFactory.getPlayer()
    }

    fun getRecorder(): IAudioRecorder {
        return recorderConnectionFactory.getRecorder()
    }

    fun errorListener(): Observable<AudioError> {
        return errorRelay
    }

    fun clearPlayerConnections() {
        playerConnectionFactory.clearConnections()
    }

    fun getConnectionCount(): Int {
        return playerConnectionFactory.connections.size
    }

    fun releasePlayer() {
        playerConnectionFactory.releasePlayer()
    }
}
