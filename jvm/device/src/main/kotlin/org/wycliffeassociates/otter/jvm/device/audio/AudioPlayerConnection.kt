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

import java.io.File
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener

internal class AudioPlayerConnection(
    val id: Int,
    private val connectionFactory: AudioPlayerConnectionFactory
) : IAudioPlayer {

    private var state = AudioPlayerConnectionState(id)

    override val frameStart: Int
        get() = state?.begin ?: 0
    override val frameEnd: Int
        get() = state?.end ?: 0

    fun addListeners() {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                state.listeners.forEach {
                    connectionFactory.player.addEventListener(it)
                }
            }
        }
    }

    override fun addEventListener(listener: IAudioPlayerListener) {
        state.listeners.add(listener)
        addListeners()
    }

    override fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit) {
        state.listeners.add(
            object : IAudioPlayerListener {
                override fun onEvent(event: AudioPlayerEvent) {
                    onEvent(event)
                }
            }
        )
        addListeners()
    }

    override fun load(file: File) {
        state.file = file
        state.position = 0
        connectionFactory.load(state)
    }

    override fun loadSection(file: File, frameStart: Int, frameEnd: Int) {
        state.file = file
        state.begin = frameStart
        state.end = frameEnd
        state.position = 0
        connectionFactory.load(state)
    }

    override fun getAudioReader(): AudioFileReader? {
        connectionFactory.load(state)
        return connectionFactory.player.getAudioReader()
    }

    override fun changeRate(rate: Double) {
        state.rate = rate
        connectionFactory.load(state)
    }

    override fun play() {
        connectionFactory.load(state)
        connectionFactory.player.play()
    }

    override fun pause() {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                state.position = connectionFactory.player.getLocationInFrames()
                connectionFactory.player.pause()
            }
        }
    }

    override fun stop() {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                state.position = connectionFactory.player.getLocationInFrames()
                return connectionFactory.player.stop()
            }
        }
    }

    override fun close() {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                connectionFactory.player.close()
                connectionFactory.currentConnection = null
            }
        }
        state.position = 0
        connectionFactory.connections.remove(state.id)
    }

    override fun seek(position: Int) {
        connectionFactory.load(state)
        connectionFactory.player.seek(position)
        connectionFactory.load(state)
    }

    override fun isPlaying(): Boolean {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                return connectionFactory.player.isPlaying()
            }
        }
        return false
    }

    override fun getDurationInFrames(): Int {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                return connectionFactory.player.getDurationInFrames()
            }
        }
        return state.durationInFrames
    }

    override fun getDurationMs(): Int {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                return connectionFactory.player.getDurationMs()
            }
        }
        return state.durationInMs
    }

    override fun getLocationInFrames(): Int {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                return connectionFactory.player.getLocationInFrames()
            }
        }
        return state.locationInFrames
    }

    override fun getLocationMs(): Int {
        connectionFactory.currentConnection?.id?.let {
            if (it == id) {
                return connectionFactory.player.getLocationMs()
            }
        }
        return state.locationInMs
    }
}
