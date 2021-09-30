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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import org.wycliffeassociates.otter.common.device.IAudioPlayer

internal class AudioPlayerConnectionFactory(
    private val errorRelay: PublishRelay<AudioError> = PublishRelay.create()
) {

    lateinit var outputLine: SourceDataLine

    internal lateinit var player: AudioBufferPlayer
        private set

    internal val connections = ConcurrentHashMap<Int, IAudioPlayer>()
    internal val idgen = AtomicInteger(1)
    internal var currentConnection: AudioPlayerConnectionState? = null

    @Synchronized
    fun setLine(newLine: SourceDataLine) {
        if (this::player.isInitialized) {
            player.pause()
        } else {
            player = AudioBufferPlayer(newLine, errorRelay)
        }
        newLine.close()
        outputLine = newLine
        currentConnection?.let {
            load(it)
        }
    }

    fun getPlayer(): IAudioPlayer {
        val id = idgen.getAndIncrement()
        val audioConnection = AudioPlayerConnection(id, this)
        connections[id] = audioConnection
        return audioConnection
    }

    private fun swapConnection(request: AudioPlayerConnectionState) {
        saveStateOfPlayer()
        currentConnection = request
    }

    private fun saveStateOfPlayer() {
        currentConnection?.let {
            it.position = player.getLocationInFrames()
            it.durationInFrames = player.getDurationInFrames()
            it.locationInFrames = player.getLocationInFrames()
            it.durationInMs = player.getDurationMs()
            it.durationInFrames = player.getDurationInFrames()
        }
    }

    private fun loadRequestIntoPlayer(request: AudioPlayerConnectionState) {
        request.listeners.forEach {
            player.addEventListener(it)
        }
        if (request.begin != null && request.end != null) {
            player.loadSection(request.file, request.begin!!, request.end!!)
        } else {
            player.load(request.file)
        }
    }

    /**
     * New connections won't know of duration until the file is loaded and a player
     * can access the AudioFile reader.
     */
    private fun setDurationOfConnection(request: AudioPlayerConnectionState) {
        request.durationInFrames = player.getDurationInFrames()
        request.durationInMs = player.getDurationMs()
    }

    @Synchronized
    internal fun load(request: AudioPlayerConnectionState) {
        try {
            player.pause()
            swapConnection(request)
            outputLine.flush()
            player.stop()
            player = AudioBufferPlayer(outputLine, errorRelay)
            setDurationOfConnection(request)
            loadRequestIntoPlayer(request)
            player.seek(request.position)
        } catch (e: LineUnavailableException) {
            errorRelay.accept(AudioError(AudioErrorType.PLAYBACK, e))
        }
    }
}
